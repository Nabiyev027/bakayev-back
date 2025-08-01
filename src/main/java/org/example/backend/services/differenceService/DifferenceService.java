package org.example.backend.services.differenceService;

import org.example.backend.dtoResponse.DifferenceResDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DifferenceService {
    void createDifference(MultipartFile img,
                          String titleUz, String descriptionUz,
                          String titleRu, String descriptionRu,
                          String titleEn, String descriptionEn);

    void deleteRef(UUID id);

    void editDif(UUID id, MultipartFile img,
                 String oldImgUrl, String titleUz, String descriptionUz,
                 String titleRu, String descriptionRu,
                 String titleEn, String descriptionEn);

    List<DifferenceResDto> getDifference();
}
