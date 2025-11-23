package com.job.radar.service.processor.field;

import com.job.radar.model.enums.statemachine.event.FormEvent;
import com.job.radar.model.enums.statemachine.state.FormState;
import com.job.radar.service.AskService;
import com.job.radar.service.ResumeService;
import lombok.AllArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.job.radar.utils.FieldNames.PHONE;
@AllArgsConstructor
@Service
public class PhoneProcessor implements ResumeFieldProcessor {
    private final ResumeService resumeService;
    private final AskService askService;

    @Override
    public BotApiMethod<?> process(Long chatId, String text, StateMachine<FormState, FormEvent> formMachine) {
        if (text == null || text.trim().isEmpty()) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Номер телефона не может быть пустым. Попробуйте еще раз:")
                    .build();
        }

        resumeService.createOrUpdate(chatId, PHONE, text);
        formMachine.sendEvent(FormEvent.NEXT);
        return askService.askForAge(chatId);
    }
}
