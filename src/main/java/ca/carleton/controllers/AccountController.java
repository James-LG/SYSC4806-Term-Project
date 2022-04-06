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

        if (user == null){
            String unencodedPassword = customer.getPassword();
            customer.startExpiration();
            customer.setSubscription(false);
            customer.setAccessLimit(Customer.TRIAL_ACCESS_LIMIT);
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

            Bucket bucket = resolveBucket(customer);
            model.addAttribute("remainingApiRequests", bucket.getAvailableTokens());
            model.addAttribute("maxApiRequests", customer.getAccessLimit());
        }

        if (user == null) {
            model.addAttribute("username", userDetails.getUsername());
            return new ModelAndView("profile-not-found", HttpStatus.NOT_FOUND);
        }

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
                Bucket bucket = resolveBucket(customer);

                if (bucket.tryConsume(1)) {
                    return new ModelAndView("redirect:/profile");
                } else {
                    model.addAttribute("username", userDetails.getUsername());
                    return new ModelAndView("too-many-requests", HttpStatus.TOO_MANY_REQUESTS);
                }
            }
        }
        return new ModelAndView("redirect:/profile");
    }

    private Bucket resolveBucket(Customer customer) {
        Bucket bucket = cache.getOrDefault(customer.getUsername(), null);
        if (bucket == null) {
            bucket = newBucket(customer);
            cache.putIfAbsent(customer.getUsername(), bucket);
        }

        return bucket;
    }

    private Bucket newBucket(Customer customer) {
        Bandwidth limit = Bandwidth.classic(customer.getAccessLimit(), Refill.greedy(customer.getAccessLimit(), Duration.ofDays(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @GetMapping("/changeAccess/{targetUsername}")
    public ModelAndView changeAccess(@AuthenticationPrincipal UserDetails authUserDetails, @PathVariable String targetUsername, @RequestParam int value, Model model) {
        User authUser = userService.findByUsername(authUserDetails.getUsername());

        if (authUser instanceof Admin) {
            User targetUser = userService.findByUsername(targetUsername);

            if (targetUser instanceof Customer) {
                Customer customer = (Customer)targetUser;
                customer.setAccessLimit(value);
                cache.replace(customer.getUsername(), newBucket(customer));

                userRepository.save(customer);

                return new ModelAndView("redirect:/adminDash");
            }
        }

        model.addAttribute("username", authUserDetails.getUsername());
        return new ModelAndView("unauthorized", HttpStatus.FORBIDDEN);
    }

    @GetMapping("/upgrade/{targetUsername}")
    public ModelAndView upgrade(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String targetUsername, Model model) {
        return changeSubscription(true, userDetails, targetUsername, model);
    }

    @GetMapping("/downgrade/{targetUsername}")
    public ModelAndView downgrade(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String targetUsername, Model model) {
        return changeSubscription(false, userDetails, targetUsername, model);
    }

    private ModelAndView changeSubscription(boolean targetSubscription, UserDetails authUserDetails, String targetUsername, Model model) {
        User authUser = userService.findByUsername(authUserDetails.getUsername());

        if (authUser instanceof Admin) {
            User targetUser = userService.findByUsername(targetUsername);

            if (targetUser instanceof Customer) {
                Customer customer = (Customer)targetUser;
                customer.setSubscription(targetSubscription);

                // Adjust customer's access limits
                if (customer.getSubscription()) {
                    customer.setAccessLimit(Customer.PAID_ACCESS_LIMIT);
                } else {
                    customer.setAccessLimit(Customer.TRIAL_ACCESS_LIMIT);
                }
                cache.replace(customer.getUsername(), newBucket(customer));

                userRepository.save(customer);

                return new ModelAndView("redirect:/adminDash");
            }
        }

        model.addAttribute("username", authUserDetails.getUsername());
        return new ModelAndView("unauthorized", HttpStatus.FORBIDDEN);
    }

}
