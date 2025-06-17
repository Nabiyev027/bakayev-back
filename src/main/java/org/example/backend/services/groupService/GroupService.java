package org.example.backend.services.groupService;

import org.example.backend.dto.GroupDataDto;
import org.example.backend.dto.GroupDto;
import org.example.backend.dtoResponse.GroupsNamesDto;
import org.example.backend.entity.Group;
import org.example.backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface GroupService {
    List<GroupDataDto> getGroupsWithData();

    void deleteGroup(UUID id);

    void createGroup(GroupDto group);

    void updateGroup(UUID id, GroupDto group);

    List<Group> getAllGroups();

    List<User> getStudents(UUID groupId);

    List<GroupsNamesDto> getGroupsNames();
}
