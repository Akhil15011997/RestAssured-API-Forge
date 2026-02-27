package com.example.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
}
