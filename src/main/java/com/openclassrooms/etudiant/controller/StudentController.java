package com.openclassrooms.etudiant.controller;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.mapper.StudentDtoMapper;
import com.openclassrooms.etudiant.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    //@GetMapping("/api/students")
    //@GetMapping("/api/students/{id}")
    //@DeleteMapping("/api/students/{id}")
    //@PutMapping("/api/students/{id}")




}
