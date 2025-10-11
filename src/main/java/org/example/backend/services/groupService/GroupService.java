package org.example.backend.services.groupService;

import org.example.backend.dto.GroupDto;
import org.example.backend.dtoResponse.GroupsNamesDto;
import org.example.backend.dtoResponse.GroupsResDto;
import org.example.backend.dtoResponse.StudentProjection;
import org.example.backend.dtoResponse.StudentResDto;
import org.example.backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface GroupService {
    List<GroupsResDto> getGroupsWithData();

    void deleteGroup(UUID id);

    void createGroup(GroupDto groupDto);

    void updateGroup(UUID id, GroupDto groupDto);

    List<StudentResDto> getStudents(UUID groupId);

    List<GroupsNamesDto> getGroupsNames();

    List<GroupsNamesDto> getGroupsByFilial(UUID filialId);

    List<GroupsNamesDto> getGroupsByTeacher(UUID teacherId);

    List<GroupsNamesDto> getGroupsByStudent(UUID studentId);
}
