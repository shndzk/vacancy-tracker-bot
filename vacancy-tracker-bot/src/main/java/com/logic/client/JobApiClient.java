package com.logic.client;

import com.exception.ApiException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
public class JobApiClient {
    private static final String BASE_URL = "https://api.hh.ru/vacancies";
    private static final String USER_AGENT = "VacancyTrackerBot/1.0 (8097776mts@gmail.com)";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public String searchVacancies(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8);
            String urlString = String.format("%s?text=%s&per_page=10&order_by=publication_time", BASE_URL, encodedKeyword);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            log.debug("Отправка запроса к HH API: {}", urlString);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                log.error("HH API вернул ошибку: {} | Тело ответа: {}", response.statusCode(), response.body());
                throw new ApiException("Ошибка HH API: статус " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            log.error("Сетевая ошибка при запросе к HH API для ключевого слова: {}", keyword, e);
            Thread.currentThread().interrupt(); // Важно для InterruptedException
            throw new ApiException("Не удалось выполнить запрос к API", e);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка в JobApiClient", e);
            throw new ApiException("Внутренняя ошибка клиента API", e);
        }
    }
}

