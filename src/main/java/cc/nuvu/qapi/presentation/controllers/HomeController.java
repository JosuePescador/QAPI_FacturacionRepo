package cc.nuvu.qapi.presentation.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping(value = { "", "/", "/status" }, produces = MediaType.TEXT_PLAIN_VALUE)
    public String getStatus() {
        return "1.1.1";
    }
}
