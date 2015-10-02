package com.freshcard.backend;

import com.mangofactory.swagger.annotations.ApiIgnore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@ApiIgnore
@Controller
@RequestMapping("/")
public class StartController {
    @RequestMapping(method = RequestMethod.GET)
    public String welcome() {
        return "index";
    }

    @RequestMapping(value = "/api-docs-html", method = RequestMethod.GET)
    public String apiDocs() {
        return "apiDocs";
    }
}