package ca.carleton.controllers;

import ca.carleton.models.Admin;
import ca.carleton.models.Customer;
import ca.carleton.models.User;
import ca.carleton.models.UserRepository;
import ca.carleton.services.SecurityService;
import ca.carleton.services.UserService;
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

@Controller()
public class AccountController {
    private final UserService userService;
    private final SecurityService securityService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    public AccountController(
            UserService userService,
            SecurityService securityService
    ) {
        this.userService = userService;
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
        String unencodedPassword = customer.getPassword();
        customer.startExpiration();
        customer.setSubscription("TRIAL");
        this.userService.save(customer);
        securityService.autoLogin(customer.getUsername(), unencodedPassword);

        return "redirect:/profile";
    }

    @GetMapping("/profile")
    public ModelAndView profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        if (user == null) {
            model.addAttribute("username", userDetails.getUsername());
            return new ModelAndView("profile-not-found", HttpStatus.NOT_FOUND);
        }

        model.addAttribute("customer", user);
        
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
            model.addAttribute("customers", userService.allCustomer());
            return new ModelAndView("adminDash");
        }

        model.addAttribute("username", userDetails.getUsername());
        return new ModelAndView("unauthorized", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/makeRequest")
    public ModelAndView requestData(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        if (user == null) {
            model.addAttribute("username", userDetails.getUsername());
            return new ModelAndView("profile-not-found", HttpStatus.NOT_FOUND);
        } else {
            if (user instanceof Customer) {
                if (((Customer) user).getSubBol()) {
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
    }

    @GetMapping("/upgrade")
    public ModelAndView upgrade(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        if(user instanceof Customer) {
            ((Customer) user).setSubscription("PAID");
            userRepository.save(user);
            model.addAttribute("sub", ((Customer) user).getSubBol());
            model.addAttribute("subscription", ((Customer) user).getSubscription());
        }
        return new ModelAndView("redirect:/profile");
    }

/*
    @GetMapping("/changeSub")
    public ModelAndView changeSub(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String sub, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        if(user instanceof Customer) {
            ((Customer) user).setSubscription(sub);
            userRepository.save(user);
            model.addAttribute("subscription", ((Customer) user).getSubscription());
        }
        return new ModelAndView("redirect:/adminDash");
    }

 */
}
