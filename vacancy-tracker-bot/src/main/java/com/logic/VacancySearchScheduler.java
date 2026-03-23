package com.logic;

import com.data.model.BotUser;
import com.data.model.Vacancy;
import com.data.repository.UserRepository;
import com.logic.client.JobApiClient;
import com.presentation.bot.VacancyTelegramBot;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class VacancySearchScheduler {
    private final VacancyTelegramBot bot;
    private final UserRepository repository;
    private final JobApiClient apiClient;
    private final VacancyParser parser;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public VacancySearchScheduler(VacancyTelegramBot bot, UserRepository repository) {
        this.bot = bot;
        this.repository = repository;
        this.apiClient = new JobApiClient();
        this.parser = new VacancyParser();
    }

    public void startPeriodicSearch() {
        scheduler.scheduleAtFixedRate(this::performSearch, 0, 1, TimeUnit.MINUTES);
        log.info("🚀 Фоновый планировщик поиска запущен (интервал: 1 мин)");
    }

    private void performSearch() {
        log.info("🔄 Запуск цикла проверки вакансий для всех пользователей...");

        List<BotUser> users = repository.findAll();
        if (users.isEmpty()) {
            log.debug("Список пользователей пуст, искать не для кого.");
            return;
        }

        for (BotUser user : users) {
            processUser(user);
        }
    }

    private void processUser(BotUser user) {
        if (user.getKeyword() == null || user.getKeyword().isBlank()) {
            return;
        }

        try {
            log.debug("Поиск для пользователя {}: '{}'", user.getChatId(), user.getKeyword());
            String jsonResponse = apiClient.searchVacancies(user.getKeyword());

            if (jsonResponse == null || jsonResponse.isBlank()) {
                log.warn("API вернуло пустой ответ для запроса: {}", user.getKeyword());
                return;
            }

            List<Vacancy> foundVacancies = parser.parse(jsonResponse);
            int userMinSalary = (user.getMinSalary() != null) ? user.getMinSalary() : 0;
            boolean hasUpdates = false;

            for (Vacancy v : foundVacancies) {
                if (!user.getSeenIds().contains(v.getId()) && parseSalary(v.getSalary()) >= userMinSalary) {

                    sendVacancyNotification(user.getChatId(), v);
                    user.getSeenIds().add(v.getId());
                    hasUpdates = true;
                }
            }

            if (hasUpdates) {
                repository.save(user);
                log.info("✅ Отправлены обновления пользователю {}", user.getChatId());
            }

        } catch (Exception e) {
            log.error("❌ Ошибка при обработке пользователя {}: {}", user.getChatId(), e.getMessage());
        }
    }

    private void sendVacancyNotification(Long chatId, Vacancy v) {
        String message = String.format(
                "🔥 *Найдена вакансия!*\n\n" +
                        "📌 *%s*\n" +
                        "🏢 Компания: %s\n" +
                        "💰 ЗП: %s\n" +
                        "🔗 [Открыть вакансию](%s)",
                escapeMarkdown(v.getTitle()),
                escapeMarkdown(v.getCompany()),
                v.getSalary(),
                v.getUrl()
        );
        bot.sendText(chatId, message);
    }

    private int parseSalary(String salaryStr) {
        if (salaryStr == null || salaryStr.isEmpty()) return 0;
        try {
            String digits = salaryStr.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? 0 : Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\` ");
    }
}
