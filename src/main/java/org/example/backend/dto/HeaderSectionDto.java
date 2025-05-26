package org.example.backend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class HeaderSectionDto {
    private String title;
    private String imgUrl;
}
