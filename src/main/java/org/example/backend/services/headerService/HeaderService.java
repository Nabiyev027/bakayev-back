package org.example.backend.services.headerService;

import org.example.backend.dto.HeaderSectionDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface HeaderService {
    void postTitle(String title, MultipartFile img, String lang);

    void editTitle(UUID id, String title, MultipartFile img, String lang);

    HeaderSectionDto getHeader(UUID id, String lang);
}
