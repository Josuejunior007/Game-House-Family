package io.github.josuejunior007.gamehousefamily.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home(){

        return "Game House Family";
    }

}
