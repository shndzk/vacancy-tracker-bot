package com.logic;

import com.data.model.Vacancy;
import com.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VacancyParserTest {

    private VacancyParser parser;

    @BeforeEach
    void setUp() {
        parser = new VacancyParser();
    }

    @Test
    @DisplayName("Успешный парсинг корректного JSON от HH")
    void testParseValidJson() {
        String json = """
            {
              "items": [
                {
                  "id": "1001",
                  "name": "Java Developer",
                  "employer": { "name": "Google" },
                  "salary": { "from": 150000, "to": 200000, "currency": "RUR" },
                  "alternate_url": "https://hh.ru"
                }
              ]
            }
            """;

        List<Vacancy> result = parser.parse(json);

        assertFalse(result.isEmpty(), "Список вакансий не должен быть пустым");
        Vacancy v = result.get(0);
        assertEquals("1001", v.getId());
        assertEquals("Java Developer", v.getTitle());
        assertEquals("Google", v.getCompany());
        assertTrue(v.getSalary().contains("150000"));
    }

    @Test
    @DisplayName("Парсинг вакансии без указанной зарплаты (null)")
    void testParseNoSalary() {
        String json = "{\"items\": [{\"id\":\"2\",\"name\":\"QA\",\"employer\":{\"name\":\"Test\"},\"salary\":null}]}";

        List<Vacancy> result = parser.parse(json);

        assertEquals("По договоренности", result.get(0).getSalary());
    }

    @Test
    @DisplayName("Обработка некорректного JSON - ожидаем ApiException")
    void testParseInvalidJson() {
        assertThrows(ApiException.class, () -> {
            parser.parse("инвалидный-джейсон");
        });
    }

    @Test
    void testEmptyParse() {
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse(null).isEmpty());
    }
}
