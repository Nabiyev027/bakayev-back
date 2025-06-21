package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.GroupDto;
import org.example.backend.dtoResponse.GroupsNamesDto;
import org.example.backend.dtoResponse.GroupsResDto;
import org.example.backend.entity.User;
import org.example.backend.services.groupService.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
@CrossOrigin
public class GroupController {
    private final GroupService groupService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllGroups() {
        try {
            List<GroupsResDto> groups =  groupService.getGroupsWithData();
            return  ResponseEntity.ok(groups);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getStudentsByGroup(@PathVariable UUID groupId) {
        try{
            List<User> students = groupService.getStudents(groupId);
            return  ResponseEntity.ok(students);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getNames")
    public ResponseEntity<?> getGroupsNames() {
        try {
            List<GroupsNamesDto> groups = groupService.getGroupsNames();
            return  ResponseEntity.ok(groups);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> postGroup(@RequestBody GroupDto groupDto) {
        try {
            groupService.createGroup(groupDto);
            return ResponseEntity.ok("Group created successfully");
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editGroup(@PathVariable UUID id, @RequestBody GroupDto groupDto) {
        try {
            groupService.updateGroup(id,groupDto);
            return ResponseEntity.ok("Group updated successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable UUID id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.ok("Group deleted successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
