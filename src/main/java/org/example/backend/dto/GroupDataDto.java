package org.example.backend.dto;

import lombok.Data;
import org.example.backend.entity.User;

import java.util.List;

@Data
public class GroupDataDto {
    private String name;
    private String degree;
    private String lessonTime;
    private String roomNumName;
    private Integer studentNumber=0;
    private List<User> teachers;

}
