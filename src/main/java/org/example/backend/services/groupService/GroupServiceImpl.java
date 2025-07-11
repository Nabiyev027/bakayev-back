package org.example.backend.services.groupService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.GroupDto;
import org.example.backend.dtoResponse.*;
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
import java.util.Optional;
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
            newGroup.setId(group.getId());
            newGroup.setName(group.getName());
            newGroup.setDegree(group.getDegree());
            Room room = roomRepo.findById(group.getRoom().getId()).orElse(null);
            RoomResDto roomDto = new RoomResDto();
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
                teacherNameDtoList.add(teacherNameDto);
            }
            newGroup.setTeacherNameDtos(teacherNameDtoList);

            FilialNameDto filialNameDto = new FilialNameDto();
            filialNameDto.setId(group.getFilial().getId());
            filialNameDto.setName(group.getFilial().getName());

            newGroup.setFilialNameDto(filialNameDto);

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
    @Transactional // bitta tranzaksiyada hammasi saqlansin
    public void updateGroup(UUID id, GroupDto dto) {

        // --- 1. Guruhni olib kelamiz
        Group group = groupRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found: " + id));

        // --- 2. Majburiy maydonlar mavjudligini tekshiramiz
        if (dto.getRoomId() == null)        throw new IllegalArgumentException("roomId bo'sh bo'lishi mumkin emas");
        if (dto.getFilialId() == null)      throw new IllegalArgumentException("filialId bo'sh bo'lishi mumkin emas");
        if (dto.getName() == null || dto.getName().isBlank())
            throw new IllegalArgumentException("Guruh nomi bo'sh bo'lishi mumkin emas");
        if (dto.getDegree() == null || dto.getDegree().isBlank())
            throw new IllegalArgumentException("Degree bo'sh bo'lishi mumkin emas");

        // --- 3. Room va Filialni tekshirib, o‘rnatamiz
        Room room = roomRepo.findById(dto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + dto.getRoomId()));
        group.setRoom(room);

        Filial filial = filialRepo.findById(dto.getFilialId())
                .orElseThrow(() -> new EntityNotFoundException("Filial not found: " + dto.getFilialId()));
        group.setFilial(filial);

        // --- 4. Guruh maydonlarini yangilaymiz
        group.setName(dto.getName());
        group.setDegree(dto.getDegree());
        group.setStartTime(dto.getStartTime());
        group.setEndTime(dto.getEndTime());

        // --- 5. Teacherlar ro‘yxatini yig‘amiz
        List<UUID> teacherIds = Optional.ofNullable(dto.getTeacherIds()).orElse(List.of());
        List<User> teachers = new ArrayList<>(teacherIds.stream()
                .map(tid -> userRepo.findById(tid)
                        .orElseThrow(() -> new EntityNotFoundException("Teacher not found: " + tid)))
                .toList()); // ➜ new ArrayList<>(...) orqali mutable holga o‘tkazildi

        group.setTeachers(teachers);


        // --- 6. Saqlaymiz
        groupRepo.save(group);
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
    @Transactional
    public void deleteGroup(UUID id) {
        Group group = groupRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found: " + id));

        // Many-to-many bog'lovchilarni tozalaymiz
        group.getTeachers().clear();
        group.getStudents().clear();

        // Lesson larni o'chirish uchun lessons ro'yxatini bo'shatish shart emas,
        // chunki CascadeType.ALL bu ishni o'zi qiladi.

        groupRepo.delete(group); // endi xotirjam o'chsa bo'ladi
    }


    @Override
    public List<User> getStudents(UUID groupId) {
        return userRepo.getByGroupId(groupId);
    }

}
