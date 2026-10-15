package com.placement.repository;

import com.google.gson.reflect.TypeToken;
import com.placement.config.StorageConfig;
import com.placement.exception.StorageException;
import com.placement.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonStorageEngineTest {

    @TempDir
    Path tempDir;

    private StorageConfig storageConfig;
    private JsonStorageEngine engine;
    private static final Type STUDENT_LIST_TYPE = new TypeToken<List<Student>>() {}.getType();

    @BeforeEach
    void setUp() {
        storageConfig = new StorageConfig();
        ReflectionTestUtils.setField(storageConfig, "dataDirConfig", tempDir.resolve("data").toString());
        ReflectionTestUtils.setField(storageConfig, "backupDirConfig", tempDir.resolve("backups").toString());
        ReflectionTestUtils.setField(storageConfig, "reportDirConfig", tempDir.resolve("reports").toString());
        storageConfig.init();

        engine = new JsonStorageEngine(storageConfig);
    }

    @Test
    @DisplayName("Should create missing file with empty list safely")
    void testMissingFileSafeInitialization() {
        List<Student> students = engine.readList("test_students.json", STUDENT_LIST_TYPE);
        assertNotNull(students);
        assertTrue(students.isEmpty());
        assertTrue(Files.exists(storageConfig.resolveDataFile("test_students.json")));
    }

    @Test
    @DisplayName("Should save and reload list of entities correctly using atomic write")
    void testSaveAndReloadList() {
        List<Student> list = new ArrayList<>();
        Student s1 = new Student("S1", "21CS001", "Alice Code", "CSE", 9.1, List.of("Java"), "alice@test.com", "123", 0, "2026-01-01", "2026-01-01");
        Student s2 = new Student("S2", "21CS002", "Bob Code", "CSE", 8.4, List.of("Python"), "bob@test.com", "456", 0, "2026-01-01", "2026-01-01");
        list.add(s1);
        list.add(s2);

        engine.writeList("test_students.json", list);

        List<Student> reloaded = engine.readList("test_students.json", STUDENT_LIST_TYPE);
        assertEquals(2, reloaded.size());
        assertEquals("21CS001", reloaded.get(0).getRollNumber());
        assertEquals("Alice Code", reloaded.get(0).getFullName());
        assertEquals("21CS002", reloaded.get(1).getRollNumber());
    }

    @Test
    @DisplayName("Corrupt JSON file should throw StorageException and NOT be wiped")
    void testCorruptedFileThrowsStorageExceptionWithoutDataLoss() throws IOException {
        Path filePath = storageConfig.resolveDataFile("corrupt.json");
        String badContent = "{ invalid: json... missing brackets";
        Files.writeString(filePath, badContent);

        assertThrows(StorageException.class, () -> {
            engine.readList("corrupt.json", STUDENT_LIST_TYPE);
        });

        // Verify the original content is still preserved
        String contentAfter = Files.readString(filePath);
        assertEquals(badContent, contentAfter, "Original corrupt file must not be silently wiped");
    }
}
