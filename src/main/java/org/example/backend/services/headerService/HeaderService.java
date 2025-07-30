package org.example.backend.services.headerService;

import org.example.backend.dto.HeaderSectionDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface HeaderService {
    void postOrEdit(String title, MultipartFile img, String lang);

    HeaderSectionDto getHeader(String lang);
}
