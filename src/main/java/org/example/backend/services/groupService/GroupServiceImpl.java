package org.example.backend.services.groupService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.GroupDto;
import org.example.backend.dtoResponse.GroupsNamesDto;
import org.example.backend.dtoResponse.GroupsResDto;
import org.example.backend.dtoResponse.RoomDto;
import org.example.backend.dtoResponse.TeacherNameDto;
import org.example.backend.entity.Filial;
import org.example.backend.entity.Group;
import org.example.backend.entity.Room;
import org.example.backend.entity.User;
import org.example.backend.repository.FilialRepo;
import org.example.backend.repository.GroupRepo;
import org.example.backend.repository.RoomRepo;
import org.example.backend.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final FilialRepo filialRepo;

    @Override
    public List<GroupsResDto> getGroupsWithData() {
        List<GroupsResDto> groupsResDtos = new ArrayList<>();
        List<Group> all = groupRepo.findAll();

        for (Group group : all) {
            GroupsResDto newGroup = new GroupsResDto();
            newGroup.setName(group.getName());
            newGroup.setDegree(group.getDegree());

            Room room = roomRepo.findById(group.getId()).get();
            RoomDto roomDto = new RoomDto();
            roomDto.setId(room.getId());
            roomDto.setName(room.getName());
            roomDto.setNumber(room.getNumber());
            newGroup.setRoomDto(roomDto);

            newGroup.setStartTime(group.getStartTime());
            newGroup.setEndTime(group.getEndTime());

            List<User> students = group.getStudents();
            newGroup.setStudentsNumber(students.size());
            List<TeacherNameDto> teacherNameDtoList = new ArrayList<>();
            List<User> teachers = group.getTeachers();
            for (User teacher : teachers) {
                TeacherNameDto teacherNameDto = new TeacherNameDto();
                teacherNameDto.setId(teacher.getId());
                teacherNameDto.setName(teacher.getFirstName() + " " + teacher.getLastName());
            }
            newGroup.setTeacherNameDtos(teacherNameDtoList);
            Filial filial = group.getFilial();
            newGroup.setFilialName(filial.getName());

            groupsResDtos.add(newGroup);
        }

        return groupsResDtos;
    }

    @Override
    public void createGroup(GroupDto groupDto) {
        Group newGroup = new Group();
        newGroup.setName(groupDto.getName());
        newGroup.setDegree(groupDto.getDegree());
        newGroup.setStartTime(groupDto.getStartTime());
        newGroup.setEndTime(groupDto.getEndTime());

        // Room o‘rnatish
        Room room = roomRepo.findById(groupDto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        newGroup.setRoom(room);

        // Filial o‘rnatish (agar kerak bo‘lsa)
        Filial filial = filialRepo.findById(groupDto.getFilialId())
                .orElseThrow(() -> new RuntimeException("Filial not found"));
        newGroup.setFilial(filial);

        // Teachersni topish va o‘rnatish
        List<User> teachers = groupDto.getTeacherIds().stream()
                .map(id -> userRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Teacher not found: " + id)))
                .collect(Collectors.toList());
        newGroup.setTeachers(teachers);

        groupRepo.save(newGroup);
    }

    @Override
    public void updateGroup(UUID id, GroupDto groupDto) {
        Group group1 = groupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        group1.setName(groupDto.getName());
        group1.setDegree(groupDto.getDegree());
        group1.setStartTime(groupDto.getStartTime());
        group1.setEndTime(groupDto.getEndTime());

        Room room = roomRepo.findById(groupDto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        group1.setRoom(room);

        // Filial o‘rnatish (agar kerak bo‘lsa)
        Filial filial = filialRepo.findById(groupDto.getFilialId())
                .orElseThrow(() -> new RuntimeException("Filial not found"));
        group1.setFilial(filial);

        List<User> teachers = groupDto.getTeacherIds().stream()
                .map(teacherId -> userRepo.findById(teacherId)
                        .orElseThrow(() -> new RuntimeException("Teacher not found: " + teacherId)))
                .collect(Collectors.toList());
        group1.setTeachers(teachers);

        groupRepo.save(group1);
    }

    @Override
    @Transactional
    public List<GroupsNamesDto> getGroupsNames() {
        List<Group> all = groupRepo.findAll();
        List<GroupsNamesDto> groupsNamesDtoList = new ArrayList<>();
        for (Group group : all) {
            GroupsNamesDto groupsNamesDto = new GroupsNamesDto();
            groupsNamesDto.setId(group.getId());
            groupsNamesDto.setName(group.getName());
            groupsNamesDtoList.add(groupsNamesDto);
        }

        return groupsNamesDtoList;
    }

    @Override
    public void deleteGroup(UUID id) {
        groupRepo.deleteById(id);
    }


    @Override
    public List<User> getStudents(UUID groupId) {
        return userRepo.getByGroupId(groupId);
    }

}
