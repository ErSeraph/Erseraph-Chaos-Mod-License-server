package it.seraph.license.controller;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import it.seraph.license.dto.CheckRequest;
import it.seraph.license.dto.CheckResponse;
import it.seraph.license.entities.Users;
import it.seraph.license.repositories.UserRepository;

@RestController
@RequestMapping("/license")
public class LicenzaController {

	@Autowired
	private UserRepository userRepository;
	
    private final RSAPrivateKey privateKey;
    private final Algorithm algorithm;

    public LicenzaController(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
        this.algorithm = Algorithm.RSA256(null, privateKey);
    }

    @PostMapping("/check")
    public ResponseEntity<?> check(@RequestBody CheckRequest req) {
        Users u = userRepository.findByLicenza(req.getLicenza()).orElse(null);
        if (u == null) return ResponseEntity.status(404).body("Not found");
        if (u.getScadenza().isBefore(ZonedDateTime.now())) return ResponseEntity.status(403).body("Expired");
        //if (u.getHwid() != null && !u.getHwid().equals(req.getHwid())) return ResponseEntity.status(403).body("HWID mismatch");

        Instant now = Instant.now();
        String token = JWT.create()
                .withSubject(u.getUsername())
                .withClaim("licenza", u.getLicenza())
                .withClaim("nonce", req.getNonce())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(120)))
                .sign(algorithm);

        return ResponseEntity.ok(new CheckResponse(token));
    }
}
