package com.logic;

import com.exception.ApiException;
import com.data.model.Vacancy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VacancyParser {
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Vacancy> parse(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return List.of();
        }

        List<Vacancy> vacancyList = new ArrayList<>();
        try {
            JsonNode root = mapper.readTree(jsonString);
            JsonNode itemsNode = root.path("items");

            if (!itemsNode.isArray()) {
                log.warn("В ответе API отсутствует массив 'items'. Тело ответа: {}",
                        jsonString.substring(0, Math.min(jsonString.length(), 100)));
                return List.of();
            }

            for (JsonNode node : itemsNode) {
                try {
                    vacancyList.add(parseSingleVacancy(node));
                } catch (Exception e) {
                    log.error("Ошибка при парсинге отдельной вакансии: {}", e.getMessage());
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Критическая ошибка парсинга JSON структуры HH: {}", e.getMessage());
            throw new ApiException("Не удалось прочитать JSON от API", e);
        }

        return vacancyList;
    }

    private Vacancy parseSingleVacancy(JsonNode node) {
        Vacancy vacancy = new Vacancy();

        vacancy.setId(node.path("id").asText("0"));
        vacancy.setTitle(node.path("name").asText("Без названия"));
        vacancy.setCompany(node.path("employer").path("name").asText("Компания не указана"));
        vacancy.setUrl(node.path("alternate_url").asText("https://hh.ru"));

        vacancy.setSalary(formatSalary(node.path("salary")));

        return vacancy;
    }

    private String formatSalary(JsonNode salaryNode) {
        if (salaryNode.isNull() || salaryNode.isMissingNode()) {
            return "По договоренности";
        }

        String from = salaryNode.path("from").asText(null);
        String to = salaryNode.path("to").asText(null);
        String currency = salaryNode.path("currency").asText("");

        if (from != null && to != null) {
            return String.format("от %s до %s %s", from, to, currency);
        } else if (from != null) {
            return String.format("от %s %s", from, currency);
        } else if (to != null) {
            return String.format("до %s %s", to, currency);
        }

        return "Зарплата не указана";
    }
}
