package com.presentation.bot;

import com.data.model.BotUser;
import com.data.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class VacancyTelegramBot extends TelegramLongPollingBot {

    private final UserRepository repository;

    public VacancyTelegramBot(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getBotUsername() {
        return "VacancyTrackerBot"; // Замените на имя вашего бота
    }

    @Override
    public String getBotToken() {
        String token = System.getenv("BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            log.error("❌ Токен бота не найден в переменных окружения (BOT_TOKEN)!");
        }
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();

        log.info("Получено сообщение от {}: {}", chatId, text);

        if (text.startsWith("/stop")) {
            handleStopCommand(chatId);
        } else if (text.startsWith("/start")) {
            handleStartCommand(chatId);
        } else if (text.startsWith("/salary")) {
            handleSalaryCommand(chatId, text);
        } else {
            handleKeywordInput(chatId, text);
        }
    }

    private void handleStartCommand(long chatId) {
        sendText(chatId, "👋 *Привет!* Я помогу найти работу на HH.ru.\n\n" +
                "1️⃣ Просто напиши название вакансии (например: `Java Developer`).\n" +
                "2️⃣ Используй `/salary 100000`, чтобы задать порог зарплаты.");
    }

    private void handleStopCommand(long chatId) {
        repository.delete(chatId);
        sendText(chatId, "❌ Подписка отменена. Все ваши данные удалены.");
    }

    private void handleSalaryCommand(long chatId, String text) {
        try {
            String salaryStr = text.replace("/salary", "").trim();
            int salary = Integer.parseInt(salaryStr);

            BotUser user = repository.findById(chatId).orElse(new BotUser());
            user.setChatId(chatId);
            user.setMinSalary(salary);
            repository.save(user);

            sendText(chatId, "💰 Минимальная зарплата установлена: *" + salary + " руб.*");
        } catch (NumberFormatException e) {
            sendText(chatId, "⚠️ Ошибка! Введите число после команды. Пример: `/salary 80000` ");
        }
    }

    private void handleKeywordInput(long chatId, String keyword) {
        BotUser user = repository.findById(chatId).orElse(new BotUser());
        user.setChatId(chatId);
        user.setKeyword(keyword);

        if (user.getMinSalary() == null) user.setMinSalary(0);

        repository.save(user);

        sendText(chatId, "✅ Настройка сохранена!\n🔎 Ищу: *" + keyword + "*\n💰 ЗП: *от " + user.getMinSalary() + " руб.*");
    }

    public void sendText(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode(ParseMode.MARKDOWN) // Позволяет делать жирный текст и ссылки
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения пользователю {}: {}", chatId, e.getMessage());
        }
    }
}
