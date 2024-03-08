package com.example.javaeasysobes.service;

import com.example.javaeasysobes.config.BotConfig;
import com.example.javaeasysobes.models.User;
import com.example.javaeasysobes.repo.QuestionRepository;
import com.example.javaeasysobes.repo.UserRepository;
import com.example.javaeasysobes.states.ChatState;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Component
public class JavaEasySobesBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    final BotConfig config;

    static final String HELP_TEXT = "This bot is created for fucking your mother!\n\n"
             +"There tou can see otner command of this bot, bitch:\n\n"
            +"Type /start to welcome message\n\n"
            +"Type /mydata to see data stored about yourself\n\n"
            +"Type /help to see this messge again, little idiot!";

    public JavaEasySobesBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommand = new ArrayList<>();
        listOfCommand.add(new BotCommand("/start", "get a welcom message"));
        listOfCommand.add(new BotCommand("/mydata", "get your data stored"));
        listOfCommand.add(new BotCommand("/deletedata", "delete my data"));
        listOfCommand.add(new BotCommand("/help", "info how to use this bot"));
        listOfCommand.add(new BotCommand("/settings", "change your settings"));
        try {
            this.execute(new SetMyCommands(listOfCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e){
            log.error("Error setting bot's command list: "  + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "Start":
                    registerUser(update.getMessage());
                    startCommandRecieved(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/start":
                    registerUser(update.getMessage());
                    startCommandRecieved(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "Help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "New":
                    newQuestion(chatId);
                    //newAnswer(update.getMessage().getText());
                    break;
                case "/question":
                    //sendQuestion(chatId, questionRepository);
                    break;
                default:
                    sendMessage(chatId,"Sorry, there is nothing");
            }
        }
    }
    //TODO:убрать нахуй менюшку
    private void newQuestion(long chatId) {
        sendMessage(chatId, "Введите ваш вопрос:");
        if (updateUserState(chatId, ChatState.NEW_QUESTION) == 1) {

            //TODO:тут должен бвть код для ожидания текта вопроса и добавления его в бд, спросить Вадима
            sendMessage(chatId, "Ваш вопрос удачно сохранен!");
        }
    }

    @Transactional
    public int updateUserState(Long chatId, ChatState newState) {
        Optional<User> userOptional = userRepository.findById(chatId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setState(newState);
            userRepository.save(user);
            return 1;
        }
        return 0;
    }
//    private void sendQuestion(long chatId, QuestionRepository questionRepository) {
//        int count = userCounter();
//        Random random = new Random();
//        int i = random.nextInt(count)
//    }


    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            Long chatId = message.getChatId();
            Chat chat = message.getChat();
            User user = new User();
            user.setId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            user.setState(ChatState.DEFAULT);
            userRepository.save(user);
            log.info("User saved: " + user);
        }
    }


    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();

        message.setChatId(String.valueOf((chatId)));
        message.setText(textToSend);

        printKeyboard(chatId, message);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occured:" + e.getMessage());
        }
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    private void startCommandRecieved(long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you";
        sendMessage(chatId, answer);
        log.info("Replied to user:" + name);
    }

    private void printKeyboard(long chatId, SendMessage message) {

        Optional<User> userOptional = userRepository.findById(chatId);
        if (userOptional.get().getState() == ChatState.DEFAULT) {
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRows = new ArrayList<>();

            KeyboardRow row = new KeyboardRow();
            row.add("Start");
            keyboardRows.add(row);

            row = new KeyboardRow();
            row.add("New");
            keyboardRows.add(row);

            row = new KeyboardRow();
            row.add("Help");
            keyboardRows.add(row);

            keyboardMarkup.setKeyboard(keyboardRows);

            message.setReplyMarkup(keyboardMarkup);
        }
    }

    private int userCounter() {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "1";
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            Statement statement = connection.createStatement();

            String query = "SELECT COUNT(*) AS count FROM user_table";

            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                return count;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
