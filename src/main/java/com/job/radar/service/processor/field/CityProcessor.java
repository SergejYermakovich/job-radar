package com.job.radar.service.processor.field;

import com.job.radar.model.enums.statemachine.event.FormEvent;
import com.job.radar.model.enums.statemachine.state.FormState;
import com.job.radar.service.AskService;
import com.job.radar.service.KeyboardService;
import com.job.radar.service.ResumeService;
import lombok.AllArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.job.radar.utils.FieldNames.CITY;

@AllArgsConstructor
@Service
public class CityProcessor implements ResumeFieldProcessor {
    private final ResumeService resumeService;
    private final KeyboardService keyboardService;

    public BotApiMethod<?> process(Long chatId,
                                       String text,
                                       StateMachine<FormState, FormEvent> formMachine) {
        if (text == null || text.trim().length() < 2) {
            return SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("❌ Название города должно содержать минимум 2 символа. Попробуйте еще раз:")
                    .build();
        }

        resumeService.createOrUpdate(chatId, CITY, text);
        formMachine.sendEvent(FormEvent.NEXT);
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("✅ Форма заполнена! Нажмите 'Подтвердить' для сохранения.")
                .replyMarkup(keyboardService.createFormNavigationKeyboard())
                .build();
    }

    @Override
    public FormState getCurrentState() {
        return FormState.ENTERING_CITY;
    }
}
