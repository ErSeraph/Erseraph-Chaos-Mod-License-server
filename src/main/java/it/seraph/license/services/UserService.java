package it.seraph.license.services;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import it.seraph.license.dto.CheckRequest;
import it.seraph.license.entities.TikTokLog;
import it.seraph.license.entities.Users;
import it.seraph.license.repositories.TikTokLogRepository;
import it.seraph.license.repositories.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TikTokLogRepository tikTokLogRepository;
	
    private final RSAPrivateKey privateKey;
    private final Algorithm algorithm;
    
    public UserService(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
        this.algorithm = Algorithm.RSA256(null, privateKey);
    }
    
    public ResponseEntity<String> checkLicense(CheckRequest req) {
    	Users u = userRepository.findByLicenza(req.getLicenza()).orElse(null);
        if (u == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(HttpStatus.NOT_FOUND.name());
        
        if (u.getBannato().booleanValue()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(u.getCommento());
        
        if (u.getScadenza().isBefore(ZonedDateTime.now(ZoneOffset.UTC))) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Expired");
        
        if (u.getHwid() == null) {
        	u.setHwid(req.getHwid());
        	u = userRepository.save(u);
        }
        
        if (!u.getHwid().equals(req.getHwid())) {
        	u.setCommento("Bannato per HWID diverso");
        	u.setBannato(true);
        	u = userRepository.save(u);
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(u.getCommento());
        }

        Instant now = Instant.now();
        String token = JWT.create()
                .withSubject(u.getUsername())
                .withClaim("licenza", u.getLicenza())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(120)))
                .sign(algorithm);

        return ResponseEntity.ok(token);
    }

    public boolean isLoggable(Long hostId) {
        Users utente = userRepository.findByLicenza(hostId.toString()).orElse(null);
        if (utente == null) return false;
        return Boolean.TRUE.equals(utente.getLog());
    }

	public void saveLog(Long hostId, Long liveId, Long timestamp, Long diamanti) {
        Users utente = userRepository.findByLicenza(hostId.toString()).orElse(null);
        if (utente == null) return;
        tikTokLogRepository.save(new TikTokLog(utente, liveId, timestamp, diamanti));
    }

	public void assignTTID(String username, Long id) {
		Users utente = userRepository.findByUsername(username).orElse(null);
        if (utente == null) return;
        if (utente.getLicenza() == null) {
            utente.setLicenza(id.toString());
            userRepository.save(utente);
        }
	}

	public void updateUsername(String licenza, String newUsername) {
		Users utente = userRepository.findByLicenza(licenza).orElse(null);
		if (utente == null) return;
		utente.setUsername(newUsername);
		userRepository.save(utente);
	}
	
}
