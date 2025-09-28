package org.example.backend.services.aboutService;

import org.example.backend.dtoResponse.AboutSectionResDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AboutService {
    AboutSectionResDto getAbout();

    void aboutPostAndUpdate(MultipartFile img, MultipartFile video, String description1Uz,
                            String description1Ru, String description1En,
                            String description2Uz, String description2Ru, String description2En);

    void deleteAbout(UUID id);
}
