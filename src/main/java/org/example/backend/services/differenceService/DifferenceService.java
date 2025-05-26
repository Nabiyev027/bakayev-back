package org.example.backend.services.differenceService;

import org.example.backend.entity.DifferenceSection;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DifferenceService {
    void createDifference(MultipartFile img, String title, String description, String lang);

    void deleteRef(UUID id);

    void editDif(UUID id, MultipartFile img, String title, String description, String lang);

    List<DifferenceSection> getDifference();
}
