package org.example.backend.services.roomService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.DayType;
import org.example.backend.dto.RoomDto;
import org.example.backend.dto.RoomUpdateDto;
import org.example.backend.dtoResponse.GroupRoomInfoResDto;
import org.example.backend.dtoResponse.GroupRoomResDto;
import org.example.backend.dtoResponse.RoomResDto;
import org.example.backend.dtoResponse.TeacherNameDto;
import org.example.backend.entity.Filial;
import org.example.backend.entity.Group;
import org.example.backend.entity.Room;
import org.example.backend.repository.FilialRepo;
import org.example.backend.repository.GroupRepo;
import org.example.backend.repository.RoomRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepo roomRepo;
    private final FilialRepo filialRepo;
    private final GroupRepo groupRepo;

    @Override
    public List<RoomResDto> getRooms() {
        List<Room> all = roomRepo.findAll();
        List<RoomResDto> roomDtos = new ArrayList<>();
        for (Room room : all) {
            RoomResDto roomDto = new RoomResDto();
            roomDto.setId(room.getId());
            roomDto.setName(room.getName());
            roomDto.setNumber(room.getNumber());
            roomDtos.add(roomDto);
        }
        return roomDtos;
    }

    @Transactional
    @Override
    public List<RoomResDto> getRoomsByFilial(UUID filialId) {
        List<RoomResDto> all = new ArrayList<>();
        Filial filial = filialRepo.findById(filialId).get();

        roomRepo.findByFilial(filial).forEach(room -> {
            RoomResDto roomDto = new RoomResDto();
            roomDto.setId(room.getId());
            roomDto.setName(room.getName());
            roomDto.setNumber(room.getNumber());
            all.add(roomDto);
        });

        return all;
    }

    @Override
    @Transactional
    public List<GroupRoomResDto> getRoomsInfo(String filialId, String dayType) {

        List<GroupRoomResDto> all = new ArrayList<>();

        List<Room> rooms;

        if ("all".equalsIgnoreCase(filialId)) {
            rooms = roomRepo.findAll(); // barcha filiallar
        } else {
            Filial filial = filialRepo.findById(UUID.fromString(filialId)).get();
            rooms = roomRepo.findByFilial(filial);
        }

        for (Room room : rooms) {
            GroupRoomResDto roomDto = new GroupRoomResDto();
            roomDto.setId(room.getId());
            roomDto.setRoomName(room.getName());
            roomDto.setRoomNumber(room.getNumber());

            List<GroupRoomInfoResDto> infoResDtos = new ArrayList<>();
            List<Group> groups = groupRepo.findAllByDayTypeAndRoom(
                    DayType.valueOf(dayType.toUpperCase()),
                    room
            );

            groups.forEach(group -> {
                GroupRoomInfoResDto info = new GroupRoomInfoResDto();
                info.setId(group.getId());
                info.setGroupName(group.getName());

                List<TeacherNameDto> teacherNames = new ArrayList<>();
                group.getTeachers().forEach(teacher -> {
                    TeacherNameDto teacherNameDto = new TeacherNameDto();
                    teacherNameDto.setId(teacher.getId());
                    teacherNameDto.setName(teacher.getFirstName()+" "+teacher.getLastName());
                    teacherNames.add(teacherNameDto);
                });

                info.setTeacherNameDtos(teacherNames);

                info.setDayType(group.getDayType().name());
                info.setStartTime(group.getStartTime().toString());
                info.setEndTime(group.getEndTime().toString());
                infoResDtos.add(info);
            });

            roomDto.setGroupRoomInfoResDtos(infoResDtos);
            all.add(roomDto);
        }

        return all;
    }

    @Transactional
    @Override
    public void updateRoomGroupInfo(RoomUpdateDto dto) {

        if (dto.getGroupId() == null) {
            throw new RuntimeException("Group ID is NULL");
        }

        if (dto.getRoomId() == null) {
            throw new RuntimeException("Room ID is NULL");
        }

        Group group = groupRepo.findById(dto.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Room room = roomRepo.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        group.setRoom(room);
        group.setStartTime(LocalTime.parse(dto.getStartTime()));
        group.setEndTime(LocalTime.parse(dto.getEndTime()));

        groupRepo.save(group);
    }


    @Override
    public void createRoom(UUID filialId, RoomDto roomDto) {
        Room room = new Room();
        room.setName(roomDto.getName());
        room.setNumber(roomDto.getNumber());
        Filial filial = filialRepo.findById(filialId).get();
        room.setFilial(filial);
        roomRepo.save(room);
    }

    @Override
    public void updateRoom(UUID id, RoomDto roomDto) {
        roomRepo.findById(id).ifPresent(room -> {
            room.setName(roomDto.getName());
            room.setNumber(roomDto.getNumber());
            roomRepo.save(room);
        });
    }

    @Override
    public void deleteRoom(UUID id) {
        Room room = roomRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        roomRepo.delete(room);
    }

}
