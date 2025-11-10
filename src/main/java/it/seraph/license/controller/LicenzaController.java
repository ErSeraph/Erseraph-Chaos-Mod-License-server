package it.seraph.license.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.seraph.license.dto.CheckRequest;
import it.seraph.license.services.UserService;

@RestController
@RequestMapping("/license")
public class LicenzaController {

	@Autowired
	private UserService userService;

    @PostMapping("/check")
    public ResponseEntity<?> check(@RequestBody CheckRequest req) {
        return userService.checkLicense(req);
    }
}
