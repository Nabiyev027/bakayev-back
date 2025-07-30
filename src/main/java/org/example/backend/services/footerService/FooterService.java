package org.example.backend.services.footerService;

import org.example.backend.dto.FooterSectionDto;
import org.example.backend.entity.FooterSection;

import java.util.UUID;

public interface FooterService {
    FooterSection getInfo();

    void postAndUpdateInfo(FooterSectionDto footerSectionDto);
}
