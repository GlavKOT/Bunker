package ru.glkot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Proffs {

    public static Map<Long, Message> chosers;

    public static void logoped (Player player) {
        playerChoser(player);
    }

    public static List<String> choserProfs () {
        return List.of("Логопед","Врач","Геронтолог","Иллюстратор","Инструктор","Нарколог","Хирург","Уборщик","Диетолог");
    }

    public static void playerChoser(Player player) {

        if (chosers == null) chosers = new HashMap<>();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(player.id);
        sendMessage.setText("Выберите игрока");
        Session session = Session.get(player.session);
        List<List<InlineKeyboardButton>> listList = new ArrayList<>();
        player.choser = true;
        for (long l : session.getPlayers()) {
            Player p = Player.get(l);
            {
                List<InlineKeyboardButton> list = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(p.getFullName());
                button.setCallbackData("prof|"+l);
                list.add(button);
                listList.add(list);
            }
        }
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(listList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        try {
            chosers.put(player.id, Bot.get.execute(sendMessage));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    public static void playerNotMeChoser(Player player) {

        if (chosers == null) chosers = new HashMap<>();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(player.id);
        sendMessage.setText("Выберите игрока");
        Session session = Session.get(player.session);
        List<List<InlineKeyboardButton>> listList = new ArrayList<>();
        player.choser = true;
        for (long l : session.getPlayers()) {
            Player p = Player.get(l);
            if (p.id != player.id)
            {
                List<InlineKeyboardButton> list = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(p.getFullName());
                button.setCallbackData("prof|"+l);
                list.add(button);
                listList.add(list);
            }
        }
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(listList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        try {
            chosers.put(player.id, Bot.get.execute(sendMessage));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public static void kidChoser(Player player) {

        if (chosers == null) chosers = new HashMap<>();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(player.id);
        sendMessage.setText("Выберите игрока");
        Session session = Session.get(player.session);
        List<List<InlineKeyboardButton>> listList = new ArrayList<>();
        player.choser = true;
        for (long l : session.getPlayers()) {
            Player p = Player.get(l);

            if (!player.getProp("Возраст").equals("-")) {

                if (Integer.parseInt(p.getProp("Возраст")) < 18) {
                    List<InlineKeyboardButton> list = new ArrayList<>();
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(p.getFullName());
                    button.setCallbackData("prof|" + l);
                    list.add(button);
                    listList.add(list);
                }
            }
        }
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(listList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        try {
            chosers.put(player.id, Bot.get.execute(sendMessage));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
