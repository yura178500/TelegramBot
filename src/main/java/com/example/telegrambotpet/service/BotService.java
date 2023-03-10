package com.example.telegrambotpet.service;
import com.example.telegrambotpet.InlineKeyboardMaker;
import com.example.telegrambotpet.configuration.BotConfiguration;
import com.example.telegrambotpet.model.User;
import com.example.telegrambotpet.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static com.example.telegrambotpet.constants.BotConstants.*;


@Slf4j
@Component
public class BotService extends TelegramLongPollingBot {

    final BotConfiguration botConfiguration;

    private final InlineKeyboardMaker inlineKeyboardMaker;

    private final UserRepository userRepository;

    /**
     * В этот конструктор можно вписать команды, которые будут открываться при нажатии кнопки меню.
     * Эта кнопка меню общая, доступна из любых разделов программы
     *
     * @param botConfiguration    DI  конфигуратор
     * @param inlineKeyboardMaker DI создание меню
     * @param userRepository
     */
    public BotService(BotConfiguration botConfiguration, InlineKeyboardMaker inlineKeyboardMaker, UserRepository userRepository) {
        this.botConfiguration = botConfiguration;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.userRepository = userRepository;
        //Создание кнопки меню
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Запуск"));
        listOfCommands.add(new BotCommand("/data", "Мои данные"));
        listOfCommands.add(new BotCommand("/deletedata", "Удалить мои данные"));
        listOfCommands.add(new BotCommand("/help", "Справка"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Command list error");
        }
    }

    @Override
    public String getBotUsername() {
        return botConfiguration.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfiguration.getToken();
    }

    /**
     * Метод для реагирования на команды
     * Вызывает методы:
     * {@link #startCommandReceived(long, String)}
     * {@link #sendMessage(long, String)} )}
     * {@link #sendMenuTakeHomeDog(long, String)}
     * {@link #sendMenuIfo(long, String)}
     * @param update сообщения от пользователя (обновления)
     */
    // messageText (текстовые команды)
    // messageData (команды кнопок)
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case INITIAL_CMD:
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    var textMessage = update.getMessage();
                    var user = textMessage.getFrom();
                    var appUser = findOrSaveUser(user);
                    break;
                default:
                    sendMessage(chatId, "Sorry, no such command");
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            String messageData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            switch (messageData) {
                case TAKE_HOME_MENU_DOG_CMD:
                    sendMenuTakeHomeDog(chatId, "Что вас интересует?");
                    break;
                case TAKE_HOME_MENU_CAT_CMD:
                    sendMenuTakeHomeCat(chatId, "Что вас интересует?");
                    break;
                case FINAL_CMD:
                    endCommandReceived(chatId, "тест");
                    break;
                case INFO_SHELTER_CMD:
                    sendMenuIfo(chatId, "Что вас интересует?");
                    break;
                case INFO_SHELTER_CMD_2_MENU:
                    sendMessage(chatId, SHELTER_INFO);
                    break;
                case ADDRESS_CMD:
                    sendMessage(chatId,SHELTER_ADDRESS);
                    break;
                case RULES_CMD:
                    sendMessage(chatId,SHELTER_RULES);
                    break;
                case CALL_VOLUNTEER_CMD:
                    sendMessage(chatId, VOLUNTEER_CALL);
                    break;
                case CALL_BACK_CMD:
                    processUpdate(chatId, update);
                    break;
                //передача инфо на разные кнопки меню по собакам
                case SOCIAL_DOG_CMD:
                    sendMessage(chatId, SOCIAL_DOG);
                    break;
                case DOCUMENTATION_DOG_CMD:
                    sendMessage(chatId, DOCUMENTATION_DOG);
                    break;
                case TRANSPORTATION_DOG_CMD:
                    sendMessage(chatId, TRANSPORTATION_DOG);
                    break;
                case COMFORT_PUPPY_DOG_CMD:
                    sendMessage(chatId, COMFORT_PUPPY_DOG);
                    break;
                case COMFORT_DOG_CMD:
                    sendMessage(chatId, COMFORT_DOG);
                    break;
                case COMFORT_INV_DOG_CMD:
                    sendMessage(chatId,COMFORT_INV_DOG );
                    break;
                case KINOLOG_ADVICE_CMD:
                    sendMessage(chatId,KINOLOG_ADVICE );
                    break;
                case COMPILATION_KINOLOG_CMD:
                    sendMessage(chatId,COMPILATION_KINOLOG );
                    break;
                case WHY_DISCLAIMER_CMD:
                    sendMessage(chatId,WHY_DISCLAIMER );
                    break;
                //передачи инфо по кнопкам меню кошки
                case SOCIAL_CAT_CMD:
                    sendMessage(chatId, SOCIAL_CAT);
                    break;
                case DOCUMENTATION_CAT_CMD:
                    sendMessage(chatId,DOCUMENTATION_CAT );
                    break;
                case TRANSPORTATION_CAT_CMD:
                    sendMessage(chatId,TRANSPORTATION_CAT );
                    break;
                case COMFORT_CAT_CMD:
                    sendMessage(chatId,COMFORT_CAT );
                    break;

                default:
                    sendMessage(chatId, "Sorry, no such Bottom");
                    break;
            }

        }
    }
    /**
     * Метод отправки стартового сообщения
     * Вызывает метод отправки меню STEP_0
     * @see #sendStartMenu(long, String)
     * @param chatId чат ID
     * @param name имя пользователя
     */
    private void startCommandReceived(long chatId, String name) {
        String answer = name + GREETING_MSG;
        log.info("Start to user: " + name);
        sendStartMenu(chatId, answer);
    }
    /**
     * Метод отправки стартового меню STEP_0
     * Вызывает метод создания меню STEP_0
     * {@link InlineKeyboardMaker#getInlineMessageButtons()}
     * @param chatId чат ID
     * @param textToSend сообщение
     */
    private void sendStartMenu(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(inlineKeyboardMaker.getInlineMessageButtons());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    /**
     * Метод отправки инфо-меню STEP_1
     * Вызывает метод создаия меню STEP_1
     * {@link InlineKeyboardMaker#infoShelterMenu()}
     * @param chatId чат ID
     * @param textToSend сообщение
     */
    private void sendMenuIfo(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(inlineKeyboardMaker.infoShelterMenu());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    /**
     * Метод отправки инфо-меню STEP_2
     * Вызывает метод создаия меню STEP_2
     * {@link InlineKeyboardMaker#animalHomeMenuDog()}
     * @param chatId чат ID
     * @param textToSend сообщение
     */
    private void sendMenuTakeHomeDog(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(inlineKeyboardMaker.animalHomeMenuDog());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
    private void sendMenuTakeHomeCat(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(inlineKeyboardMaker.animalHomeMenuCat());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    // Тестовый метод создания меню 3 мапой
    private void endCommandReceived(long chatId, String textToSend){
        HashMap<String,String> menuStep3 = new HashMap<>();
        menuStep3.put("Отправить отчет", "SEND_REPORT_CMD");
        menuStep3.put("Отправить отчет2", "SEND_REPORT_CMD2");
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(inlineKeyboardMaker.getInlineMessageButtonsByMap(menuStep3));
        try {
            execute(message);
        }
        catch (TelegramApiException e){
            log.error("Error occurred: " + e.getMessage());
        }
        log.info("Replied to user: ");
        sendMessage(chatId, " Скоро перезвоню");

    }

    /**
     * Метод отправки сообщений
     * @param chatId чат ID
     * @param textToSend сообщение
     */
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    /**
     Метод для добавления нового юзера в базу данных
     * @param user
     * @return
     */
    private User findOrSaveUser(org.telegram.telegrambots.meta.api.objects.User user){
        User persistentUser = userRepository.findUserByChatId(user.getId());
        if (persistentUser==null){
            User transientUser= new User();
            transientUser.setChatId(user.getId());
            transientUser.setName(user.getFirstName()+user.getLastName());
            transientUser.setUserName(user.getUserName());
            transientUser.setIsActive(true);
            transientUser.setTelephone_number(user.getSupportInlineQueries().toString());
            return userRepository.save(transientUser);
        }
        return persistentUser;
    }
    private void processUpdate(Long chatId, Update update) {
        String userMessage = update.getMessage().getText();
        String[] userMessages = userMessage.split(" ");
        User user = new User();
        user.setName(userMessages[0]);
        user.setTelephone_number(userMessages[1]);
        userRepository.save(user);

        sendMessage(chatId, "Данные успешно записаны");
    }
}