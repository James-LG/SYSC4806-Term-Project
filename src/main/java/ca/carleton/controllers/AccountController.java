package ca.carleton.controllers;

import ca.carleton.models.Customer;
import ca.carleton.models.User;
import ca.carleton.models.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller()
public class AccountController {
    private final UserRepository userRepository;

    @Autowired
    public AccountController(
            UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String loginForm(Model model){
        model.addAttribute("customer", new Customer());
        return "index";
    }

    @PostMapping("/")
    public String loginSubmit(Model model, @ModelAttribute Customer customer){
        return String.format("redirect:/profile/%s", customer.getUsername());
    }

    @GetMapping("/signup")
    public String signUpForm(Model model){
        model.addAttribute("customer", new Customer());
        return "signup";
    }

    @PostMapping("/signup")
    public String signUpSubmit(Model model, @ModelAttribute Customer customer) {
        this.userRepository.save(customer);
        return String.format("redirect:/profile/%s", customer.getUsername());
    }

    @GetMapping("/profile/{username}")
    public ModelAndView profile(@PathVariable String username, Model model) {
        User user = this.userRepository.findByUsername(username);

        if (user == null) {
            model.addAttribute("username", username);
            return new ModelAndView("profile-not-found", HttpStatus.NOT_FOUND);
        }

        model.addAttribute("customer", user);
        
        return new ModelAndView("profile");
    }

    @GetMapping("/makeRequest/{username}")
    public String requestData(@PathVariable String username){
        System.out.println("MAKE API CALL");
        return String.format("redirect:/profile/%s", username);
    }


}
