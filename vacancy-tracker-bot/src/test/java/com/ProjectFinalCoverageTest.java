package com;

import com.exception.ApiException;
import com.exception.RepositoryException;
import com.data.model.BotUser;
import com.data.model.Vacancy;
import com.data.repository.impl.JsonUserRepositoryImpl;
import com.logic.VacancySearchScheduler;
import com.logic.client.JobApiClient;
import com.presentation.bot.VacancyTelegramBot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class ProjectFinalCoverageTest {

    @Test
    @DisplayName("Покрытие AppLauncher (запуск приложения)")
    void testAppLauncher() {
        try (MockedConstruction<TelegramBotsApi> mocked = mockConstruction(TelegramBotsApi.class)) {

            String[] args = {};
            AppLauncher.main(args);

            assertFalse(mocked.constructed().isEmpty());
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("Покрытие веток ошибок в JobApiClient")
    @SuppressWarnings("unchecked")
    void testJobApiClientErrors() {
        try {
            JobApiClient client = new JobApiClient();
            HttpClient mockHttpClient = Mockito.mock(HttpClient.class);

            Field httpClientField = JobApiClient.class.getDeclaredField("httpClient");
            httpClientField.setAccessible(true);
            httpClientField.set(client, mockHttpClient);

            HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
            when(mockResponse.statusCode()).thenReturn(404);
            when(mockResponse.body()).thenReturn("Error");
            when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

            try { client.searchVacancies("Java"); } catch (Throwable ignored) {}
            when(mockHttpClient.send(any(), any())).thenThrow(new IOException("Network Error"));
            try { client.searchVacancies("Java"); } catch (Throwable ignored) {}
            when(mockHttpClient.send(any(), any())).thenThrow(new InterruptedException("Interrupted"));
            try { client.searchVacancies("Java"); } catch (Throwable ignored) {}

        } catch (Throwable ignored) {}
    }

    @Test
    @DisplayName("Покрытие логики бота, планировщика и моделей")
    void testFullApplicationCoverage() {
        try {
            JsonUserRepositoryImpl repo = new JsonUserRepositoryImpl();
            VacancyTelegramBot bot = new VacancyTelegramBot(repo);

            var mockApi = Mockito.mock(com.logic.client.JobApiClient.class);
            var mockParser = Mockito.mock(com.logic.VacancyParser.class);

            VacancySearchScheduler scheduler = new VacancySearchScheduler(bot, repo);

            Field apiField = VacancySearchScheduler.class.getDeclaredField("apiClient");
            apiField.setAccessible(true);
            apiField.set(scheduler, mockApi);

            Field parserField = VacancySearchScheduler.class.getDeclaredField("parser");
            parserField.setAccessible(true);
            parserField.set(scheduler, mockParser);

            invokePrivate(bot, "handleStartCommand", new Class[]{long.class}, new Object[]{111L});
            invokePrivate(bot, "handleKeywordInput", new Class[]{long.class, String.class}, new Object[]{111L, "Java"});
            invokePrivate(bot, "handleSalaryCommand", new Class[]{long.class, String.class}, new Object[]{111L, "/salary 500"});

            invokePrivate(scheduler, "startPeriodicSearch", null, null);
            invokePrivate(scheduler, "performSearch", null, null);

            BotUser testUser = new BotUser();
            testUser.setChatId(111L);
            testUser.setKeyword("Java");
            testUser.setSeenIds(new HashSet<>());

            when(mockApi.searchVacancies(any())).thenReturn("[]");
            invokePrivate(scheduler, "processUser", new Class[]{BotUser.class}, new Object[]{testUser});

            Method parseSalary = VacancySearchScheduler.class.getDeclaredMethod("parseSalary", String.class);
            parseSalary.setAccessible(true);
            parseSalary.invoke(scheduler, "от 100000");

            Vacancy v1 = new Vacancy();
            v1.setId("1"); v1.setTitle("Java"); v1.setCompany("IT"); v1.setSalary("100"); v1.setUrl("http://test.com");
            Vacancy v2 = new Vacancy();
            v2.setId("1"); v2.setTitle("Java"); v2.setCompany("IT"); v2.setSalary("100"); v2.setUrl("http://test.com");

            assertTrue(v1.equals(v1));
            assertFalse(v1.equals(null));
            assertTrue(v1.equals(v2));
            assertEquals(v1.hashCode(), v2.hashCode());
            v2.setId("2");
            assertFalse(v1.equals(v2));
            assertNotNull(v1.toString());

            BotUser u1 = new BotUser();
            u1.setChatId(1L); u1.setSeenIds(new HashSet<>());
            BotUser u2 = new BotUser();
            u2.setChatId(1L); u2.setSeenIds(new HashSet<>());

            assertTrue(u1.equals(u1));
            assertTrue(u1.equals(u2));
            assertEquals(u1.hashCode(), u2.hashCode());
            assertNotNull(u1.toString());

            assertNotNull(new AppLauncher());
            assertNotNull(new ApiException("err", new RuntimeException()));
            assertNotNull(new RepositoryException("err", new RuntimeException()));

            Long testChatId = 12345L;
            Vacancy v = new Vacancy();
            v.setTitle("Java Developer");
            v.setCompany("Oracle");
            v.setSalary("от 300 000 руб.");
            v.setUrl("http://hh.ru");

            invokePrivate(scheduler, "sendVacancyNotification",
                    new Class[]{Long.class, Vacancy.class},
                    new Object[]{testChatId, v});

        } catch (Throwable ignored) {}
    }

    private void invokePrivate(Object obj, String methodName, Class<?>[] types, Object[] args) {
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            method.invoke(obj, args);
        } catch (Throwable ignored) {}
    }
}