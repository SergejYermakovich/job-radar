package com.job.radar.service.handler;

import com.job.radar.model.enums.statemachine.event.FormEvent;
import com.job.radar.model.enums.statemachine.state.FormState;
import com.job.radar.service.ResumeService;
import com.job.radar.service.StateMachineManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.job.radar.utils.ButtonConsts.*;

@Slf4j
@SuppressWarnings("deprecation")
@Service
public class FormHandler {

    private final StateMachineManager stateMachineManager;
    private final ResumeService resumeService;
    private final MessageSender messageSender;

    public FormHandler(StateMachineManager stateMachineManager,
                       ResumeService resumeService,
                       MessageSender messageSender) {
        this.stateMachineManager = stateMachineManager;
        this.resumeService = resumeService;
        this.messageSender = messageSender;
    }

    public BotApiMethod<?> handleFormStep(Long chatId, String text) {
        StateMachine<FormState, FormEvent> formMachine = stateMachineManager.getFormStateMachine(chatId);
        FormState currentState = formMachine.getState().getId();

        log.info("handleFormStep text: {}", text);

        // Обработка кнопок навигации
        if (CMD_SKIP.equals(text) || SKIP.equals(text)) {
            formMachine.sendEvent(FormEvent.SKIP);
            return askNextQuestion(chatId);
        }

        if (CMD_BACK.equals(text) || BACK.equals(text)) {
            formMachine.sendEvent(FormEvent.PREVIOUS);
            return askPreviousQuestion(chatId);
        }
        
        if (CANCEL.equals(text) || CMD_CANCEL.equals(text)) {
            formMachine.sendEvent(FormEvent.CANCEL);
            stateMachineManager.cleanupFormMachine(chatId);
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Заполнение формы отменено.")
                    .build();
        }

        // Обработка по текущему состоянию
        return switch (currentState) {
            case ENTERING_FULL_NAME -> processFullName(chatId, text, formMachine);
            case ENTERING_EMAIL -> processEmail(chatId, text, formMachine);
            case ENTERING_PHONE -> processPhone(chatId, text, formMachine);
            case ENTERING_AGE -> processAge(chatId, text, formMachine);
            case ENTERING_CITY -> processCity(chatId, text, formMachine);

            // ... обработка остальных полей

            case CONFIRMING_FORM -> processConfirmation(chatId, text, formMachine);
            default -> null;
        };
    }

    private BotApiMethod<?> processFullName(Long chatId, String text,
                                            StateMachine<FormState, FormEvent> formMachine) {
        // Валидация
        if (text.length() < 2) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ ФИО должно содержать минимум 2 символа. Попробуйте еще раз:")
                    .build();
        }

        // Сохраняем значение (временное хранение)
        saveFormField(chatId, "fullName", text);

        // Переходим к следующему шагу
        formMachine.sendEvent(FormEvent.NEXT);

