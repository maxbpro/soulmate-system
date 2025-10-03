package ru.maxb.soulmate.profile;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/v1")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @GetMapping("")
    public String test() {
        log.info("Test Hello World");
        return "Hello World";
    }
}
