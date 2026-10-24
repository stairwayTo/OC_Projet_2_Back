package com.openclassrooms.etudiant.controller;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.mapper.StudentDtoMapper;
import com.openclassrooms.etudiant.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final StudentDtoMapper studentDtoMapper;

    @PostMapping("/api/students")
    public ResponseEntity<?> create(@Valid @RequestBody StudentRequestDTO studentRequestDTO) {
        studentService.create(studentDtoMapper.toEntity(studentRequestDTO));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/api/students")
    public ResponseEntity<List<StudentResponseDTO>> findAll() {
        List<Student> students = studentService.findAll();
        return ResponseEntity.ok(studentDtoMapper.toResponseDTOList(students));
    }
    @GetMapping("/api/students/{id}")
    public ResponseEntity<StudentResponseDTO> findById(@PathVariable Long id) {
        Student student = studentService.findById(id);
        return ResponseEntity.ok(studentDtoMapper.toResponseDTO(student));
    }
    @DeleteMapping("/api/students/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        studentService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PutMapping("/api/students/{id}")
    public ResponseEntity<StudentResponseDTO> updateById(@PathVariable Long id,
                                                         @Valid @RequestBody StudentRequestDTO studentRequestDTO) {
        Student updatedStudent = studentService.updateById(id, studentDtoMapper.toEntity(studentRequestDTO));
        return ResponseEntity.ok(studentDtoMapper.toResponseDTO(updatedStudent));
    }




}