        // Запрашиваем следующий вопрос
        return askForEmail(chatId);
    }

    private BotApiMethod<?> askForEmail(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("📧 Введите ваш email:")
                .replyMarkup(createFormNavigationKeyboard())
                .build();
    }

    private BotApiMethod<?> processEmail(Long chatId, String text,
                                         StateMachine<FormState, FormEvent> formMachine) {
        if (!isValidEmail(text)) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Неверный формат email. Попробуйте еще раз:")
                    .build();
        }

        saveFormField(chatId, "email", text);
        formMachine.sendEvent(FormEvent.NEXT);

        return askForPhone(chatId);
    }

    private BotApiMethod<?> askForPhone(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("📱 Введите ваш номер телефона:")
                .replyMarkup(createFormNavigationKeyboard())
                .build();
    }

    private ReplyKeyboardMarkup createFormNavigationKeyboard() {
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

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void saveFormField(Long chatId, String field, String value) {
        resumeService.createOrUpdate(chatId, field, value);
    }

    private void saveFormField(Long chatId, String field, Integer value) {
        resumeService.createOrUpdate(chatId, field, value);
    }

    public BotApiMethod<?> askNextQuestion(Long chatId) {
        StateMachine<FormState, FormEvent> formMachine = stateMachineManager.getFormStateMachine(chatId);
        FormState currentState = formMachine.getState().getId();

        return switch (currentState) {
            case ENTERING_FULL_NAME -> askForFullName(chatId);
            case ENTERING_EMAIL -> askForEmail(chatId);
            case ENTERING_PHONE -> askForPhone(chatId);
            case ENTERING_AGE -> askForAge(chatId);
            case ENTERING_CITY -> askForCity(chatId);
            default -> null;
        };
    }

    public BotApiMethod<?> askPreviousQuestion(Long chatId) {
        StateMachine<FormState, FormEvent> formMachine = stateMachineManager.getFormStateMachine(chatId);
        FormState currentState = formMachine.getState().getId();

        return switch (currentState) {
            case ENTERING_EMAIL -> askForFullName(chatId);
            case ENTERING_PHONE -> askForEmail(chatId);
            case ENTERING_AGE -> askForPhone(chatId);
            case ENTERING_CITY -> askForAge(chatId);
            default -> null;
        };
    }

    private BotApiMethod<?> askForFullName(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("👤 Введите ваше ФИО:")
                .replyMarkup(createFormNavigationKeyboard())
                .build();
    }

    private BotApiMethod<?> askForAge(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("🎂 Введите ваш возраст:")
                .replyMarkup(createFormNavigationKeyboard())
                .build();
    }

    private BotApiMethod<?> askForCity(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("🏙️ Введите ваш город:")
                .replyMarkup(createFormNavigationKeyboard())
                .build();
    }

    public BotApiMethod<?> processPhone(Long chatId, String text, StateMachine<FormState, FormEvent> formMachine) {
        if (text == null || text.trim().isEmpty()) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Номер телефона не может быть пустым. Попробуйте еще раз:")
                    .build();
        }

        saveFormField(chatId, "phone", text);
        formMachine.sendEvent(FormEvent.NEXT);
        return askForAge(chatId);
    }

    public BotApiMethod<?> processAge(Long chatId, String text, StateMachine<FormState, FormEvent> formMachine) {
        try {
            int age = Integer.parseInt(text);
            if (age < 14 || age > 100) {
                return SendMessage.builder()
                        .chatId(chatId.toString())
                        .text("❌ Возраст должен быть от 14 до 100 лет. Попробуйте еще раз:")
                        .build();
            }
            saveFormField(chatId, "age", age);
            formMachine.sendEvent(FormEvent.NEXT);
            return askForCity(chatId);
        } catch (NumberFormatException e) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Введите корректный возраст (число). Попробуйте еще раз:")
                    .build();
        }
    }

    public BotApiMethod<?> processCity(Long chatId, String text, StateMachine<FormState, FormEvent> formMachine) {
        if (text == null || text.trim().length() < 2) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Название города должно содержать минимум 2 символа. Попробуйте еще раз:")
                    .build();
        }

        saveFormField(chatId, "city", text);
        formMachine.sendEvent(FormEvent.NEXT);
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("✅ Форма заполнена! Нажмите 'Подтвердить' для сохранения.")
                .replyMarkup(createFormNavigationKeyboard())
                .build();
    }

    public BotApiMethod<?> processConfirmation(Long chatId, String text, StateMachine<FormState, FormEvent> formMachine) {
        if (CONFIRM.equals(text) || CONFIRM_TEXT.equals(text)) {
            formMachine.sendEvent(FormEvent.CONFIRM);
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("✅ Форма успешно сохранена!")
                    .build();
        } else if (CANCEL.equals(text) || CANCEL_TEXT.equals(text)) {
            formMachine.sendEvent(FormEvent.CANCEL);
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Заполнение формы отменено.")
                    .build();
        }
        return null;
    }
}