package it.seraph.license.entities;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "licenza", nullable = false, unique = true)
    private String licenza;
    
    @Column(name = "hwid", nullable = true, unique = true)
    private String hwid;
    
    @Column(name = "scadenza", nullable = false)
    private ZonedDateTime scadenza;
    
    @Column(name = "bannato", nullable = false)
    private Boolean bannato;
    
    @Column(name = "commento", nullable = true)
    private String commento;

}
