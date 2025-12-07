package com.job.radar.service;

import com.job.radar.model.enums.statemachine.state.FormState;
import com.job.radar.model.enums.statemachine.state.ResumeState;
import com.job.radar.service.handler.FormHandler;
import com.job.radar.service.handler.NavigationHandler;
import com.job.radar.service.handler.ResumeFormHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Consumer;

@Slf4j
@AllArgsConstructor
@Service
public class UpdateProcessorService {
    private final NavigationHandler navigationHandler;
    private final FormHandler formHandler;
    private final ResumeFormHandler resumeFormHandler;
    private final StateMachineManager stateMachineManager;

    public void processUpdate(Update update,
                              Consumer<BotApiMethod<?>> responseConsumer
    ) {
        try {
            BotApiMethod<?> response = process(update);
            if (response != null) {
                responseConsumer.accept(response);
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }

    public BotApiMethod<?> processUpdate(Update update) {
        return process(update);
    }

    private BotApiMethod<?> process(Update update) {
        // Обработка callback query
        if (update.hasCallbackQuery()) {
            return navigationHandler.handleCallbackQuery(update.getCallbackQuery());
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return null;
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        log.info("text update: {}", text);
        if (stateMachineManager.isInFormFlow(chatId)) {
            FormState formState = stateMachineManager.getCurrentFormState(chatId);
            log.info("isInFormFlow state: {}, chat = {}", formState, chatId);
            if (formState != null && formState != FormState.FORM_IDLE && formState != FormState.FORM_COMPLETED) {
                return formHandler.handleFormStep(chatId, text);
            }
        }

        // Check if user is in resume creation flow
        ResumeState resumeState = stateMachineManager.getCurrentResumeState(chatId);
        log.info("resume state: {}, chat = {}", resumeState, chatId);
        if (resumeState == ResumeState.RESUME_CREATE) {
            return resumeFormHandler.processResumeStep(chatId, text);
        }

        // Default: handle navigation
        return navigationHandler.handleUpdate(update);
    }
}
