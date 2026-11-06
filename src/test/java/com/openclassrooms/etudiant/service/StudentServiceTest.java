package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    private static final Long ID = 1L;
    private static final String FIRST_NAME = "toto";
    private static final String LAST_NAME = "tutu";
    private static final String EMAIL = "EMAIL";


    @Mock
    private StudentRepository studentRepository;
    @InjectMocks
    private StudentService studentService;

    private Student buildStudent(){
        Student student = new Student();
        student.setId(ID);
        student.setFirstName(FIRST_NAME);
        student.setLastName(LAST_NAME);
        student.setEmail(EMAIL);
        return student;
    }
    private Student buildNewStudent(){
        Student newStudent = new Student();
        newStudent.setFirstName("Fname");
        newStudent.setLastName("Lname");
        newStudent.setEmail("EMAIL@EMAIL.EMAIL");
        return newStudent;
    }


    @Test
    void create_email_already_exist_throws_IllegalArgumentException() {
        //given
        Student student = buildStudent();
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        //when
        //then
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> studentService.create(student));
    }
    @Test
    void create_happy_path() {
        //given
        Student student = buildStudent();
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(Optional.empty());
        //when
        studentService.create(student);
        //then
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());
        assertThat(studentCaptor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(studentCaptor.getValue()).isEqualTo(student);
    }

    //Ce test est useless ? il apel juste un repo ....
    @Test
    void findAll_happy_path(){
        //given
        Student student1 = buildStudent();
        Student student2 = buildNewStudent();
        List<Student> students = List.of(student1, student2);
        when(studentRepository.findAll()).thenReturn(students);
        //when
        List<Student> result = studentService.findAll();
        //then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(student1, student2);
    }

    @Test
    void findById_id_not_found_throw_IllegalArgumentException() {
        //given
        when(studentRepository.findById(ID)).thenReturn(Optional.empty());
        //when
        //then
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> studentService.findById(ID));
    }

    @Test
    void findById_happy_path(){
        //given
        Student student = buildStudent();
        when(studentRepository.findById(ID)).thenReturn(Optional.of(student));
        //when
        Student foundStudent = studentService.findById(ID);
        //then
        assertThat(foundStudent.getId()).isEqualTo(ID);
        assertThat(foundStudent).isEqualTo(student);
    }

    @Test
    void deleteById_id_not_found_throw_IllegalArgumentException() {
        //given
        when(studentRepository.findById(ID)).thenReturn(Optional.empty());
        //when
        //then
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> studentService.deleteById(ID));
        verify(studentRepository, never()).deleteById(any());
    }

    @Test
    void deleteById_happy_path() {
        //given
        Student student = buildStudent();
        when(studentRepository.findById(ID)).thenReturn(Optional.of(student));
        //when
        studentService.deleteById(ID);
        //then
        verify(studentRepository).deleteById(ID);
    }

    @Test
    void updateById_id_not_found_throw_IllegalArgumentException() {
        //given
        when(studentRepository.findById(ID)).thenReturn(Optional.empty());
        Student updatedStudent = buildNewStudent();
        //when

        //then
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> studentService.updateById(ID, updatedStudent));
        verify(studentRepository, never()).save(any());
    }
    @Test
    void updateById_happy_path() {
        //given
        Student existingStudent = buildStudent();
        Student updatedStudent = buildNewStudent();
        when(studentRepository.findById(ID)).thenReturn(Optional.of(existingStudent));

        //when
        studentService.updateById(ID, updatedStudent);

        //then
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());
        assertThat(studentCaptor.getValue().getId()).isEqualTo(ID);
        assertThat(studentCaptor.getValue().getFirstName()).isEqualTo(updatedStudent.getFirstName());
        assertThat(studentCaptor.getValue().getLastName()).isEqualTo(updatedStudent.getLastName());
        assertThat(studentCaptor.getValue().getEmail()).isEqualTo(updatedStudent.getEmail());

    }
}