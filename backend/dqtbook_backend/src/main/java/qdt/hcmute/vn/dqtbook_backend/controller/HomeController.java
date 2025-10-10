package qdt.hcmute.vn.dqtbook_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/")   // router: http://localhost:8080/
    public String home() {
        return "Hello, this is Home page!";
    }
}