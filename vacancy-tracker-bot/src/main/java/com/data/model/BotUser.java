package com.data.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class BotUser {
    private Long chatId;
    private String keyword;
    private Integer minSalary;
    private Set<String> seenIds = new HashSet<>(); // Список ID вакансий, которые мы уже показали
}
