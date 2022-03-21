package ca.carleton.controllers;

import ca.carleton.models.Admin;
import ca.carleton.models.Customer;
import ca.carleton.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller()
public class AccountController {

    Admin admin = new Admin("AdminSteve", "Steve", "123abc");

    @GetMapping("/")
    public String loginForm(Model model){
        model.addAttribute("customer", new Customer());
        return "index";
    }

    @PostMapping("/")
    public String loginSubmit(Model model, @ModelAttribute Customer customer){
        return "profile";
    }

    @GetMapping("/signup")
    public String signUpForm(Model model){
        model.addAttribute("customer", new Customer());
        return "signup";
    }

    @PostMapping("/signup")
    public String signUpSubmit(Model model, @ModelAttribute Customer customer){
        return "profile";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        return "profile";
    }
/*
    @PostMapping("/manageuser")
    public String manageUsers(Model model){

        User user = this.userRepository.findByUsername(username);

        if (user == admin) {

        }
        return "manageuser";
    }
*/

    @GetMapping("/adminDash")
    public String adminForm(@ModelAttribute User user, Model model){

        if (user instanceof Admin) {
            return String.format("redirect:/adminDash/%s", user.getUsername());
        }
        return "admin";
    }

}
