package com.job.radar.service.processor.field;

import com.job.radar.model.enums.statemachine.event.FormEvent;
import com.job.radar.model.enums.statemachine.state.FormState;
import org.springframework.statemachine.StateMachine;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

public interface ResumeFieldProcessor {
    BotApiMethod<?>  process(Long chatId, String text, StateMachine<FormState, FormEvent> formMachine);
    FormState getCurrentState();
}
