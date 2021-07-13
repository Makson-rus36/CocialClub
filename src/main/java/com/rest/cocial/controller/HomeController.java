package com.rest.cocial.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping( "/v1/home" )
@CrossOrigin(origins = {"http://localhost:4200/","https://social-club-vchat.herokuapp.com/"})
public class HomeController {

    @GetMapping
    public Map<String, String> getUserName(@AuthenticationPrincipal( expression = "attributes['name']" ) String username ) {
        return Collections.singletonMap("name", username);
    }
}
