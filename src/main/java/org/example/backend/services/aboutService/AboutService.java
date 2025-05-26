package org.example.backend.services.aboutService;

import org.example.backend.dto.AboutSectionDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AboutService {
    AboutSectionDto getAbout(String lang);

    void addAbout(MultipartFile img, String video, String description1, String description2, String lang);

    void editAbout(UUID id, MultipartFile img, String video, String description1, String description2, String lang);

    void deleteAbout(UUID id);
}
