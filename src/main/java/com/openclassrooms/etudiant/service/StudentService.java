package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public void create(Student student) {
        Optional<Student> optionalStudent = studentRepository.findByEmail(student.getEmail());
        if(optionalStudent.isPresent()){
            throw new IllegalArgumentException("Student with " + student.getEmail() + " already exists");
        }
        studentRepository.save(student);
    }
}
