package org.example.backend.services.studentService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.StudentSectionResDto;
import org.example.backend.entity.StudentSection;
import org.example.backend.repository.StudentSectionRepo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentSectionRepo studentSectionRepo;

    @Override
    public void addStudent(MultipartFile img, String name, Double listening, Double reading, Double writing, Double speaking, Double overall) {
        StudentSection studentSection = new StudentSection();
        if(img != null && !img.isEmpty() ) {
            String path = createImage(img);
            studentSection.setImgUrl(path);
        }
        studentSection.setName(name);
        studentSection.setListening(listening);
        studentSection.setReading(reading);
        studentSection.setWriting(writing);
        studentSection.setSpeaking(speaking);
        studentSection.setOverall(overall);
        studentSectionRepo.save(studentSection);
    }

    @Transactional
    @Override
    public List<StudentSectionResDto> getStudentInfo() {
        List<StudentSection> all = studentSectionRepo.findAll();
        List<StudentSectionResDto> studentSectionResDtos = new ArrayList<>();

        all.forEach(student -> {
            StudentSectionResDto studentSection = new StudentSectionResDto();
            studentSection.setId(student.getId());
            studentSection.setImgUrl(student.getImgUrl());
            studentSection.setName(student.getName());
            studentSection.setListening(student.getListening());
            studentSection.setReading(student.getReading());
            studentSection.setWriting(student.getWriting());
            studentSection.setSpeaking(student.getSpeaking());
            studentSection.setOverall(student.getOverall());
            studentSectionResDtos.add(studentSection);
        });

        return studentSectionResDtos;
    }

    @Transactional
    @Override
    public void updateStudent(UUID id, MultipartFile img,String name, Double listening, Double reading, Double writing, Double speaking, Double overall) {
        StudentSection student = studentSectionRepo.findById(id).orElseThrow(() ->
                new RuntimeException("Student topilmadi: " + id));

        if (img != null && !img.isEmpty()) {
            String oldImgUrl = student.getImgUrl();
            String newImgUrl = replaceImage(oldImgUrl, img);
            student.setImgUrl(newImgUrl);
        }

        student.setName(name);
        student.setListening(listening);
        student.setReading(reading);
        student.setWriting(writing);
        student.setSpeaking(speaking);
        student.setOverall(overall);
        studentSectionRepo.save(student);

    }

    @Transactional
    @Override
    public void deleteStudent(UUID id) {
        StudentSection studentSection = studentSectionRepo.findById(id)
                .orElseThrow(()-> new RuntimeException("Student topilmadi: " + id));
        deleteImage(studentSection.getImgUrl());
        studentSectionRepo.deleteById(studentSection.getId());

    }

    private String replaceImage(String oldImgUrl, MultipartFile newImg) {
        Optional.ofNullable(oldImgUrl)
                .filter(url -> !url.isEmpty())
                .map(url -> url.substring(url.lastIndexOf("/") + 1))
                .map(fileName -> Paths.get(System.getProperty("user.dir"), "uploads", fileName))
                .ifPresent(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        System.err.println("Eski rasmni o‘chirishda xatolik: " + e.getMessage());
                    }
                });

        return createImage(newImg);
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

    public void deleteImage(String imgUrl) {
        if (imgUrl == null || imgUrl.isBlank()) return;

        try {
            // uploads papkaga yo‘l
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File imageFile = new File(uploadDir + imgUrl.replace("/uploads", ""));

            if (imageFile.exists()) {
                boolean deleted = imageFile.delete();
                if (!deleted) {
                    System.err.println("❌ Rasmni o‘chirish muvaffaqiyatsiz: " + imageFile.getAbsolutePath());
                }
            } else {
                System.err.println("⚠️ Rasm topilmadi: " + imageFile.getAbsolutePath());
            }

        } catch (Exception e) {
            throw new RuntimeException("❌ Rasmni o‘chirishda xatolik: " + e.getMessage());
        }
    }


}
