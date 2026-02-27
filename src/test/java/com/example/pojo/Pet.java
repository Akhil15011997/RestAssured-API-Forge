package com.example.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pet implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("category")
    private Category category;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("photoUrls")
    private List<String> photoUrls;
    
    @JsonProperty("tags")
    private List<Tag> tags;
    
    @JsonProperty("status")
    private Status status;
    
    public enum Status {
        @JsonProperty("available")
        AVAILABLE("available"),
        
        @JsonProperty("pending")
        PENDING("pending"),
        
        @JsonProperty("sold")
        SOLD("sold");
        
        private final String value;
        
        Status(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
}
