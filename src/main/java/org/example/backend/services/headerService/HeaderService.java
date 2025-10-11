package org.example.backend.services.headerService;

import org.example.backend.dtoResponse.HeaderSectionDto;
import org.example.backend.dtoResponse.HomeSectionResDto;
import org.springframework.web.multipart.MultipartFile;

public interface HeaderService {
    void postOrEdit(MultipartFile img, String titleUz, String titleRu, String titleEn);

    HeaderSectionDto getHeader();

    HomeSectionResDto getHeaderInfo(String lang);
}
