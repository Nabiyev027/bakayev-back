package org.example.backend.services.locationService;

import lombok.AllArgsConstructor;
import org.example.backend.entity.LocationSection;
import org.example.backend.repository.LocationSectionRepo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationSectionRepo locationSectionRepo;

    @Override
    public List<LocationSection> getLocations() {
        List<LocationSection> all = locationSectionRepo.findAll();
        return all;
    }

    @Override
    public void addNewLocation(MultipartFile img, String address) {
        LocationSection locationSection = new LocationSection();
        locationSection.setAddress(address);
        String path = createImage(img);
        locationSection.setImgUrl(path);
        locationSectionRepo.save(locationSection);
    }

    @Override
    public void editLocation(UUID id, MultipartFile img, String address) {
        LocationSection locationSection = locationSectionRepo.findById(id).get();
        locationSection.setAddress(address);
        String path = replaceImage(locationSection.getImgUrl(), img);
        locationSection.setImgUrl(path);
        locationSectionRepo.save(locationSection);
    }

    @Override
    public void deleteLocation(UUID id) {
        locationSectionRepo.deleteById(id);
    }


    private String replaceImage(String oldImgUrl, MultipartFile newImg) {
        try {
            // static papkaning to‘liq yo‘lini olish
            File staticFolder = new ClassPathResource("static").getFile();

            // Eski rasmni o‘chirish
            if (oldImgUrl != null && !oldImgUrl.isEmpty()) {
                File oldImageFile = new File(staticFolder.getAbsolutePath() + oldImgUrl);
                if (oldImageFile.exists()) {
                    oldImageFile.delete();
                }
            }

            // Yangi rasmni saqlash
            return createImage(newImg);

        } catch (IOException e) {
            throw new RuntimeException("Rasmni almashtirishda xatolik yuz berdi", e);
        }
    }

    private String createImage(MultipartFile img) {
        try {
            // static/uploads papkasi joylashgan manzilni olish
            File uploadsFolder = new ClassPathResource("static/uploads/").getFile();

            // Agar papka mavjud bo'lmasa - yaratamiz
            if (!uploadsFolder.exists()) {
                uploadsFolder.mkdirs();
            }

            // Unikal fayl nomi yaratamiz
            String uniqueFileName = UUID.randomUUID().toString() + "_" + img.getOriginalFilename();

            // Faylni to'liq yo'liga saqlaymiz
            File destination = new File(uploadsFolder, uniqueFileName);
            img.transferTo(destination);

            // Frontendda ko‘rsatish uchun nisbiy yo‘lni qaytaramiz
            return "/uploads/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Rasmni saqlab bo‘lmadi", e);
        }
    }


}
