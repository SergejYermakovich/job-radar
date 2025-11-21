package com.job.radar.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@Service
public class AskService {
    private final KeyboardService keyboardService;

    public AskService(KeyboardService keyboardService) {
        this.keyboardService = keyboardService;
    }

    public BotApiMethod<?> askForFullName(Long chatId) {
        log.info("FormFormHandler - askForFullName ....");
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("👤 Введите ваше ФИО:")
                .replyMarkup(keyboardService.createFormNavigationKeyboard())
                .build();
    }

    public BotApiMethod<?> askForAge(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("🎂 Введите ваш возраст:")
                .replyMarkup(keyboardService.createFormNavigationKeyboard())
                .build();
    }

    public BotApiMethod<?> askForCity(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("🏙️ Введите ваш город:")
                .replyMarkup(keyboardService.createFormNavigationKeyboard())
                .build();
    }

    public BotApiMethod<?> askForEmail(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("📧 Введите ваш email:")
                .replyMarkup(keyboardService.createFormNavigationKeyboard())
                .build();
    }

    public BotApiMethod<?> askForPhone(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("📱 Введите ваш номер телефона:")
                .replyMarkup(keyboardService.createFormNavigationKeyboard())
                .build();
    }
}
