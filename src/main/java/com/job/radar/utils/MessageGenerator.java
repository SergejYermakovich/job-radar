package com.job.radar.utils;

import com.job.radar.model.integration.Salary;
import com.job.radar.model.integration.Vacancy;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageGenerator {


    public String generateMessage(Vacancy vacancy) {
        StringBuilder messageText = new StringBuilder();

        // Vacancy name
        if (vacancy.getName() != null) {
            messageText.append("💼 ").append(vacancy.getName()).append("\n\n");
        }

        // Salary
        if (vacancy.getSalary() != null) {
            Salary salary = vacancy.getSalary();
            messageText.append("💰 Зарплата: ");
            if (salary.getFrom() != null && salary.getTo() != null) {
                messageText.append(salary.getFrom()).append(" - ").append(salary.getTo());
            } else if (salary.getFrom() != null) {
                messageText.append("от ").append(salary.getFrom());
            } else if (salary.getTo() != null) {
                messageText.append("до ").append(salary.getTo());
            }
            if (salary.getCurrency() != null) {
                messageText.append(" ").append(salary.getCurrency());
            }
            if (salary.getIsGross() != null && salary.getIsGross()) {
                messageText.append(" (до вычета НДФЛ)");
            }
            messageText.append("\n");
        }

        // Area (location)
        if (vacancy.getArea() != null && vacancy.getArea().getName() != null) {
            messageText.append("📍 ").append(vacancy.getArea().getName()).append("\n");
        }

        // Employer
        if (vacancy.getEmployer() != null && vacancy.getEmployer().getName() != null) {
            messageText.append("🏢 ").append(vacancy.getEmployer().getName()).append("\n");
        }

        // Experience
        if (vacancy.getExperience() != null && vacancy.getExperience().getName() != null) {
            messageText.append("📊 Опыт: ").append(vacancy.getExperience().getName()).append("\n");
        }

        // Employment type
        if (vacancy.getEmployment() != null && vacancy.getEmployment().getName() != null) {
            messageText.append("⏰ ").append(vacancy.getEmployment().getName()).append("\n");
        }

        return messageText.toString();
    }
}
