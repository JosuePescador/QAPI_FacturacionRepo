package cc.nuvu.qapi.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping(value = { "", "/", "/status" }, produces = MediaType.TEXT_PLAIN_VALUE)
    public String getStatus() {
        return new String("Application is Up");
    }
}
