package org.example.backend.services.footerService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.FooterSectionDto;
import org.example.backend.entity.FooterSection;
import org.example.backend.repository.FooterSectionRepo;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FooterServiceImpl implements FooterService {

    private final FooterSectionRepo footerSectionRepo;

    @Override
    public FooterSection getInfo() {
        return footerSectionRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Footer information not found"));
    }

    @Override
    public void postInfo(FooterSectionDto footerSectionDto) {
        FooterSection footerSection = new FooterSection();
        footerSection.setPhone1(footerSectionDto.getPhone1());
        footerSection.setPhone2(footerSectionDto.getPhone2());
        footerSection.setEmail(footerSectionDto.getEmail());
        footerSection.setInstagramUrl(footerSectionDto.getInstagramUrl());
        footerSection.setTelegramUrl(footerSectionDto.getTelegramUrl());
        footerSection.setFacebookUrl(footerSectionDto.getFacebookUrl());
        footerSectionRepo.save(footerSection);
    }

    @Override
    public void updateInfo(UUID id, FooterSectionDto footerSectionDto) {
        FooterSection info = footerSectionRepo.findById(id).get();
        info.setPhone1(footerSectionDto.getPhone1());
        info.setPhone2(footerSectionDto.getPhone2());
        info.setEmail(footerSectionDto.getEmail());
        info.setInstagramUrl(footerSectionDto.getInstagramUrl());
        info.setTelegramUrl(footerSectionDto.getTelegramUrl());
        info.setFacebookUrl(footerSectionDto.getFacebookUrl());
        footerSectionRepo.save(info);
    }

    @Override
    public void deleteInfo(UUID id) {
        footerSectionRepo.findById(id)
                .ifPresentOrElse(footerSectionRepo::delete,
                        () -> { throw new EntityNotFoundException("Footer section not found with id: " + id); });
    }

}
