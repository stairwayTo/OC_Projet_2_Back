package com.openclassrooms.etudiant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.service.StudentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class StudentControllerTest {

    private static final String STUDENTS_URL = "/api/students";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String EMAIL = "mail@mail.mail";

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.4");

    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    private void registerStudentInDatabase() {
        Student student = new Student();
        student.setFirstName(FIRST_NAME);
        student.setLastName(LAST_NAME);
        student.setEmail(EMAIL);
        studentService.create(student);
    }

    private StudentRequestDTO buildStudentRequestDTO() {
        StudentRequestDTO studentRequestDTO = new StudentRequestDTO();
        studentRequestDTO.setFirstName(FIRST_NAME);
        studentRequestDTO.setLastName(LAST_NAME);
        studentRequestDTO.setEmail(EMAIL);
        return studentRequestDTO;
    }

    private Long getSavedStudentId() {
        return studentRepository.findByEmail(EMAIL).orElseThrow().getId();
    }

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");

    }

    @AfterEach
    public void afterEach() {
        studentRepository.deleteAll();
    }

    @Test
    void createStudentWithToken_returnsCreatedAndSavesStudent() throws Exception {
        // GIVEN
        StudentRequestDTO studentRequestDTO = buildStudentRequestDTO();

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.post(STUDENTS_URL)
                        .with(jwt())
                        .content(objectMapper.writeValueAsString(studentRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN
                .andExpect(MockMvcResultMatchers.status().isCreated());

        assertThat(studentRepository.findByEmail(EMAIL)).isPresent();
    }

    @Test
    void getAllStudentsWithToken_returnsStudentList() throws Exception {
        // GIVEN
        registerStudentInDatabase();

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get(STUDENTS_URL)
                        .with(jwt()) // faux jwt -> post-processor A REVOIR
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value(EMAIL));
    }

    @Test
    void getStudentByIdWithToken_returnsStudent() throws Exception {
        // GIVEN
        registerStudentInDatabase();
        Long id = getSavedStudentId();

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get(STUDENTS_URL + "/" + id)
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(EMAIL));
    }

    @Test
    void deleteStudentWithToken_returnsNoContentAndRemovesStudent() throws Exception {
        // GIVEN
        registerStudentInDatabase();
        Long id = getSavedStudentId();

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.delete(STUDENTS_URL + "/" + id)
                        .with(jwt()))
                .andDo(print())
                // THEN
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        assertThat(studentRepository.findById(id)).isEmpty();
    }

    @Test
    void updateStudentWithToken_returnsUpdatedStudent() throws Exception {
        // GIVEN
        registerStudentInDatabase();
        Long id = getSavedStudentId();
        StudentRequestDTO studentRequestDTO = buildStudentRequestDTO();
        studentRequestDTO.setFirstName("new_first_name");

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.put(STUDENTS_URL + "/" + id)
                        .with(jwt())
                        .content(objectMapper.writeValueAsString(studentRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("new_first_name"));
    }

    @Test
    void getAllStudentsWithoutToken_returnsUnauthorized() throws Exception {
        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get(STUDENTS_URL)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}