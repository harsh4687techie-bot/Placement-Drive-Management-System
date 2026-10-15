package com.placement.repository;

import com.google.gson.reflect.TypeToken;
import com.placement.model.PlacementDrive;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PlacementDriveRepository {

    private static final String FILE_NAME = "drives.json";
    private static final Type LIST_TYPE = new TypeToken<List<PlacementDrive>>() {}.getType();

    private final JsonStorageEngine storageEngine;

    public PlacementDriveRepository(JsonStorageEngine storageEngine) {
        this.storageEngine = storageEngine;
    }

    public synchronized List<PlacementDrive> findAll() {
        return storageEngine.readList(FILE_NAME, LIST_TYPE);
    }

    public synchronized Optional<PlacementDrive> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(d -> id.equalsIgnoreCase(d.getId()))
                .findFirst();
    }

    public synchronized PlacementDrive save(PlacementDrive drive) {
        List<PlacementDrive> drives = new ArrayList<>(findAll());
        if (drive.getId() == null || drive.getId().isBlank()) {
            drive.setId(UUID.randomUUID().toString());
            drives.add(drive);
        } else {
            boolean updated = false;
            for (int i = 0; i < drives.size(); i++) {
                if (drives.get(i).getId().equalsIgnoreCase(drive.getId())) {
                    drives.set(i, drive);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                drives.add(drive);
            }
        }
        storageEngine.writeList(FILE_NAME, drives);
        return drive;
    }

    public synchronized void saveAll(List<PlacementDrive> newDrives) {
        storageEngine.writeList(FILE_NAME, newDrives != null ? newDrives : new ArrayList<>());
    }

    public synchronized boolean deleteById(String id) {
        List<PlacementDrive> drives = new ArrayList<>(findAll());
        boolean removed = drives.removeIf(d -> d.getId().equalsIgnoreCase(id));
        if (removed) {
            storageEngine.writeList(FILE_NAME, drives);
        }
        return removed;
    }
}
