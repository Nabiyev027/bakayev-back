package org.example.backend.services.groupService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.GroupDataDto;
import org.example.backend.dto.GroupDto;
import org.example.backend.entity.Group;
import org.example.backend.entity.Room;
import org.example.backend.entity.User;
import org.example.backend.repository.GroupRepo;
import org.example.backend.repository.RoomRepo;
import org.example.backend.repository.UserRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService{
    private final GroupRepo groupRepo;
    private final RoomRepo roomRepo;
    private final UserRepo userRepo;

    @Override
    public List<GroupDataDto> getGroupsWithData() {
        List<GroupDataDto> groupDataDtoList = new ArrayList<>();
        List<Group> all = groupRepo.findAll();

        for (Group group : all) {
            GroupDataDto groupDataDto = new GroupDataDto();
            groupDataDto.setName(group.getName());
            groupDataDto.setDegree(group.getDegree());
            groupDataDto.setRoomNumName(group.getRoom().getNumber() + " " + group.getRoom().getName());
            groupDataDto.setLessonTime(group.getStartTime() + "-" + group.getEndTime());

            List<User> students = group.getStudents();
            groupDataDto.setStudentNumber(students.size());

            // Teacherlarni o‘rnatish
            groupDataDto.setTeachers(group.getTeachers());

            groupDataDtoList.add(groupDataDto);
        }

        return groupDataDtoList;
    }

    @Override
    public void createGroup(GroupDto group) {
        Group newGroup = new Group();
        newGroup.setName(group.getName());
        newGroup.setDegree(group.getDegree());
        newGroup.setStartTime(group.getStartTime());
        newGroup.setEndTime(group.getEndTime());

        // Room o‘rnatish
        Room room = roomRepo.findById(group.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        newGroup.setRoom(room);

        // Teachersni topish va o‘rnatish
        List<User> teachers = group.getTeachersId().stream()
                .map(id -> userRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Teacher not found: " + id)))
                .collect(Collectors.toList());
        newGroup.setTeachers(teachers);

        groupRepo.save(newGroup);
    }

    @Override
    public void updateGroup(UUID id, GroupDto group) {
        Group group1 = groupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        group1.setName(group.getName());
        group1.setDegree(group.getDegree());
        group1.setStartTime(group.getStartTime());
        group1.setEndTime(group.getEndTime());

        Room room = roomRepo.findById(group.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        group1.setRoom(room);

        List<User> teachers = group.getTeachersId().stream()
                .map(teacherId -> userRepo.findById(teacherId)
                        .orElseThrow(() -> new RuntimeException("Teacher not found: " + teacherId)))
                .collect(Collectors.toList());
        group1.setTeachers(teachers);

        groupRepo.save(group1);
    }

    @Override
    public void deleteGroup(UUID id) {
        groupRepo.deleteById(id);
    }

    @Override
    public List<Group> getAllGroups() {
        return groupRepo.findAll();
    }

    @Override
    public List<User> getStudents(UUID groupId) {
        return userRepo.getByGroupId(groupId);
    }

}
