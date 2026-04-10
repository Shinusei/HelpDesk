package com.example.helpdesk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    /**
     * Forwards all non-API and non-resource requests to index.html 
     * so that the Svelte router can handle the navigation.
     */
    @GetMapping(value = {
        "/",
        "/{path:[^\\.]*}",
        "/**/{path:[^\\.]*}"
    })
    public String redirect() {
        return "forward:/index.html";
    }
}
