package com.data.repository;

import com.data.model.BotUser;
import com.data.repository.impl.JsonUserRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

class JsonUserRepositoryImplTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Сохранение и поиск пользователя")
    void testSaveAndFind() {
        JsonUserRepositoryImpl repository = new JsonUserRepositoryImpl();

        BotUser user = new BotUser();
        user.setChatId(999L);
        user.setKeyword("Python");

        repository.save(user);
        Optional<BotUser> found = repository.findById(999L);

        assertTrue(found.isPresent());
        assertEquals("Python", found.get().getKeyword());

        repository.delete(999L);
    }

    @Test
    void testFullRepositoryCycle() {
        JsonUserRepositoryImpl repo = new JsonUserRepositoryImpl();
        com.data.model.BotUser user = new com.data.model.BotUser();
        user.setChatId(555L);
        user.setKeyword("Test");

        repo.save(user);
        assertFalse(repo.findAll().isEmpty());
        assertTrue(repo.findById(555L).isPresent());

        repo.delete(555L);
        assertTrue(repo.findById(555L).isEmpty());
    }
}
