package com.placement.repository;

import com.google.gson.reflect.TypeToken;
import com.placement.model.StatusHistory;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class StatusHistoryRepository {

    private static final String FILE_NAME = "status_history.json";
    private static final Type LIST_TYPE = new TypeToken<List<StatusHistory>>() {}.getType();

    private final JsonStorageEngine storageEngine;

    public StatusHistoryRepository(JsonStorageEngine storageEngine) {
        this.storageEngine = storageEngine;
    }

    public synchronized List<StatusHistory> findAll() {
        return storageEngine.readList(FILE_NAME, LIST_TYPE);
    }

    public synchronized List<StatusHistory> findByRegistrationId(String registrationId) {
        if (registrationId == null) {
            return new ArrayList<>();
        }
        return findAll().stream()
                .filter(h -> registrationId.equalsIgnoreCase(h.getRegistrationId()))
                .sorted(Comparator.comparing(StatusHistory::getChangedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public synchronized List<StatusHistory> findRecent(int limit) {
        return findAll().stream()
                .sorted((a, b) -> {
                    if (a.getChangedAt() == null && b.getChangedAt() == null) return 0;
                    if (a.getChangedAt() == null) return 1;
                    if (b.getChangedAt() == null) return -1;
                    return b.getChangedAt().compareTo(a.getChangedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    public synchronized StatusHistory save(StatusHistory history) {
        List<StatusHistory> historyList = new ArrayList<>(findAll());
        if (history.getId() == null || history.getId().isBlank()) {
            history.setId(UUID.randomUUID().toString());
        }
        historyList.add(history);
        storageEngine.writeList(FILE_NAME, historyList);
        return history;
    }

    public synchronized void saveAll(List<StatusHistory> newHistoryList) {
        storageEngine.writeList(FILE_NAME, newHistoryList != null ? newHistoryList : new ArrayList<>());
    }
}
