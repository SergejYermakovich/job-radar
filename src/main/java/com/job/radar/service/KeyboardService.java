package com.job.radar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.job.radar.utils.ButtonConsts.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class KeyboardService {

    public ReplyKeyboardMarkup createMainMenuKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(MY_RESUME);
        row1.add(VACANCIES);
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(SEARCH);
        row2.add(SETTINGS);
        rows.add(row2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public ReplyKeyboardMarkup createFormNavigationKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(SKIP);
        row1.add(BACK);
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(CANCEL);
        rows.add(row2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public ReplyKeyboardMarkup createVacanciesMenuKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(SEARCH_VACANCIES);
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(MY_APPLICATIONS);
        rows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(BACK);
        rows.add(row3);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public BotApiMethod<?> showVacanciesMenu(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("💼 Раздел вакансий:")
                .replyMarkup(createVacanciesMenuKeyboard())
                .build();
    }

    public BotApiMethod<?> showSettings(Long chatId) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(PROFILE_SETTINGS);
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(NOTIFICATIONS);
        rows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(BACK);
        rows.add(row3);

        keyboard.setKeyboard(rows);

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("⚙️ Настройки:")
                .replyMarkup(keyboard)
                .build();
    }

    /**
     * Создает inline клавиатуру для пагинации вакансий
     * @param currentPage текущая страница (0-based)
     * @param totalPages общее количество страниц
     * @return InlineKeyboardMarkup с кнопками навигации
     */
    public InlineKeyboardMarkup createVacancyPaginationKeyboard(int currentPage, int totalPages) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        // Кнопка "Назад"
        if (currentPage > 0) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton();
            prevButton.setText("◀️ Назад");
            prevButton.setCallbackData("vacancy_page_" + (currentPage - 1));
            row.add(prevButton);
        }

        // Кнопка "Вперед"
        if (currentPage < totalPages - 1) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("Вперед ▶️");
            nextButton.setCallbackData("vacancy_page_" + (currentPage + 1));
            row.add(nextButton);
        }

        if (!row.isEmpty()) {
            rows.add(row);
        }

        // Кнопка "Новый поиск" (очищает просмотренные)
        List<InlineKeyboardButton> newSearchRow = new ArrayList<>();
        InlineKeyboardButton newSearchButton = new InlineKeyboardButton();
        newSearchButton.setText("🔄 Новый поиск");
        newSearchButton.setCallbackData("vacancy_new_search");
        newSearchRow.add(newSearchButton);
        rows.add(newSearchRow);

        keyboard.setKeyboard(rows);
        return keyboard;
    }
}
