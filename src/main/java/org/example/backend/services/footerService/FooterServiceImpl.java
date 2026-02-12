package org.example.backend.services.footerService;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.FooterSectionDto;
import org.example.backend.entity.FooterSection;
import org.example.backend.repository.FooterSectionRepo;
import org.springframework.stereotype.Service;


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
    public void postAndUpdateInfo(FooterSectionDto dto) {
        FooterSection footerSection = footerSectionRepo.findTopByOrderByIdAsc()
                .orElse(new FooterSection());

        if (dto.getPhone1() != null && !dto.getPhone1().isBlank())
            footerSection.setPhone1(dto.getPhone1());

        if (dto.getPhone2() != null && !dto.getPhone2().isBlank())
            footerSection.setPhone2(dto.getPhone2());

        if (dto.getEmail() != null && !dto.getEmail().isBlank())
            footerSection.setEmail(dto.getEmail());

        if (dto.getInstagramUrl() != null && !dto.getInstagramUrl().isBlank())
            footerSection.setInstagramUrl(dto.getInstagramUrl());

        if (dto.getTelegramUrl() != null && !dto.getTelegramUrl().isBlank())
            footerSection.setTelegramUrl(dto.getTelegramUrl());

        if (dto.getFacebookUrl() != null && !dto.getFacebookUrl().isBlank())
            footerSection.setFacebookUrl(dto.getFacebookUrl());

        footerSectionRepo.save(footerSection);
    }


}
