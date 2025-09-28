package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TeacherResDto {
    private UUID id;
    private String imgUrl;
    private String firstName;
    private String lastName;
    private String phone;
    private List<FilialNameDto> branches;
    private List<GroupsNamesDto> groups;
    private String username;

}
