package ca.carleton.controllers;

import ca.carleton.models.Admin;
import ca.carleton.models.Customer;
import ca.carleton.models.User;
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
        }

        return new ModelAndView("redirect:/profile");
    }

    @PostMapping("/adminDash")
    public String changeSub(@ModelAttribute User user, Model model) {
        user.setSubscription(!user.getSubscription());
        return "admin";
    }

    @PostMapping("/profile")
    public String upgrade(@ModelAttribute Customer customer, Model model){

        customer.setSubscription(true);

        return "upgrade";
    }
}
