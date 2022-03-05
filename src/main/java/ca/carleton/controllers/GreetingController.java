package ca.carleton.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller()
public class GreetingController {

    @GetMapping("/greeting")
    public String greeting(Model model) {
        return "greeting";
    }
}
