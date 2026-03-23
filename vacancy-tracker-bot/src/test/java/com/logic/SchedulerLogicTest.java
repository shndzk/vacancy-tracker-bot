package com.logic;

import com.data.repository.impl.JsonUserRepositoryImpl;
import com.presentation.bot.VacancyTelegramBot;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

class SchedulerLogicTest {

    @Test
    void testSalaryAndMarkdown() throws Exception {
        VacancySearchScheduler scheduler = new VacancySearchScheduler(
                new VacancyTelegramBot(new JsonUserRepositoryImpl()),
                new JsonUserRepositoryImpl()
        );

        Method parseSalary = VacancySearchScheduler.class.getDeclaredMethod("parseSalary", String.class);
        parseSalary.setAccessible(true);

        assertEquals(150000, parseSalary.invoke(scheduler, "от 150 000 руб."));
        assertEquals(0, parseSalary.invoke(scheduler, "з/п не указана"));
        assertEquals(0, parseSalary.invoke(scheduler, (String) null));

        Method escape = VacancySearchScheduler.class.getDeclaredMethod("escapeMarkdown", String.class);
        escape.setAccessible(true);

        assertEquals("Java\\_Developer", escape.invoke(scheduler, "Java_Developer"));
        assertEquals("Co\\*Ltd", escape.invoke(scheduler, "Co*Ltd"));
        assertEquals("", escape.invoke(scheduler, (String) null));
    }
}
