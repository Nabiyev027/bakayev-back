package org.example.backend.services.filialService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.FilialDto;
import org.example.backend.entity.Filial;
import org.example.backend.repository.FilialRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FilialServiceImpl implements FilialService {

    private final FilialRepo filialRepo;

    @Override
    public List<FilialDto> getFilials() {
        List<Filial> all = filialRepo.findAll();
        List<FilialDto> filialDtos = new ArrayList<>();

        all.forEach(filial -> {
            FilialDto filialDto = new FilialDto();
            filialDto.setId(filial.getId());
            filialDto.setName(filial.getName());
            filialDto.setLocation(filial.getLocation());
            filialDto.setDescription(filial.getDescription());
            filialDto.setImageUrl(filial.getImageUrl());
            filialDtos.add(filialDto);
        });

        return filialDtos;
    }

    @Override
    public void createFilial(String name, String description, String location, MultipartFile image) {
        Filial newFilial = new Filial();
        newFilial.setName(name);
        newFilial.setDescription(description);
        newFilial.setLocation(location);
        if (image != null && !image.isEmpty()) {
            String imgPath = createImage(image);
            newFilial.setImageUrl(imgPath);
        }
        filialRepo.save(newFilial);
    }

    @Override
    public void updateFilial(String id, String name, String description, String location, MultipartFile image) {
        Optional<Filial> optionalFilial = filialRepo.findById(UUID.fromString(id));
        if (optionalFilial.isEmpty()) {
            throw new RuntimeException("Filial topilmadi: " + id);
        }

        Filial filial1 = optionalFilial.get();
        filial1.setName(name);
        filial1.setDescription(description);
        filial1.setLocation(location);

        if (image != null && !image.isEmpty()) {
            deleteImageFile(filial1.getImageUrl());
            String imgPath = createImage(image);
            filial1.setImageUrl(imgPath);
        }

        filialRepo.save(filial1);
    }

    @Override
    public void deleteFilial(UUID id) {
        Optional<Filial> optionalFilial = filialRepo.findById(id);
        if (optionalFilial.isEmpty()) {
            throw new RuntimeException("Filial topilmadi: " + id);
        }

        Filial filial1 = optionalFilial.get();
        deleteImageFile(filial1.getImageUrl());

        filialRepo.delete(filial1);
    }

    private String createImage(MultipartFile img) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File uploadsFolder = new File(uploadDir);

            if (!uploadsFolder.exists()) {
                uploadsFolder.mkdirs();
            }

            String uniqueFileName = UUID.randomUUID().toString() + "_" + img.getOriginalFilename();
            File destination = new File(uploadsFolder, uniqueFileName);
            img.transferTo(destination);

            // Agar rasmlar frontend static fayllarida ko‘rsatilsa:
            return "/uploads/" + uniqueFileName;

        } catch (IOException e) {
            e.printStackTrace(); // Konsolda to‘liq xatoni ko‘rsatish uchun
            throw new RuntimeException("Rasmni saqlab bo‘lmadi: " + e.getMessage(), e);
        }
    }

    private void deleteImageFile(String imageUrl) {
        try {
            String path = System.getProperty("user.dir") + imageUrl;
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            System.out.println("Rasmni o‘chirishda xatolik: " + e.getMessage());
        }
    }



}
