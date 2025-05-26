package org.example.backend.services.locationService;

import org.example.backend.entity.LocationSection;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface LocationService {
    List<LocationSection> getLocations();

    void addNewLocation(MultipartFile img, String address);

    void editLocation(UUID id, MultipartFile img, String address);

    void deleteLocation(UUID id);
}
