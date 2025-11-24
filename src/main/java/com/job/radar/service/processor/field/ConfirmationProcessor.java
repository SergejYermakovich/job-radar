package com.job.radar.service.processor.field;

import com.job.radar.model.enums.statemachine.event.FormEvent;
import com.job.radar.model.enums.statemachine.state.FormState;
import lombok.AllArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.job.radar.utils.ButtonConsts.*;
import static com.job.radar.utils.ButtonConsts.CANCEL_TEXT;

@AllArgsConstructor
@Service
public class ConfirmationProcessor implements ResumeFieldProcessor {

    public BotApiMethod<?> process(Long chatId,
                                   String text,
                                   StateMachine<FormState, FormEvent> formMachine) {
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

    @Override
    public FormState getCurrentState() {
        return FormState.CONFIRMING_FORM;
    }
}
