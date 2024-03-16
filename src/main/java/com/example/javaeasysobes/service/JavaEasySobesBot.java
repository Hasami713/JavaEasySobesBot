package com.example.javaeasysobes.service;

import com.example.javaeasysobes.config.BotConfig;
import com.example.javaeasysobes.models.Answer;
import com.example.javaeasysobes.models.Question;
import com.example.javaeasysobes.models.User;
import com.example.javaeasysobes.repo.AnswerRepository;
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

import javax.swing.event.AncestorEvent;
import java.lang.ref.ReferenceQueue;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.example.javaeasysobes.states.ChatState.*;

@Slf4j
@Component
public class JavaEasySobesBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    final BotConfig config;

    static final String HELP_TEXT = "This bot is created for fucking your mother!\n\n"
            + "There tou can see otner command of this bot, bitch:\n\n"
            + "Type /start to welcome message\n\n"
            + "Type /mydata to see data stored about yourself\n\n"
            + "Type /help to see this messge again, little idiot!";

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
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            registerUser(update.getMessage());
            Optional<User> userOptional = userRepository.findById(chatId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                ChatState state = user.getState();

                switch (state) {
                    case DEFAULT:
                        handleDefaultState(chatId, messageText, update, user);
                        break;
                    case NEW_QUESTION:
                        handleNewQuestionState(chatId, messageText, user);
                        break;
                    case NEW_ANSWER:
                        handleNewAnswerState(chatId, messageText, user);
                        saveNewTask(chatId, user);
                        break;
                    case SENDED_QUESTION:
                        sendAnswer(chatId, user);
                        break;
                    default:
                        sendMessage(chatId, "Unknown state");
                        break;
                }
            } else {
                sendMessage(chatId, "User not found");
            }
        }
    }


    private void handleDefaultState(long chatId, String messageText, Update update, User user) {
        switch (messageText) {
            case "Start":
            case "/start":
                registerUser(update.getMessage());
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                break;
            case "/help":
            case "Help":
                sendMessage(chatId, HELP_TEXT);
                break;
            case "/new":
            case "New":
                //TODO:подумать, как можно получше сделать отправку сообщений о вопросе и ответе
                sendMessage(chatId, "Введите текст вашего вопроса:");
                updateUserState(chatId, NEW_QUESTION);
                break;
            case "Send new question":
                sendQuestion(chatId, user);
                break;
            default:
                sendMessage(chatId, "Sorry, there is nothing");
                break;
        }
    }

    //TODO:убрать нахуй менюшку

    private void handleNewQuestionState(long chatId, String messageText, User user) {
        user.setNewQuestion(messageText);
        sendMessage(chatId, "Ваш вопрос успешно сохранен!");
        sendMessage(chatId, "Введите текст вашего ответа: ");
        userRepository.save(user);
        updateUserState(chatId, NEW_ANSWER);
    }

    private void handleNewAnswerState(long chatId, String messageText, User user) {
        user.setNewAnswer(messageText);
        sendMessage(chatId, "Ваш ответ успешно сохранен!");
        userRepository.save(user);

    }

    private void saveNewTask(long chatId, User user) {
        Question question = new Question();
        question.setQuestionText(user.getNewQuestion());
        Answer answer = new Answer();
        answer.setAnswerText(user.getNewAnswer());
        answer.setQuestion(question);
        answerRepository.save(answer);
        updateUserState(chatId, DEFAULT);
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

    private void sendQuestion(long chatId, User user) {
        int count = questionCounter();
        Random random = new Random();
        long i = random.nextInt(count) + 1;
        Optional<Question> questionOptional = questionRepository.findById(i);
        if (questionOptional.isPresent()) {
            Question question = questionOptional.get();
            sendMessage(chatId, question.getQuestionText());
            user.setCurrentQuestionId(i);
            userRepository.save(user);
        }
        updateUserState(chatId, DEFAULT);
    }

    private void sendAnswer(long chatId, User user) {

    }

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
            user.setState(DEFAULT);
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

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you";
        sendMessage(chatId, answer);
        log.info("Replied to user:" + name);
    }

    private void printKeyboard(long chatId, SendMessage message) {

        Optional<User> userOptional = userRepository.findById(chatId);
        if (userOptional.isPresent() && userOptional.get().getState() == DEFAULT) {
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

            row = new KeyboardRow();
            row.add("Send new question");
            keyboardRows.add(row);

            keyboardMarkup.setKeyboard(keyboardRows);

            message.setReplyMarkup(keyboardMarkup);
        }
    }

    private int questionCounter() {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "1";
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            Statement statement = connection.createStatement();

            String query = "SELECT COUNT(*) AS count FROM question";

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
