package ca.carleton.controllers;

import ca.carleton.models.Admin;
import ca.carleton.models.Customer;
import ca.carleton.models.User;
import ca.carleton.models.UserRepository;
import ca.carleton.services.SecurityService;
import ca.carleton.services.UserService;
import io.github.bucket4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller()
public class AccountController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final Map<String, Bucket> cache;

    private static final long MAX_API_REQUESTS = 1000;


    @Autowired
    public AccountController(
            UserService userService,
            UserRepository userRepository,
            SecurityService securityService
    ) {
        cache = new ConcurrentHashMap<>();

        this.userService = userService;
        this.userRepository = userRepository;
        this.securityService = securityService;
    }

    @GetMapping("/")
    public String index() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        User user = userService.findByUsername(principal.getUsername());

        if (user == null) {
            authentication.setAuthenticated(false);
        }

        if (authentication.isAuthenticated()) {
            if( user instanceof Admin ) {
                return "redirect:/adminDash";
            }
            return "redirect:/profile";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("error", "Your username and password is invalid.");
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        model.addAttribute("customer", new Customer());
        return "login";
    }

    @GetMapping("/signup")
    public String signUpForm(Model model){
        model.addAttribute("customer", new Customer());
        return "signup";
    }

    @PostMapping("/signup")
    public String signUpSubmit(Model model, @ModelAttribute Customer customer) {
        User user = userService.findByUsername(customer.getUsername());

        if(user == null){
            String unencodedPassword = customer.getPassword();
            customer.startExpiration();
            customer.setSubscription(false);
            this.userService.save(customer);
            securityService.autoLogin(customer.getUsername(), unencodedPassword);
            return "redirect:/profile";
        }

        model.addAttribute("message", "Username already exists.");
        return "signup";

    }

    @GetMapping("/profile")
    public ModelAndView profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        if (user instanceof Customer) {
            model.addAttribute("isCustomer", true);
            Customer customer = (Customer)user;
            Date currentDate = new Date();

            // If access has expired, access is unauthorized
            if (customer.getAccessExpiration().before(currentDate)) {
                return new ModelAndView("unauthorized", HttpStatus.FORBIDDEN);
            }
        }

        if (user == null) {
            model.addAttribute("username", userDetails.getUsername());
            return new ModelAndView("profile-not-found", HttpStatus.NOT_FOUND);
        }

        Bucket bucket = resolveBucket(user.getUsername());
        model.addAttribute("remainingApiRequests", bucket.getAvailableTokens());
        model.addAttribute("maxApiRequests", MAX_API_REQUESTS);

        model.addAttribute("user", user);
        
        return new ModelAndView("profile");
    }

    @GetMapping("/adminDash")
    public ModelAndView adminForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        if (user == null) {
            model.addAttribute("username", userDetails.getUsername());
            return new ModelAndView("profile-not-found", HttpStatus.NOT_FOUND);
        }

        if (user instanceof Admin) {
            model.addAttribute("customers", userService.getAllCustomers());
            return new ModelAndView("adminDash");
        }

        model.addAttribute("username", userDetails.getUsername());
        return new ModelAndView("unauthorized", HttpStatus.FORBIDDEN);
    }

    @GetMapping("/makeRequest")
    public ModelAndView requestData(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        if (user == null) {
            model.addAttribute("username", userDetails.getUsername());
            return new ModelAndView("profile-not-found", HttpStatus.NOT_FOUND);
        } else {
            if (user instanceof Customer) {
                Customer customer = (Customer)user;
                if (customer.getSubscription()) {
                    return new ModelAndView("redirect:/profile");
                } else {
                    Bucket bucket = resolveBucket(user.getUsername());

                    if (bucket.tryConsume(1)) {
                        return new ModelAndView("redirect:/profile");
                    } else {
                        return new ModelAndView("too-many-requests", HttpStatus.TOO_MANY_REQUESTS);
                    }
                }
            }
        }
        return new ModelAndView("redirect:/profile");
    }

    private Bucket resolveBucket(String username) {
        return cache.computeIfAbsent(username, this::newBucket);
    }
    private Bucket newBucket(String username) {
        Bandwidth limit = Bandwidth.classic(MAX_API_REQUESTS, Refill.greedy(1000, Duration.ofDays(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @GetMapping("/upgrade/{username}")
    public ModelAndView upgrade(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String username, Model model) {
        return changeSubscription(true, userDetails, username, model);
    }

    @GetMapping("/downgrade/{username}")
    public ModelAndView downgrade(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String username, Model model) {
        return changeSubscription(false, userDetails, username, model);
    }

    private ModelAndView changeSubscription(boolean targetSubscription, UserDetails authUserDetails, String targetUsername, Model model) {
        User authUser = userService.findByUsername(authUserDetails.getUsername());

        if (authUser instanceof Admin) {
            User targetUser = userService.findByUsername(targetUsername);

            if (targetUser instanceof Customer) {
                Customer customer = (Customer)targetUser;
                customer.setSubscription(targetSubscription);
                userRepository.save(customer);
                model.addAttribute("subscription", customer.getSubscription());
                return new ModelAndView("redirect:/adminDash");
            }
        }

        return new ModelAndView("unauthorized", HttpStatus.FORBIDDEN);
    }

}
