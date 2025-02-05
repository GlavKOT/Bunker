package ru.glkot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Pages {
    User user;
    long chatId;
    Bot bot = Bot.get;
    Player player;

    public Pages (User user, long chatId) {
        this.user = user;
        this.chatId = chatId;
        this.player = Player.get(user);
    }


    public void menu() {
        SendPhoto sendMessage = new SendPhoto();
        sendMessage.setChatId(chatId);
        sendMessage.setCaption("Меню");
        sendMessage.setPhoto(new InputFile(Path.of("main.png").toFile()));
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        {
            KeyboardRow keyboardButtons = new KeyboardRow();
            keyboardButtons.add("Создать");
            keyboardButtons.add("Войти");
            keyboardRows.add(keyboardButtons);
        }
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);

        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            player.messages.add(bot.execute(sendMessage).getMessageId());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
