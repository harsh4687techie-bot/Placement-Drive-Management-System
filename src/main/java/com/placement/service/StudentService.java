package com.placement.service;

import com.placement.dto.StudentDto;
import com.placement.exception.DuplicateResourceException;
import com.placement.exception.ResourceNotFoundException;
import com.placement.model.Student;
import com.placement.repository.RegistrationRepository;
import com.placement.repository.StudentRepository;
import com.placement.util.DateTimeUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;

    public StudentService(StudentRepository studentRepository, RegistrationRepository registrationRepository) {
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
    }

    public List<Student> getAllStudents(String search, String branch, Double minCgpa, String skill, String sortBy, String sortOrder) {
        List<Student> students = studentRepository.findAll();

        return students.stream()
                .filter(s -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    boolean nameMatch = s.getFullName() != null && s.getFullName().toLowerCase().contains(q);
                    boolean rollMatch = s.getRollNumber() != null && s.getRollNumber().toLowerCase().contains(q);
                    return nameMatch || rollMatch;
                })
                .filter(s -> {
                    if (branch == null || branch.isBlank() || branch.equalsIgnoreCase("ALL")) return true;
                    return s.getBranch() != null && s.getBranch().equalsIgnoreCase(branch.trim());
                })
                .filter(s -> {
                    if (minCgpa == null) return true;
                    return s.getCgpa() != null && s.getCgpa() >= minCgpa;
                })
                .filter(s -> {
                    if (skill == null || skill.isBlank()) return true;
                    String targetSkill = skill.trim().toLowerCase();
                    return s.getSkills() != null && s.getSkills().stream()
                            .anyMatch(sk -> sk != null && sk.trim().toLowerCase().contains(targetSkill));
                })
                .sorted((s1, s2) -> {
                    int result = 0;
                    if ("cgpa".equalsIgnoreCase(sortBy)) {
                        Double c1 = s1.getCgpa() != null ? s1.getCgpa() : 0.0;
                        Double c2 = s2.getCgpa() != null ? s2.getCgpa() : 0.0;
                        result = c1.compareTo(c2);
                    } else if ("rollNumber".equalsIgnoreCase(sortBy)) {
                        String r1 = s1.getRollNumber() != null ? s1.getRollNumber() : "";
                        String r2 = s2.getRollNumber() != null ? s2.getRollNumber() : "";
                        result = r1.compareToIgnoreCase(r2);
                    } else { // default sort by name
                        String n1 = s1.getFullName() != null ? s1.getFullName() : "";
                        String n2 = s2.getFullName() != null ? s2.getFullName() : "";
                        result = n1.compareToIgnoreCase(n2);
                    }
                    return "desc".equalsIgnoreCase(sortOrder) ? -result : result;
                })
                .collect(Collectors.toList());
    }

    public Student getStudentById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
    }

    public synchronized Student createStudent(StudentDto dto) {
        if (studentRepository.existsByRollNumber(dto.getRollNumber())) {
            throw new DuplicateResourceException("A student with roll number '" + dto.getRollNumber().trim() + "' already exists.");
        }

        Student student = new Student();
        student.setRollNumber(dto.getRollNumber().trim().toUpperCase());
        student.setFullName(dto.getFullName().trim());
        student.setBranch(dto.getBranch().trim().toUpperCase());
        student.setCgpa(dto.getCgpa());
        student.setSkills(dto.getSkills() != null ? dto.getSkills() : List.of());
        student.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        student.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        student.setBacklogs(dto.getBacklogs() != null ? dto.getBacklogs() : 0);
        student.setCreatedAt(DateTimeUtil.nowIso());
        student.setUpdatedAt(DateTimeUtil.nowIso());

        return studentRepository.save(student);
    }

    public synchronized Student updateStudent(String id, StudentDto dto) {
        Student existing = getStudentById(id);

        if (studentRepository.existsByRollNumberAndIdNot(dto.getRollNumber(), id)) {
            throw new DuplicateResourceException("Roll number '" + dto.getRollNumber().trim() + "' is already assigned to another student.");
        }

        existing.setRollNumber(dto.getRollNumber().trim().toUpperCase());
        existing.setFullName(dto.getFullName().trim());
        existing.setBranch(dto.getBranch().trim().toUpperCase());
        existing.setCgpa(dto.getCgpa());
        existing.setSkills(dto.getSkills() != null ? dto.getSkills() : List.of());
        existing.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        existing.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        existing.setBacklogs(dto.getBacklogs() != null ? dto.getBacklogs() : 0);
        existing.setUpdatedAt(DateTimeUtil.nowIso());

        return studentRepository.save(existing);
    }

    public synchronized boolean deleteStudent(String id) {
        Student student = getStudentById(id);
        // Also remove associated registrations to maintain referential integrity
        registrationRepository.findByStudentId(student.getId()).forEach(r -> {
            registrationRepository.deleteById(r.getId());
        });
        return studentRepository.deleteById(id);
    }
}
