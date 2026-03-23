package com;

import com.data.repository.UserRepository;
import com.data.repository.impl.JsonUserRepositoryImpl;
import com.logic.VacancySearchScheduler;
import com.presentation.bot.VacancyTelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class AppLauncher {

    public static void main(String[] args) {
        log.info("🏁 Запуск приложения...");

        try {
            UserRepository repository = new JsonUserRepositoryImpl();

            VacancyTelegramBot vacancyBot = new VacancyTelegramBot(repository);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(vacancyBot);
            log.info("🤖 Telegram Bot успешно зарегистрирован.");

            VacancySearchScheduler scheduler = new VacancySearchScheduler(vacancyBot, repository);
            scheduler.startPeriodicSearch();

            log.info("🚀 Приложение полностью запущено и готово к работе!");

        } catch (Exception e) {
            log.error("❌ Критическая ошибка при запуске приложения: ", e);
        }
    }
}
