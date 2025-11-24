package com.job.radar.service.processor.field;

import com.job.radar.model.enums.statemachine.event.FormEvent;
import com.job.radar.model.enums.statemachine.state.FormState;
import com.job.radar.service.AskService;
import com.job.radar.service.ResumeService;
import com.job.radar.utils.ResumeFieldValidators;
import lombok.AllArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.job.radar.utils.FieldNames.EMAIL;

@AllArgsConstructor
@Service
public class EmailProcessor implements ResumeFieldProcessor {
    private final ResumeService resumeService;
    private final AskService askService;

    public BotApiMethod<?> process(Long chatId,
                                         String text,
                                         StateMachine<FormState, FormEvent> formMachine) {
        if (!ResumeFieldValidators.isValidEmail(text)) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Неверный формат email. Попробуйте еще раз:")
                    .build();
        }

        resumeService.createOrUpdate(chatId, EMAIL, text);
        formMachine.sendEvent(FormEvent.NEXT);

        return askService.askForPhone(chatId);
    }

    @Override
    public FormState getCurrentState() {
        return FormState.ENTERING_EMAIL;
    }
}
