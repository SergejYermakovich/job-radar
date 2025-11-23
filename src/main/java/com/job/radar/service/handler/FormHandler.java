package com.job.radar.service.handler;

import com.job.radar.model.enums.statemachine.event.FormEvent;
import com.job.radar.model.enums.statemachine.state.FormState;
import com.job.radar.service.AskService;
import com.job.radar.service.KeyboardService;
import com.job.radar.service.ResumeService;
import com.job.radar.service.StateMachineManager;
import com.job.radar.utils.ResumeFieldValidators;
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
import static com.job.radar.utils.FieldNames.*;

@Slf4j
@SuppressWarnings("deprecation")
@Service
public class FormHandler {
    private final StateMachineManager stateMachineManager;
    private final AskService askService;

    public FormHandler(StateMachineManager stateMachineManager,
                       AskService askService) {
        this.stateMachineManager = stateMachineManager;
        this.askService = askService;
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

    public BotApiMethod<?> askNextQuestion(Long chatId) {
        StateMachine<FormState, FormEvent> formMachine = stateMachineManager.getFormStateMachine(chatId);
        FormState currentState = formMachine.getState().getId();

        return switch (currentState) {
            case ENTERING_FULL_NAME -> askService.askForFullName(chatId);
            case ENTERING_EMAIL -> askService.askForEmail(chatId);
            case ENTERING_PHONE -> askService.askForPhone(chatId);
            case ENTERING_AGE -> askService.askForAge(chatId);
            case ENTERING_CITY -> askService.askForCity(chatId);
            default -> null;
        };
    }

    public BotApiMethod<?> askPreviousQuestion(Long chatId) {
        StateMachine<FormState, FormEvent> formMachine = stateMachineManager.getFormStateMachine(chatId);
        FormState currentState = formMachine.getState().getId();

        return switch (currentState) {
            case ENTERING_EMAIL -> askService.askForFullName(chatId);
            case ENTERING_PHONE -> askService.askForEmail(chatId);
            case ENTERING_AGE -> askService.askForPhone(chatId);
            case ENTERING_CITY -> askService.askForAge(chatId);
            default -> null;
        };
    }
}