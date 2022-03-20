package ca.carleton.controllers;

import ca.carleton.models.Customer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller()
public class AccountController {
    private Customer customer;

    @GetMapping("/")
    public String loginForm(Model model){
        model.addAttribute("customer", new Customer());
        return "index";
    }

    @PostMapping("/")
    public String loginSubmit(Model model, @ModelAttribute Customer customer){
        this.customer = customer;
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

    @RequestMapping("/makeRequest")
    public String requestData(Model model){
        model.addAttribute("customer", customer);
        System.out.println("MAKE API CALL");
        return "profile";
    }


}
