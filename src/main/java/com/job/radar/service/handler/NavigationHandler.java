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
import com.job.radar.service.VacancySearchService;
import com.job.radar.utils.LoggerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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
import java.util.Set;

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
    private final VacancySearchService vacancySearchService;

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

    private BotApiMethod<?> searchVacancies(Long chatId) {
        return searchVacancies(chatId, 0);
    }

    private BotApiMethod<?> searchVacancies(Long chatId, int page) {
        String searchQuery = "java"; // TODO: получать из профиля пользователя
        
        VacancyResponse response = null;
        try {
            response = headHunterHttpService.searchVacancies(searchQuery, page);
        } catch (IOException e) {
            log.error("Error searching vacancies", e);
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Произошла ошибка при поиске вакансий. Попробуйте позже.")
                    .replyMarkup(keyboardService.createVacanciesMenuKeyboard())
                    .build();
        }

        if (response.getVacancies() == null || response.getVacancies().isEmpty()) {
            vacancySearchService.clearSearchSession(chatId);
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("🔍 Вакансии не найдены.")
                    .replyMarkup(keyboardService.createVacanciesMenuKeyboard())
                    .build();
        }

        // Фильтруем только новые вакансии (не просмотренные ранее)
        List<Vacancy> newVacancies = filterNewVacancies(chatId, response.getVacancies());
        
        if (newVacancies.isEmpty() && page == 0) {
            // Если на первой странице все вакансии уже просмотрены, показываем их все равно
            newVacancies = response.getVacancies();
        } else if (newVacancies.isEmpty()) {
            // Если на других страницах все просмотрены, переходим на следующую
            return showVacanciesPage(chatId, searchQuery, page + 1, response.getPages(), response.getFound());
        }

        // Сохраняем сессию поиска
        VacancySearchService.SearchSession session = new VacancySearchService.SearchSession(
                searchQuery, 
                page, 
                response.getPages(), 
                response.getFound(), 
                response.getPerPage()
        );
        session.getAllVacancyIds().addAll(
                newVacancies.stream().map(Vacancy::getId).toList()
        );
        vacancySearchService.saveSearchSession(chatId, session);

        // Отмечаем вакансии как просмотренные
        List<String> vacancyIds = newVacancies.stream().map(Vacancy::getId).toList();
        vacancySearchService.markVacanciesAsViewed(chatId, vacancyIds);

        // Отправляем вакансии
        for (Vacancy vacancy : newVacancies) {
            try {
                sendVacancyMessage(chatId, vacancy);
            } catch (TelegramApiException e) {
                log.error("Error sending vacancy message", e);
            }
        }

        // Отправляем сообщение с пагинацией в конце (после всех вакансий)
        try {
            SendMessage paginationMessage = (SendMessage) showVacanciesPage(chatId, searchQuery, page, response.getPages(), response.getFound());
            messageSender.execute(paginationMessage);
        } catch (TelegramApiException e) {
            log.error("Error sending pagination message", e);
        }

        // Возвращаем null, так как сообщения уже отправлены
        return null;
    }

    private List<Vacancy> filterNewVacancies(Long chatId, List<Vacancy> vacancies) {
        Set<String> viewedIds = vacancySearchService.getViewedVacancyIds(chatId);
        return vacancies.stream()
                .filter(v -> !viewedIds.contains(v.getId()))
                .toList();
    }

    private BotApiMethod<?> showVacanciesPage(Long chatId,
                                              String searchQuery,
                                              int currentPage,
                                              int totalPages,
                                              int totalFound
    ) {
        String messageText = String.format(
                "🔍 Найдено вакансий: %d\n" +
                "📄 Страница %d из %d",
                totalFound,
                currentPage + 1,
                totalPages
        );

        InlineKeyboardMarkup keyboard = createPaginationKeyboard(chatId, currentPage, totalPages);

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(messageText)
                .replyMarkup(keyboard)
                .build();
    }

    private InlineKeyboardMarkup createPaginationKeyboard(Long chatId, int currentPage, int totalPages) {
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

    /**
     * Обработка callback query для пагинации
     */
    public BotApiMethod<?> handleCallbackQuery(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        // Отвечаем на callback query сразу
        try {
            messageSender.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .build());
        } catch (TelegramApiException e) {
            log.error("Error answering callback query", e);
        }

        if (data.startsWith("vacancy_page_")) {
            int page = Integer.parseInt(data.replace("vacancy_page_", ""));
            VacancySearchService.SearchSession session = vacancySearchService.getSearchSession(chatId);
            
            // Получаем новые вакансии для страницы
            String searchQuery = session != null ? session.getSearchQuery() : "java";
            VacancyResponse response;
            try {
                response = headHunterHttpService.searchVacancies(searchQuery, page);
            } catch (IOException e) {
                log.error("Error searching vacancies in callback", e);
                return null;
            }

            if (response.getVacancies() == null || response.getVacancies().isEmpty()) {
                return null;
            }

            // Определяем направление навигации
            VacancySearchService.SearchSession currentSession = vacancySearchService.getSearchSession(chatId);
            boolean goingForward = true;
            if (currentSession != null) {
                goingForward = page > currentSession.getCurrentPage();
            }
            
            // Фильтруем только новые вакансии
            List<Vacancy> newVacancies = filterNewVacancies(chatId, response.getVacancies());
            
            // Если идем назад, показываем все вакансии страницы (даже если просмотрены)
            // Если идем вперед и все просмотрены, ищем новые на следующих страницах
            if (newVacancies.isEmpty() && goingForward) {
                // Ищем новые вакансии на следующих страницах
                int currentPage = page;
                while (newVacancies.isEmpty() && currentPage < response.getPages() - 1) {
                    currentPage++;
                    try {
                        VacancyResponse nextResponse = headHunterHttpService.searchVacancies(searchQuery, currentPage);
                        if (nextResponse.getVacancies() != null && !nextResponse.getVacancies().isEmpty()) {
                            newVacancies = filterNewVacancies(chatId, nextResponse.getVacancies());
                            if (!newVacancies.isEmpty()) {
                                response = nextResponse;
                                page = currentPage;
                                break;
                            }
                        }
                    } catch (IOException e) {
                        log.error("Error searching next page", e);
                        break;
                    }
                }
            }
            
            // Если все равно пусто, показываем все вакансии текущей страницы
            if (newVacancies.isEmpty()) {
                newVacancies = response.getVacancies();
            }

            // Обновляем сессию
            VacancySearchService.SearchSession newSession = new VacancySearchService.SearchSession(
                    searchQuery, 
                    page, 
                    response.getPages(), 
                    response.getFound(), 
                    response.getPerPage()
            );
            newSession.getAllVacancyIds().addAll(
                    newVacancies.stream().map(Vacancy::getId).toList()
            );
            vacancySearchService.saveSearchSession(chatId, newSession);

            // Отмечаем как просмотренные
            List<String> vacancyIds = newVacancies.stream().map(Vacancy::getId).toList();
            vacancySearchService.markVacanciesAsViewed(chatId, vacancyIds);

            // Удаляем старое сообщение с пагинацией
            try {
                messageSender.execute(DeleteMessage.builder()
                        .chatId(chatId.toString())
                        .messageId(messageId)
                        .build());
            } catch (TelegramApiException e) {
                log.error("Error deleting old pagination message", e);
            }

            // Отправляем вакансии
            for (Vacancy vacancy : newVacancies) {
                try {
                    sendVacancyMessage(chatId, vacancy);
                } catch (TelegramApiException e) {
                    log.error("Error sending vacancy message", e);
                }
            }

            // Отправляем новое сообщение с пагинацией в конце (после всех вакансий)
            try {
                SendMessage paginationMessage = (SendMessage) showVacanciesPage(chatId, searchQuery, page, response.getPages(), response.getFound());
                messageSender.execute(paginationMessage);
            } catch (TelegramApiException e) {
                log.error("Error sending pagination message", e);
            }

            return null;
        } else if (data.equals("vacancy_new_search")) {
            // Очищаем просмотренные вакансии для нового поиска
            vacancySearchService.clearSearchSession(chatId);
            return searchVacancies(chatId, 0);
        }

        return null;
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