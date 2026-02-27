package com.example.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("code")
    private Integer code;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("message")
    private String message;
}
