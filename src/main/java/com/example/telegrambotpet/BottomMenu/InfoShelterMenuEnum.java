package com.example.telegrambotpet.BottomMenu;

import lombok.Getter;

import static com.example.telegrambotpet.constants.BotConstants.*;

public enum InfoShelterMenuEnum {

    /*
    Инфо меню STEP_1
    Меню в которое попадаешь после нажатия 1 кнопки в главном меню STEP_0 (Информация о приюте)
     */
    INFO_BOTTOM_1(INFO_SHELTER_CMD_2_MENU, "Узнать о приюте"),
    INFO_BOTTOM_2(ADDRESS_CMD, "Расписание работы, как нас найти"),
    INFO_BOTTOM_3(RULES_CMD, "Технике безопасности на территории"),
    INFO_BOTTOM_4(CALL_VOLUNTEER_CMD, "Позвать волонтера"),
    INFO_BOTTOM_5(CALL_BACK_CMD, "Заказать обратный звонок");
    @Getter
    private final String commandInfo;

    @Getter
    private final String buttonNameInfo;


    InfoShelterMenuEnum(String commandInfo, String buttonNameInfo) {
        this.commandInfo = commandInfo;
        this.buttonNameInfo = buttonNameInfo;
    }

}
