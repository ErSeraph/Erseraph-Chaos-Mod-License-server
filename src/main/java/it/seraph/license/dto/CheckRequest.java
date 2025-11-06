package it.seraph.license.dto;

import lombok.Data;

@Data
public class CheckRequest {

    private String licenza;
    private String hwid;
}