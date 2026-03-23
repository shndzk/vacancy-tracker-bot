package com.data;

import com.data.model.BotUser;
import com.data.model.Vacancy;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
    @Test
    void testModels() {
        BotUser user = new BotUser();
        user.setChatId(1L);
        user.setKeyword("Java");
        user.setMinSalary(100000);
        user.setSeenIds(new HashSet<>());
        assertEquals(1L, user.getChatId());
        assertEquals("Java", user.getKeyword());

        Vacancy v = new Vacancy();
        v.setId("123");
        v.setTitle("Dev");
        v.setCompany("Google");
        v.setSalary("100k");
        v.setUrl("http");
        assertEquals("123", v.getId());
    }
}
