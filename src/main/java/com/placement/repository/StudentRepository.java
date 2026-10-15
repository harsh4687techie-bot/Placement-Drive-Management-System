package com.placement.repository;

import com.google.gson.reflect.TypeToken;
import com.placement.model.Student;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StudentRepository {

    private static final String FILE_NAME = "students.json";
    private static final Type LIST_TYPE = new TypeToken<List<Student>>() {}.getType();

    private final JsonStorageEngine storageEngine;

    public StudentRepository(JsonStorageEngine storageEngine) {
        this.storageEngine = storageEngine;
    }

    public synchronized List<Student> findAll() {
        return storageEngine.readList(FILE_NAME, LIST_TYPE);
    }

    public synchronized Optional<Student> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(s -> id.equalsIgnoreCase(s.getId()))
                .findFirst();
    }

    public synchronized Optional<Student> findByRollNumber(String rollNumber) {
        if (rollNumber == null || rollNumber.isBlank()) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(s -> rollNumber.trim().equalsIgnoreCase(s.getRollNumber().trim()))
                .findFirst();
    }

    public synchronized boolean existsByRollNumber(String rollNumber) {
        return findByRollNumber(rollNumber).isPresent();
    }

    public synchronized boolean existsByRollNumberAndIdNot(String rollNumber, String excludeId) {
        if (rollNumber == null || rollNumber.isBlank()) {
            return false;
        }
        return findAll().stream()
                .filter(s -> !s.getId().equalsIgnoreCase(excludeId))
                .anyMatch(s -> rollNumber.trim().equalsIgnoreCase(s.getRollNumber().trim()));
    }

    public synchronized Student save(Student student) {
        List<Student> students = new ArrayList<>(findAll());
        if (student.getId() == null || student.getId().isBlank()) {
            student.setId(UUID.randomUUID().toString());
            students.add(student);
        } else {
            boolean updated = false;
            for (int i = 0; i < students.size(); i++) {
                if (students.get(i).getId().equalsIgnoreCase(student.getId())) {
                    students.set(i, student);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                students.add(student);
            }
        }
        storageEngine.writeList(FILE_NAME, students);
        return student;
    }

    public synchronized void saveAll(List<Student> newStudents) {
        storageEngine.writeList(FILE_NAME, newStudents != null ? newStudents : new ArrayList<>());
    }

    public synchronized boolean deleteById(String id) {
        List<Student> students = new ArrayList<>(findAll());
        boolean removed = students.removeIf(s -> s.getId().equalsIgnoreCase(id));
        if (removed) {
            storageEngine.writeList(FILE_NAME, students);
        }
        return removed;
    }
}
