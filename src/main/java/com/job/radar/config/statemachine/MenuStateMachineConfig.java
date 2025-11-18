package com.job.radar.config.statemachine;

import com.job.radar.model.enums.statemachine.event.MenuEvent;
import com.job.radar.model.enums.statemachine.state.MenuState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import static com.job.radar.model.enums.statemachine.event.MenuEvent.*;
import static com.job.radar.model.enums.statemachine.state.MenuState.*;
import static com.job.radar.model.enums.statemachine.state.MenuState.EXIT;

@Configuration
@EnableStateMachineFactory(name = "menuStateMachine")
public class MenuStateMachineConfig extends StateMachineConfigurerAdapter<MenuState, MenuEvent> {
    @Override
    public void configure(StateMachineStateConfigurer<MenuState, MenuEvent> states) throws Exception {
        states
                .withStates()
                .initial(MAIN_MENU)
                .state(RESUME_SECTION)
                .state(VACANCIES_SECTION)
                .state(SETTINGS_SECTION)
                .end(EXIT);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<MenuState, MenuEvent> transitions) throws Exception {
        transitions
                // Переходы между разделами
                .withExternal()
                .source(MAIN_MENU).target(RESUME_SECTION).event(OPEN_RESUME)
                .and()
                .withExternal()
                .source(MAIN_MENU).target(VACANCIES_SECTION).event(OPEN_VACANCIES)
                .and()
                .withExternal()
                .source(MAIN_MENU).target(SETTINGS_SECTION).event(OPEN_SETTINGS)
                .and()
                .withExternal()
                .and()

                // Возврат в главное меню
                .withExternal()
                .source(RESUME_SECTION).target(MAIN_MENU).event(BACK)
                .and()
                .withExternal()
                .source(VACANCIES_SECTION).target(MAIN_MENU).event(BACK)
                .and()
                .withExternal()
                .source(SETTINGS_SECTION).target(MAIN_MENU).event(BACK)
                .and()
                .withExternal()
                .and()

                // Выход из ЛЮБОГО состояния (кроме конечных)
                .withExternal()
                .source(MenuState.MAIN_MENU).target(MenuState.EXIT).event(MenuEvent.EXIT)
                .and()
                .withExternal()
                .source(MenuState.RESUME_SECTION).target(MenuState.EXIT).event(MenuEvent.EXIT)
                .and()
                .withExternal()
                .source(MenuState.VACANCIES_SECTION).target(MenuState.EXIT).event(MenuEvent.EXIT)
                .and()
                .withExternal()
                .source(MenuState.SETTINGS_SECTION).target(MenuState.EXIT).event(MenuEvent.EXIT);
    }
}
