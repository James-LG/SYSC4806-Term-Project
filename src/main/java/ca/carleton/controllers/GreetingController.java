package ca.carleton.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller()
public class GreetingController {

    @GetMapping("/profile")
    public String profile(Model model) {
        return "profile";
    }
}
