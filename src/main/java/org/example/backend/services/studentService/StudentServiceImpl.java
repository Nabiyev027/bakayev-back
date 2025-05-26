package org.example.backend.services.studentService;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.StudentSection;
import org.example.backend.repository.StudentSectionRepo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentSectionRepo studentSectionRepo;

    @Override
    public void addStudent(MultipartFile img, String name, String listening, String reading, String writing, String speaking, String overall) {
        StudentSection studentSection = new StudentSection();
        String path = createImage(img);
        studentSection.setImgUrl(path);
        studentSection.setName(name);
        studentSection.setListening(listening);
        studentSection.setReading(reading);
        studentSection.setWriting(writing);
        studentSection.setSpeaking(speaking);
        studentSection.setOverall(overall);
        studentSectionRepo.save(studentSection);
    }

    @Override
    public List<StudentSection> getStudentInfo() {
        return studentSectionRepo.findAll();
    }

    @Override
    public void updateStudent(UUID id,MultipartFile img,String name, String listening, String reading, String writing, String speaking, String overall) {
        StudentSection student = studentSectionRepo.findById(id).orElseThrow(() ->
                new RuntimeException("O‘qituvchi topilmadi: " + id));

        if (img != null && !img.isEmpty()) {
            String oldImgUrl = student.getImgUrl(); // Teacher entityda rasm yo‘li bo‘lishi kerak
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

    @Override
    public void deleteStudent(UUID id) {
        Optional<StudentSection> optionalStudent = studentSectionRepo.findById(id);
        if (optionalStudent.isPresent()) {
            StudentSection student = optionalStudent.get();

            // Rasmni o‘chirish
            String imageUrl = student.getImgUrl(); // Faraz qilaylik `imageUrl` degan field bor
            deleteImage(imageUrl);

            // Studentni o‘chirish
            studentSectionRepo.deleteById(id);
        }
    }

    private void deleteImage(String imgUrl) {
        try {
            if (imgUrl != null && !imgUrl.isEmpty()) {
                File staticFolder = new ClassPathResource("static").getFile();
                File imageFile = new File(staticFolder.getAbsolutePath() + imgUrl);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Rasmni o‘chirishda xatolik yuz berdi", e);
        }
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
