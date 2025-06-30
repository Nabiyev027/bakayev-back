package org.example.backend.repository;

import org.example.backend.dtoResponse.GroupsResDto;
import org.example.backend.entity.Filial;
import org.example.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepo extends JpaRepository<Room, UUID> {

    List<Room> findByFilial(Filial filial);
}