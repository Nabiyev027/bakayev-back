package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.GroupDto;
import org.example.backend.dtoResponse.*;
import org.example.backend.entity.User;
import org.example.backend.services.groupService.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
            List<StudentResDto> students = groupService.getStudents(groupId);
            return  ResponseEntity.ok(students);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getGroupByTeacher(@PathVariable UUID teacherId) {
        try{
            List<GroupsNamesDto> groups = groupService.getGroupsByTeacher(teacherId);
            return  ResponseEntity.ok(groups);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getGroupByStudent(@PathVariable UUID studentId) {
        try{
            List<GroupsNamesDto> groups = groupService.getGroupsByStudent(studentId);
            return  ResponseEntity.ok(groups);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getGroupByFilial(@RequestParam UUID filialId) {
        try{
            List<GroupsNamesDto> groups = groupService.getGroupsByFilial(filialId);
            return  ResponseEntity.ok(groups);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getByFilials")
    public ResponseEntity<?> getGroupsByFilials(
            @RequestParam("ids") String ids
    ) {
        try {
            // ids = "id1,id2,id3"
            List<String> filialIds = Arrays.asList(ids.split(","));

            List<FilialGroupNameResDto> groups = groupService.getGroupsByFilialIds(filialIds);

            return ResponseEntity.ok(groups);

        } catch (Exception e) {
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

    @PostMapping("/add")
    public ResponseEntity<?> postGroup(@RequestBody GroupDto groupDto) {
        try {
            System.out.println(groupDto);
            groupService.createGroup(groupDto);
            return ResponseEntity.ok("Group created successfully");
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editGroup(@PathVariable UUID id, @RequestBody GroupDto groupDto) {
        try {
            groupService.updateGroup(id, groupDto);
            return ResponseEntity.ok("Group updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Xatolik yuz berdi: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable UUID id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.ok("Group deleted successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    

}
