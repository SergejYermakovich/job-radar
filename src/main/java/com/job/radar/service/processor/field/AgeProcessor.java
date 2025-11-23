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

import static com.job.radar.utils.FieldNames.AGE;

@AllArgsConstructor
@Service
public class AgeProcessor implements ResumeFieldProcessor {
    private final ResumeService resumeService;
    private final AskService askService;

    public BotApiMethod<?> process(Long chatId,
                                   String text,
                                   StateMachine<FormState, FormEvent> formMachine
    ) {
        try {
            int age = Integer.parseInt(text);
            if (age < 14 || age > 100) {
                return SendMessage.builder()
                        .chatId(chatId.toString())
                        .text("❌ Возраст должен быть от 14 до 100 лет. Попробуйте еще раз:")
                        .build();
            }
            resumeService.createOrUpdate(chatId, AGE, age);
            formMachine.sendEvent(FormEvent.NEXT);
            return askService.askForCity(chatId);
        } catch (NumberFormatException e) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Введите корректный возраст (число). Попробуйте еще раз:")
                    .build();
        }
    }
}
