package org.example.backend.services.filialService;

import org.example.backend.dtoResponse.FilialDto;
import org.example.backend.entity.Filial;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FilialService {
    List<FilialDto> getFilials();

    void createFilial(String name, String description, String location, MultipartFile image);

    void updateFilial(String id,String name, String description, String location, MultipartFile image);

    void deleteFilial(UUID id);
}
