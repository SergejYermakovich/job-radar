package com.job.radar.service.handler;

import com.job.radar.model.enums.statemachine.event.ResumeEvent;
import com.job.radar.model.enums.statemachine.state.ResumeState;
import com.job.radar.service.ResumeService;
import com.job.radar.service.StateMachineManager;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.job.radar.utils.ButtonConsts.*;
import static com.job.radar.utils.FieldNames.EMAIL;
import static com.job.radar.utils.FieldNames.FULL_NAME;

@SuppressWarnings("deprecation")
@Service
public class ResumeFormHandler {

    private final StateMachineManager stateMachineManager;
    private final ResumeService resumeService;

    public ResumeFormHandler(StateMachineManager stateMachineManager,
                             ResumeService resumeService) {
        this.stateMachineManager = stateMachineManager;
        this.resumeService = resumeService;
    }

    public BotApiMethod<?> processResumeStep(Long chatId, String text) {
        StateMachine<ResumeState, ResumeEvent> resumeMachine = stateMachineManager.getResumeStateMachine(chatId);
        ResumeState currentState = resumeMachine.getState().getId();

        switch (currentState) {
            case RESUME_VIEW:
                if (CREATE_RESUME.equals(text)) {
                    resumeMachine.sendEvent(ResumeEvent.CREATE_RESUME);
                    return askForFullName(chatId);
                }
                break;
            case RESUME_CREATE:
                return processFullName(chatId, text);
            case RESUME_EDIT:
                return processEmail(chatId, text);
            case RESUME_COMPLETED:
                return processResumeConfirmation(chatId, text);
            default:
                break;
        }

        return null;
    }

    public BotApiMethod<?> askForFullName(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("👤 Введите ваше ФИО для резюме:")
                .build();
    }

    public BotApiMethod<?> processFullName(Long chatId, String text) {
        if (text == null || text.trim().length() < 2) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ ФИО должно содержать минимум 2 символа. Попробуйте еще раз:")
                    .build();
        }

        resumeService.createOrUpdate(chatId, FULL_NAME, text);
        StateMachine<ResumeState, ResumeEvent> resumeMachine = stateMachineManager.getResumeStateMachine(chatId);
        resumeMachine.sendEvent(ResumeEvent.COMPLETE);

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("✅ ФИО сохранено!")
                .build();
    }

    public BotApiMethod<?> processEmail(Long chatId, String text) {
        if (text == null || !text.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Неверный формат email. Попробуйте еще раз:")
                    .build();
        }

        resumeService.createOrUpdate(chatId, EMAIL, text);
        StateMachine<ResumeState, ResumeEvent> resumeMachine = stateMachineManager.getResumeStateMachine(chatId);
        resumeMachine.sendEvent(ResumeEvent.COMPLETE);

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("✅ Email сохранен!")
                .build();
    }

    public BotApiMethod<?> processResumeConfirmation(Long chatId, String text) {
        if (CONFIRM.equals(text) || CONFIRM_TEXT.equals(text)) {
            StateMachine<ResumeState, ResumeEvent> resumeMachine = stateMachineManager.getResumeStateMachine(chatId);
            resumeMachine.sendEvent(ResumeEvent.COMPLETE);
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("✅ Резюме успешно сохранено!")
                    .build();
        } else if (CANCEL.equals(text) || CANCEL_TEXT.equals(text)) {
            StateMachine<ResumeState, ResumeEvent> resumeMachine = stateMachineManager.getResumeStateMachine(chatId);
            resumeMachine.sendEvent(ResumeEvent.CANCEL);
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Создание резюме отменено.")
                    .build();
        }
        return null;
    }
}