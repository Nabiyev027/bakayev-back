package org.example.backend.dtoResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;
@JsonIgnoreProperties(ignoreUnknown = true)
public interface StudentProjection {
     UUID getId();
    String getFirstName(); //
    String getLastName();
    String getPhone();
}
