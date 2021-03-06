package com.hihiboss.daangnpriceapi.interfaces.web;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class WebController {

    @GetMapping("/")
    public String main() {
        return "main";
    }

    @GetMapping("/history")
    public String history() {
        return "history";
    }
}
