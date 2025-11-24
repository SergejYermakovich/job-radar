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

import static com.job.radar.utils.FieldNames.FULL_NAME;

@AllArgsConstructor
@Service
public class FullNameProcessor implements ResumeFieldProcessor {
    private final ResumeService resumeService;
    private final AskService askService;

    @Override
    public BotApiMethod<?> process(Long chatId, String text,
                                            StateMachine<FormState, FormEvent> formMachine) {
        if (text.length() < 2) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ ФИО должно содержать минимум 2 символа. Попробуйте еще раз:")
                    .build();
        }

        // Сохраняем значение (временное хранение)
        resumeService.createOrUpdate(chatId, FULL_NAME, text);

        // Переходим к следующему шагу
        formMachine.sendEvent(FormEvent.NEXT);

        // Запрашиваем следующий вопрос
        return askService.askForEmail(chatId);
    }

    @Override
    public FormState getCurrentState() {
        return FormState.ENTERING_FULL_NAME;
    }
}
