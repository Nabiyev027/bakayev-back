package org.example.backend.services.groupService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.DayType;
import org.example.backend.dto.GroupDto;
import org.example.backend.dtoResponse.*;
import org.example.backend.entity.*;
import org.example.backend.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService{
    private final GroupRepo groupRepo;
    private final RoomRepo roomRepo;
    private final UserRepo userRepo;
    private final FilialRepo filialRepo;
    private final RoleRepo roleRepo;
    private final GroupStudentRepo groupStudentRepo;
    private final AttendanceRepo attendanceRepo;
    private final TeacherSalaryRepo teacherSalaryRepo;

    @Transactional(readOnly = true)
    @Override
    public List<GroupsResDto> getGroupsWithData(String filialId) {

        List<Group> groups;
        if (filialId == null || filialId.trim().isEmpty() || filialId.equalsIgnoreCase("all")) {
            groups = groupRepo.findAll(Sort.by(Sort.Direction.ASC, "name")); // name bo'yicha tartib
        } else {
            Filial filial = filialRepo.findById(UUID.fromString(filialId))
                    .orElseThrow(() -> new RuntimeException("Filial not found"));

            groups = groupRepo.getGroupByFilial(filial, Sort.by(Sort.Direction.ASC, "name")); // agar getGroupByFilial Sort qabul qilsa
        }


        return groups.stream().map(group -> {
            GroupsResDto dto = new GroupsResDto();
            dto.setId(group.getId());
            dto.setName(group.getName());
            dto.setDegree(group.getDegree());
            dto.setDayType(group.getDayType().toString());
            dto.setStartTime(group.getStartTime());
            dto.setEndTime(group.getEndTime());

            if (group.getRoom() != null) {
                RoomResDto roomDto = new RoomResDto();
                roomDto.setId(group.getRoom().getId());
                roomDto.setName(group.getRoom().getName());
                roomDto.setNumber(group.getRoom().getNumber());
                dto.setRoomDto(roomDto);
            }

            dto.setStudentsNumber(
                    group.getGroupStudents() != null ? group.getGroupStudents().size() : 0
            );

            List<TeacherNameDto> teacherDtos = group.getTeachers().stream()
                    .map(teacher -> {
                        TeacherNameDto tDto = new TeacherNameDto();
                        tDto.setId(teacher.getId());
                        tDto.setName(teacher.getFirstName() + " " + teacher.getLastName());
                        return tDto;
                    }).toList();

            dto.setTeacherNameDtos(teacherDtos);

            if (group.getFilial() != null) {
                FilialNameDto filialDto = new FilialNameDto();
                filialDto.setId(group.getFilial().getId());
                filialDto.setName(group.getFilial().getName());
                dto.setFilialNameDto(filialDto);
            }

            return dto;
        }).toList();
    }


    @Override
    public void createGroup(GroupDto groupDto) {
        Group newGroup = new Group();
        newGroup.setName(groupDto.getName());
        newGroup.setDegree(groupDto.getDegree());
        newGroup.setStartTime(groupDto.getStartTime());
        newGroup.setEndTime(groupDto.getEndTime());

        if(!groupDto.getDayType().equals("")) {
            newGroup.setDayType(DayType.valueOf(groupDto.getDayType()));
        }else {
            newGroup.setDayType(DayType.ALL);
        }

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

        Group savedG = groupRepo.save(newGroup);


        savedG.getTeachers().forEach(teacher -> {
            TeacherSalary teacherSalary = new TeacherSalary();
            teacherSalary.setGroup(savedG);
            teacherSalary.setTeacher(teacher);
            teacherSalary.setSalaryDate(LocalDate.now());
            teacherSalary.setPercentage(0);
            teacherSalary.setTotalAmount(0);
            teacherSalaryRepo.save(teacherSalary);
        });

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

        if(!dto.getDayType().equals("")) {
            group.setDayType(DayType.valueOf(dto.getDayType()));
        }else {
            group.setDayType(DayType.ALL);
        }

        group.setDayType(DayType.valueOf(dto.getDayType()));

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
    public List<GroupsNamesDto> getGroupsByFilial(UUID filialId) {
        List<GroupsNamesDto> groups = new ArrayList<>();

        Filial filial = filialRepo.findById(filialId).get();

        List<Group> groupByFilial = groupRepo.getGroupByFilial(filial, Sort.by(Sort.Direction.ASC, "name"));

        groupByFilial.forEach(group -> {
            GroupsNamesDto groupsNamesDto = new GroupsNamesDto();
            groupsNamesDto.setId(group.getId());
            groupsNamesDto.setName(group.getName());
            groups.add(groupsNamesDto);
        });

        return groups;
    }

    @Transactional
    @Override
    public List<GroupsNamesDto> getGroupsByTeacher(UUID teacherId) {
        User t = userRepo.findById(teacherId) // custom query ishlatsa yaxshi
                .orElseThrow(() -> new UsernameNotFoundException("Foydalanuvchi topilmadi: " + teacherId));

        System.out.println(t);


        List<GroupsNamesDto> groups = new ArrayList<>();

        List<Group> groupsByTeachers = groupRepo.getGroupsByTeacher(t.getId());

        System.out.println("groupsByTeachers: " + groupsByTeachers);

        groupsByTeachers.forEach(group -> {
            GroupsNamesDto groupsNamesDto = new GroupsNamesDto();
            groupsNamesDto.setId(group.getId());
            groupsNamesDto.setName(group.getName());
            groups.add(groupsNamesDto);
        });

        return groups;
    }

    @Override
    public List<GroupsNamesDto> getGroupsByStudent(UUID studentId) {
        User s = userRepo.findByIdWithGroups(studentId) // custom query ishlatsa yaxshi
                .orElseThrow(() -> new UsernameNotFoundException("Foydalanuvchi topilmadi: " + studentId));

        List<GroupsNamesDto> groups = new ArrayList<>();

        List<Group> groupsByStudent = groupRepo.getGroupsByStudent(s.getId());

        groupsByStudent.forEach(group -> {
            GroupsNamesDto groupsNamesDto = new GroupsNamesDto();
            groupsNamesDto.setId(group.getId());
            groupsNamesDto.setName(group.getName());
            groups.add(groupsNamesDto);
        });

        return groups;
    }


    @Override
    public List<FilialGroupNameResDto> getGroupsByFilialIds(List<String> filialIds) {

        // Agar hech narsaga kirmasa bo'sh qaytaramiz
        if (filialIds == null || filialIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Repo orqali bazadan filiallarga tegishli gruppalarni olish
        List<Group> groups = groupRepo.findByFilialIdIn(filialIds);

        List<FilialGroupNameResDto> filialGroups = new ArrayList<>();

        groups.forEach(g -> {
            FilialGroupNameResDto dto = new FilialGroupNameResDto();
            dto.setId(g.getId().toString());
            dto.setName(g.getName());
            dto.setFilialId(g.getFilial().getId().toString());
            dto.setFilialName(g.getFilial().getName());
            filialGroups.add(dto);
        });

        return filialGroups;
    }



    @Override
    @Transactional
    public void deleteGroup(UUID id) {
        Group group = groupRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found: " + id));

        // Teacherlarni xotiradan tozalash
        group.getTeachers().clear();

        attendanceRepo.deleteAttendancesByGroup(group);


        // Studentlarni GroupStudent orqali o'chirish
        if (group.getGroupStudents() != null) {
            for (GroupStudent gs : group.getGroupStudents()) {
                groupStudentRepo.delete(gs); // DBdan o'chirish
            }
            group.getGroupStudents().clear(); // xotirada ham tozalash
        }

        groupRepo.delete(group); // endi xavfsiz o'chadi
    }


    @Override
    @Transactional
    public List<StudentResDto> getStudents(UUID groupId) {
        Role roleStudent = roleRepo.findByName("ROLE_STUDENT").orElseThrow();
        Group group = groupRepo.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Group not found: " + groupId));
        List<StudentProjection> usersByGroupAndRole = userRepo.findUsersByGroupAndRole(group, roleStudent);
        List<StudentResDto> studentResDtoList = new ArrayList<>();
        for (StudentProjection studentProjection : usersByGroupAndRole) {
            StudentResDto studentResDto = new StudentResDto(studentProjection.getFirstName(),studentProjection.getLastName(),studentProjection.getPhone(),studentProjection.getId());
            studentResDtoList.add(studentResDto);
        }
        return studentResDtoList;
    }

}
