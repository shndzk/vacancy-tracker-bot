package com.data.repository;

import com.data.model.BotUser;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(BotUser user);
    Optional<BotUser> findById(Long chatId); // Найти пользователя
    void delete(Long chatId);
    List<BotUser> findAll();
}
