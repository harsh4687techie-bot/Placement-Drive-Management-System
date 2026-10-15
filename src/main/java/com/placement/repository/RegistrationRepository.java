package com.placement.repository;

import com.google.gson.reflect.TypeToken;
import com.placement.model.PlacementStatus;
import com.placement.model.Registration;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RegistrationRepository {

    private static final String FILE_NAME = "registrations.json";
    private static final Type LIST_TYPE = new TypeToken<List<Registration>>() {}.getType();

    private final JsonStorageEngine storageEngine;

    public RegistrationRepository(JsonStorageEngine storageEngine) {
        this.storageEngine = storageEngine;
    }

    public synchronized List<Registration> findAll() {
        return storageEngine.readList(FILE_NAME, LIST_TYPE);
    }

    public synchronized Optional<Registration> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(r -> id.equalsIgnoreCase(r.getId()))
                .findFirst();
    }

    public synchronized Optional<Registration> findByStudentIdAndDriveId(String studentId, String driveId) {
        if (studentId == null || driveId == null) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(r -> studentId.equalsIgnoreCase(r.getStudentId()) && driveId.equalsIgnoreCase(r.getDriveId()))
                .findFirst();
    }

    public synchronized List<Registration> findByStudentId(String studentId) {
        if (studentId == null) {
            return new ArrayList<>();
        }
        return findAll().stream()
                .filter(r -> studentId.equalsIgnoreCase(r.getStudentId()))
                .collect(Collectors.toList());
    }

    public synchronized List<Registration> findByDriveId(String driveId) {
        if (driveId == null) {
            return new ArrayList<>();
        }
        return findAll().stream()
                .filter(r -> driveId.equalsIgnoreCase(r.getDriveId()))
                .collect(Collectors.toList());
    }

    public synchronized List<Registration> findByStatus(PlacementStatus status) {
        if (status == null) {
            return new ArrayList<>();
        }
        return findAll().stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    }

    public synchronized Registration save(Registration registration) {
        List<Registration> registrations = new ArrayList<>(findAll());
        if (registration.getId() == null || registration.getId().isBlank()) {
            registration.setId(UUID.randomUUID().toString());
            registrations.add(registration);
        } else {
            boolean updated = false;
            for (int i = 0; i < registrations.size(); i++) {
                if (registrations.get(i).getId().equalsIgnoreCase(registration.getId())) {
                    registrations.set(i, registration);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                registrations.add(registration);
            }
        }
        storageEngine.writeList(FILE_NAME, registrations);
        return registration;
    }

    public synchronized void saveAll(List<Registration> newRegistrations) {
        storageEngine.writeList(FILE_NAME, newRegistrations != null ? newRegistrations : new ArrayList<>());
    }

    public synchronized boolean deleteById(String id) {
        List<Registration> registrations = new ArrayList<>(findAll());
        boolean removed = registrations.removeIf(r -> r.getId().equalsIgnoreCase(id));
        if (removed) {
            storageEngine.writeList(FILE_NAME, registrations);
        }
        return removed;
    }
}
