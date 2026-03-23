package com.data.repository.impl;

import com.exception.RepositoryException;
import com.data.model.BotUser;
import com.data.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class JsonUserRepositoryImpl implements UserRepository {
    private final ObjectMapper mapper;
    private final Path folderPath = Paths.get("users_data");

    public JsonUserRepositoryImpl() {
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            log.error("Не удалось создать папку для данных: {}", folderPath, e);
            throw new RepositoryException("Ошибка инициализации хранилища", e);
        }
    }

    @Override
    public void save(BotUser user) {
        Path filePath = folderPath.resolve(user.getChatId() + ".json");
        try {
            mapper.writeValue(filePath.toFile(), user);
            log.info("Данные пользователя {} успешно сохранены", user.getChatId());
        } catch (IOException e) {
            log.error("Ошибка при сохранении пользователя {}", user.getChatId(), e);
            throw new RepositoryException("Не удалось сохранить данные пользователя", e);
        }
    }

    @Override
    public List<BotUser> findAll() {
        List<BotUser> users = new ArrayList<>();
        File folder = folderPath.toFile();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File file : files) {
                try {
                    users.add(mapper.readValue(file, BotUser.class));
                } catch (IOException e) {
                    log.warn("Ошибка при чтении файла {}: {}", file.getName(), e.getMessage());
                }
            }
        }
        return users;
    }

    @Override
    public Optional<BotUser> findById(Long chatId) {
        Path filePath = folderPath.resolve(chatId + ".json");
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        try {
            BotUser user = mapper.readValue(filePath.toFile(), BotUser.class);
            return Optional.of(user);
        } catch (IOException e) {
            log.error("Ошибка при чтении данных пользователя {}", chatId, e);
            throw new RepositoryException("Ошибка загрузки данных пользователя", e);
        }
    }

    @Override
    public void delete(Long chatId) {
        Path filePath = folderPath.resolve(chatId + ".json");
        try {
            if (Files.deleteIfExists(filePath)) {
                log.info("Данные пользователя {} удалены", chatId);
            }
        } catch (IOException e) {
            log.error("Не удалось удалить файл пользователя {}", chatId, e);
            throw new RepositoryException("Ошибка при удалении данных", e);
        }
    }
}
