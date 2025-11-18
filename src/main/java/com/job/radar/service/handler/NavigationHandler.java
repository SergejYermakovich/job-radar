package com.job.radar.service.handler;

import com.job.radar.model.entity.Resume;
import com.job.radar.model.enums.statemachine.event.MenuEvent;
import com.job.radar.model.enums.statemachine.state.MenuState;
import com.job.radar.service.ResumeService;
import com.job.radar.service.StateMachineManager;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("deprecation")
@Service
public class NavigationHandler {

    private final StateMachineManager stateMachineManager;
    private final ResumeService resumeService;

    public NavigationHandler(StateMachineManager stateMachineManager, ResumeService resumeService) {
        this.stateMachineManager = stateMachineManager;
        this.resumeService = resumeService;
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return null;
        }
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        // Проверяем специальные команды
        if ("/start".equals(text)) {
            stateMachineManager.cleanupUserSession(chatId); // Сбрасываем сессию
            return showWelcomeMessage(chatId);
        }

        if ("/cancel".equals(text)) {
            stateMachineManager.cleanupResumeMachine(chatId);
            return showMainMenu(chatId);
        }

        // Основная логика обработки
        return handleNavigation(chatId, text);
    }

    private BotApiMethod<?> handleNavigation(Long chatId, String text) {
        MenuState currentMenuState = stateMachineManager.getCurrentMenuState(chatId);

        switch (currentMenuState) {
            case MAIN_MENU:
                return handleMainMenu(chatId, text);

            case RESUME_SECTION:
                return handleResumeSection(chatId, text);

            case VACANCIES_SECTION:
                return handleVacanciesSection(chatId, text);

            default:
                return showMainMenu(chatId);
        }
    }

    private BotApiMethod<?> handleMainMenu(Long chatId, String text) {
        StateMachine<MenuState, MenuEvent> menuMachine = stateMachineManager.getMenuStateMachine(chatId);

        switch (text) {
            case "📄 Моё резюме":
                menuMachine.sendEvent(MenuEvent.OPEN_RESUME);
                return enterResumeSection(chatId);

            case "💼 Вакансии":
                menuMachine.sendEvent(MenuEvent.OPEN_VACANCIES);
                return showVacanciesMenu(chatId);

            case "⚙️ Настройки":
                menuMachine.sendEvent(MenuEvent.OPEN_SETTINGS);
                return showSettings(chatId);

            default:
                return showMainMenu(chatId);
        }
    }

    public BotApiMethod<?> showWelcomeMessage(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("👋 Добро пожаловать в JobRadar!\n\nЯ помогу вам создать резюме и найти работу.")
                .replyMarkup(createMainMenuKeyboard())
                .build();
    }

    public BotApiMethod<?> handleResumeSection(Long chatId, String text) {
        return enterResumeSection(chatId);
    }

    public BotApiMethod<?> handleVacanciesSection(Long chatId, String text) {
        return showVacanciesMenu(chatId);
    }

    public BotApiMethod<?> showVacanciesMenu(Long chatId) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("🔍 Поиск вакансий");
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("📋 Мои отклики");
        rows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add("↩️ Назад");
        rows.add(row3);

        keyboard.setKeyboard(rows);

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("💼 Раздел вакансий:")
                .replyMarkup(keyboard)
                .build();
    }

    public BotApiMethod<?> showSettings(Long chatId) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("⚙️ Настройки профиля");
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🔔 Уведомления");
        rows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add("↩️ Назад");
        rows.add(row3);

        keyboard.setKeyboard(rows);

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("⚙️ Настройки:")
                .replyMarkup(keyboard)
                .build();
    }

    public BotApiMethod<?> showResumeCreationPrompt(Long chatId) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("✅ Создать резюме");
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("↩️ Назад");
        rows.add(row2);

        keyboard.setKeyboard(rows);

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("📄 У вас пока нет резюме. Хотите создать новое?")
                .replyMarkup(keyboard)
                .build();
    }

    private BotApiMethod<?> showExistingResume(Long chatId, Resume resume) {
        StringBuilder resumeText = new StringBuilder();
        resumeText.append("📄 Ваше резюме:\n\n");
        
        if (resume.getFullName() != null) {
            resumeText.append("👤 ФИО: ").append(resume.getFullName()).append("\n");
        }
        if (resume.getEmail() != null) {
            resumeText.append("📧 Email: ").append(resume.getEmail()).append("\n");
        }
        if (resume.getPhone() != null) {
            resumeText.append("📱 Телефон: ").append(resume.getPhone()).append("\n");
        }
        if (resume.getCity() != null) {
            resumeText.append("🏙️ Город: ").append(resume.getCity()).append("\n");
        }
        if (resume.getPosition() != null) {
            resumeText.append("💼 Должность: ").append(resume.getPosition()).append("\n");
        }

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("✏️ Редактировать");
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("↩️ Назад");
        rows.add(row2);

        keyboard.setKeyboard(rows);

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(resumeText.toString())
                .replyMarkup(keyboard)
                .build();
    }

    private BotApiMethod<?> enterResumeSection(Long chatId) {
        // Получаем или создаем Resume State Machine
        stateMachineManager.getResumeStateMachine(chatId);

        Optional<Resume> resume = resumeService.findByChatId(chatId);

        if (resume.isPresent()) {
            return showExistingResume(chatId, resume.get());
        } else {
            return showResumeCreationPrompt(chatId);
        }
    }

    private BotApiMethod<?> showMainMenu(Long chatId) {
        // Убеждаемся, что мы в главном меню
        StateMachine<MenuState, MenuEvent> menuMachine = stateMachineManager.getMenuStateMachine(chatId);
        if (menuMachine.getState().getId() != MenuState.MAIN_MENU) {
            // Сбрасываем состояние
            stateMachineManager.cleanupUserSession(chatId);
        }

        ReplyKeyboardMarkup keyboard = createMainMenuKeyboard();
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("Главное меню:")
                .replyMarkup(keyboard)
                .build();
    }

    private ReplyKeyboardMarkup createMainMenuKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("📄 Моё резюме");
        row1.add("💼 Вакансии");
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🔍 Поиск");
        row2.add("⚙️ Настройки");
        rows.add(row2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }
}