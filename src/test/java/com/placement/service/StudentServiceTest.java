package com.placement.service;

import com.placement.config.StorageConfig;
import com.placement.dto.StudentDto;
import com.placement.exception.DuplicateResourceException;
import com.placement.model.Student;
import com.placement.repository.JsonStorageEngine;
import com.placement.repository.RegistrationRepository;
import com.placement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentServiceTest {

    @TempDir
    Path tempDir;

    private StudentRepository studentRepository;
    private RegistrationRepository registrationRepository;
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        StorageConfig storageConfig = new StorageConfig();
        ReflectionTestUtils.setField(storageConfig, "dataDirConfig", tempDir.resolve("data").toString());
        ReflectionTestUtils.setField(storageConfig, "backupDirConfig", tempDir.resolve("backups").toString());
        ReflectionTestUtils.setField(storageConfig, "reportDirConfig", tempDir.resolve("reports").toString());
        storageConfig.init();

        JsonStorageEngine engine = new JsonStorageEngine(storageConfig);
        studentRepository = new StudentRepository(engine);
        registrationRepository = new RegistrationRepository(engine);
        studentService = new StudentService(studentRepository, registrationRepository);
    }

    @Test
    @DisplayName("Should successfully create student with valid details")
    void testCreateStudentSuccess() {
        StudentDto dto = new StudentDto();
        dto.setRollNumber("21CS099");
        dto.setFullName("David Miller");
        dto.setBranch("CSE");
        dto.setCgpa(8.9);
        dto.setSkills(List.of("Java", "Spring Boot"));
        dto.setEmail("david@college.edu");
        dto.setBacklogs(0);

        Student created = studentService.createStudent(dto);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("21CS099", created.getRollNumber());
        assertEquals("David Miller", created.getFullName());
        assertEquals(1, studentRepository.findAll().size());
    }

    @Test
    @DisplayName("Should reject duplicate roll numbers when creating student")
    void testDuplicateRollNumberRejected() {
        StudentDto dto1 = new StudentDto();
        dto1.setRollNumber("21CS001");
        dto1.setFullName("Original Person");
        dto1.setBranch("CSE");
        dto1.setCgpa(8.0);
        studentService.createStudent(dto1);

        StudentDto dto2 = new StudentDto();
        dto2.setRollNumber("21CS001");
        dto2.setFullName("Duplicate Person");
        dto2.setBranch("CSE");
        dto2.setCgpa(7.5);

        assertThrows(DuplicateResourceException.class, () -> {
            studentService.createStudent(dto2);
        });

        assertEquals(1, studentRepository.findAll().size());
    }

    @Test
    @DisplayName("Should search and filter students correctly")
    void testSearchAndFilterStudents() {
        StudentDto s1 = new StudentDto();
        s1.setRollNumber("21CS101");
        s1.setFullName("Alice Bob");
        s1.setBranch("CSE");
        s1.setCgpa(9.2);
        s1.setSkills(List.of("Java", "AWS"));
        studentService.createStudent(s1);

        StudentDto s2 = new StudentDto();
        s2.setRollNumber("21EC102");
        s2.setFullName("Charlie Brown");
        s2.setBranch("ECE");
        s2.setCgpa(7.8);
        s2.setSkills(List.of("Python"));
        studentService.createStudent(s2);

        // Filter by branch
        List<Student> cseList = studentService.getAllStudents(null, "CSE", null, null, "name", "asc");
        assertEquals(1, cseList.size());
        assertEquals("21CS101", cseList.get(0).getRollNumber());

        // Filter by min CGPA
        List<Student> highCgpa = studentService.getAllStudents(null, null, 8.0, null, "name", "asc");
        assertEquals(1, highCgpa.size());
        assertEquals("Alice Bob", highCgpa.get(0).getFullName());

        // Search by name
        List<Student> searchResults = studentService.getAllStudents("Charlie", null, null, null, "name", "asc");
        assertEquals(1, searchResults.size());
        assertEquals("21EC102", searchResults.get(0).getRollNumber());
    }
}
