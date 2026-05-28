package com.claw.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FaissSearchResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean ok;
    private List<String> passages;
    private List<Double> scores;
    private String message;
}
