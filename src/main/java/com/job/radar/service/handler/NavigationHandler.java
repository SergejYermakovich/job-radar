package com.job.radar.service.handler;

import com.job.radar.model.entity.Resume;
import com.job.radar.model.enums.statemachine.event.FormEvent;
import com.job.radar.model.enums.statemachine.event.MenuEvent;
import com.job.radar.model.enums.statemachine.event.ResumeEvent;
import com.job.radar.model.enums.statemachine.state.FormState;
import com.job.radar.model.enums.statemachine.state.MenuState;
import com.job.radar.model.enums.statemachine.state.ResumeState;
import com.job.radar.model.integration.Salary;
import com.job.radar.model.integration.Vacancy;
import com.job.radar.model.integration.VacancyResponse;
import com.job.radar.service.HeadHunterHttpService;
import com.job.radar.service.KeyboardService;
import com.job.radar.service.ResumeService;
import com.job.radar.service.StateMachineManager;
import com.job.radar.utils.LoggerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.job.radar.utils.ButtonConsts.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class NavigationHandler {
    private final StateMachineManager stateMachineManager;
    private final ResumeService resumeService;
    private final KeyboardService keyboardService;
    private final HeadHunterHttpService headHunterHttpService;
    private final MessageSender messageSender;

    public BotApiMethod<?> handleUpdate(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return null;
        }
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        log.info("NavigationHandler text update: {}, chat: {}", text, chatId);

        if (CMD_START.equals(text)) {
            stateMachineManager.cleanupUserSession(chatId);
            return showWelcomeMessage(chatId);
        }

        if (CMD_CANCEL.equals(text)) {
            stateMachineManager.cleanupResumeMachine(chatId);
            return showMainMenu(chatId);
        }

        if (CMD_TEST.equals(text)) {
            return showTest(chatId);
        }

        return handleNavigation(chatId, text);
    }

    private BotApiMethod<?> handleNavigation(Long chatId, String text) {
        MenuState currentMenuState = stateMachineManager.getCurrentMenuState(chatId);

        log.info("currentMenuState: {}, chat id: {}", currentMenuState, chatId);
        return switch (currentMenuState) {
            case MAIN_MENU -> handleMainMenu(chatId, text);
            case RESUME_SECTION -> handleResumeSection(chatId, text);
            case VACANCIES_SECTION -> handleVacanciesSection(chatId, text);
            case SETTINGS_SECTION -> handleSettingsSection(chatId, text);
            default -> showMainMenu(chatId);
        };
    }

    private BotApiMethod<?> handleMainMenu(Long chatId, String text) {
        StateMachine<MenuState, MenuEvent> menuMachine = stateMachineManager.getMenuStateMachine(chatId);

        switch (text) {
            case MY_RESUME:
                menuMachine.sendEvent(MenuEvent.OPEN_RESUME);
                return enterResumeSection(chatId);
            case VACANCIES:
                menuMachine.sendEvent(MenuEvent.OPEN_VACANCIES);
                return keyboardService.showVacanciesMenu(chatId);
            case SETTINGS:
                menuMachine.sendEvent(MenuEvent.OPEN_SETTINGS);
                return keyboardService.showSettings(chatId);
            case BACK:
                menuMachine.sendEvent(MenuEvent.BACK);
                return showMainMenu(chatId);
            default:
                return showMainMenu(chatId);
        }
    }

    public BotApiMethod<?> showWelcomeMessage(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("👋 Добро пожаловать в JobRadar!\n\nЯ помогу вам создать резюме и найти работу.")
                .replyMarkup(keyboardService.createMainMenuKeyboard())
                .build();
    }

    public BotApiMethod<?> handleResumeSection(Long chatId, String text) {
        // Обработка кнопки "Назад"
        if (BACK.equals(text)) {
            return handleBackToMainMenu(chatId);
        }

        // Обработка кнопок в разделе резюме
        if (CREATE_RESUME.equals(text)) {
            return handleCreateResume(chatId);
        }

        if (EDIT_RESUME.equals(text)) {
            // TODO: Реализовать редактирование резюме
            return showMainMenu(chatId);
        }

        // По умолчанию показываем раздел резюме
        return enterResumeSection(chatId);
    }

    private BotApiMethod<?> handleCreateResume(Long chatId) {
        // Запускаем процесс создания резюме
        StateMachine<ResumeState, ResumeEvent> resumeMachine =
                stateMachineManager.getResumeStateMachine(chatId);
        resumeMachine.sendEvent(ResumeEvent.CREATE_RESUME);

        // Запускаем форму создания резюме
        FormState formState = stateMachineManager.getCurrentFormState(chatId);
        if (formState == null || formState == FormState.FORM_IDLE) {
            StateMachine<FormState, FormEvent> formMachine =
                    stateMachineManager.getFormStateMachine(chatId);
            formMachine.sendEvent(FormEvent.START_CREATION);

            // Запрашиваем первое поле формы
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("👤 Введите ваше ФИО:")
                    .replyMarkup(keyboardService.createFormNavigationKeyboard())
                    .build();
        }
        return enterResumeSection(chatId);
    }

    private BotApiMethod<?> handleBackToMainMenu(Long chatId) {
        StateMachine<MenuState, MenuEvent> menuMachine = stateMachineManager.getMenuStateMachine(chatId);
        menuMachine.sendEvent(MenuEvent.BACK);
        return showMainMenu(chatId);
    }

    public BotApiMethod<?> handleVacanciesSection(Long chatId, String text) {
        if (BACK.equals(text)) {
            return handleBackToMainMenu(chatId);
        }

        if (SEARCH_VACANCIES.equals(text)) {
            return searchVacancies(chatId);
        }

        // TODO: Добавить обработку "📋 Мои отклики"

        return keyboardService.showVacanciesMenu(chatId);
    }


    public BotApiMethod<?> handleSettingsSection(Long chatId, String text) {
        if (BACK.equals(text)) {
            return handleBackToMainMenu(chatId);
        }

        // Обработка других кнопок в разделе настроек
        // TODO: Добавить обработку "⚙️ Настройки профиля" и "🔔 Уведомления"

        return keyboardService.showSettings(chatId);
    }

    public BotApiMethod<?> showResumeCreationPrompt(Long chatId) {
        ReplyKeyboardMarkup keyboard = createSimpleKeyboard(CREATE_RESUME, BACK);

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("📄 У вас пока нет резюме. Хотите создать новое?")
                .replyMarkup(keyboard)
                .build();
    }

    private BotApiMethod<?> showExistingResume(Long chatId, Resume resume) {
        StringBuilder resumeText = new StringBuilder("📄 Ваше резюме:\n\n");

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

        ReplyKeyboardMarkup keyboard = createSimpleKeyboard(EDIT_RESUME, BACK);

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(resumeText.toString())
                .replyMarkup(keyboard)
                .build();
    }

    private ReplyKeyboardMarkup createSimpleKeyboard(String... buttons) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        for (String button : buttons) {
            KeyboardRow row = new KeyboardRow();
            row.add(button);
            rows.add(row);
        }

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private BotApiMethod<?> enterResumeSection(Long chatId) {
        // Получаем или создаем Resume State Machine
        stateMachineManager.getResumeStateMachine(chatId);

        Optional<Resume> resume = resumeService.findByChatId(chatId);
        return resume.isPresent()
                ? showExistingResume(chatId, resume.get())
                : showResumeCreationPrompt(chatId);
    }

    private BotApiMethod<?> showMainMenu(Long chatId) {
        StateMachine<MenuState, MenuEvent> menuMachine = stateMachineManager.getMenuStateMachine(chatId);
        // Сбрасываем состояние, если не в главном меню
        if (menuMachine.getState().getId() != MenuState.MAIN_MENU) {
            stateMachineManager.cleanupUserSession(chatId);
        }

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("Главное меню:")
                .replyMarkup(keyboardService.createMainMenuKeyboard())
                .build();
    }

    private BotApiMethod<?> showTest(Long chatId) {
        VacancyResponse response = null;
        try {
            response = headHunterHttpService.searchVacancies("java");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Vacancy vacancy : response.getVacancies()) {
            LoggerUtil.log(vacancy);
        }
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("vacancies: " + response.getFound())
                .replyMarkup(keyboardService.createMainMenuKeyboard())
                .build();
    }

    private BotApiMethod<?> searchVacancies(Long chatId) {
        VacancyResponse response = null;
        try {
            response = headHunterHttpService.searchVacancies("java");
        } catch (IOException e) {
            log.error("Error searching vacancies", e);
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Произошла ошибка при поиске вакансий. Попробуйте позже.")
                    .replyMarkup(keyboardService.createVacanciesMenuKeyboard())
                    .build();
        }

        if (response.getVacancies() == null || response.getVacancies().isEmpty()) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("🔍 Вакансии не найдены.")
                    .replyMarkup(keyboardService.createVacanciesMenuKeyboard())
                    .build();
        }

        // Send individual messages for each vacancy
        for (Vacancy vacancy : response.getVacancies()) {
            try {
                sendVacancyMessage(chatId, vacancy);
            } catch (TelegramApiException e) {
                log.error("Error sending vacancy message", e);
            }
        }

        String messageText = String.format(
                "🔍 Найдено вакансий: %d\n\n" +
                "Показано первых %d результатов.",
                response.getFound(),
                response.getVacancies().size()
        );

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(messageText)
                .replyMarkup(keyboardService.createVacanciesMenuKeyboard())
                .build();
    }

    private void sendVacancyMessage(Long chatId, Vacancy vacancy) throws TelegramApiException {
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

        // Create inline keyboard with link button
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        
        InlineKeyboardButton linkButton = new InlineKeyboardButton();
        linkButton.setText("🔗 Открыть вакансию");
        linkButton.setUrl(vacancy.getAlternateUrl());
        row.add(linkButton);
        
        keyboard.add(row);
        inlineKeyboard.setKeyboard(keyboard);

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(messageText.toString())
                .replyMarkup(inlineKeyboard)
                .build();

        messageSender.execute(message);
    }
}