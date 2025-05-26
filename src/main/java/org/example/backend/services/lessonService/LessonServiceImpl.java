package org.example.backend.services.lessonService;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Group;
import org.example.backend.entity.Lesson;
import org.example.backend.repository.GroupRepo;
import org.example.backend.repository.LessonRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService{

    private final GroupRepo groupRepo;
    private final LessonRepo lessonRepo;

    @Override
    public List<Lesson> getLessons(UUID groupId) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        return lessonRepo.getByGroup(group);
    }

    @Override
    public void postLesson(UUID groupId, String lessonTypes) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

            Lesson lesson1 = new Lesson();
            lesson1.setType(lessonTypes);
            lesson1.setGroup(group);
            lessonRepo.save(lesson1);

    }

    @Override
    public void editLesson(UUID lessonId, String lessonType) {
        Lesson lesson = lessonRepo.findById(lessonId).get();
        lesson.setType(lessonType);
        lessonRepo.save(lesson);
    }

    @Override
    public void deletelesson(UUID id) {
        lessonRepo.deleteById(id);
    }

}
