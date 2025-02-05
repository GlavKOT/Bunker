package ru.glkot;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.StopPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramBot;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

import static java.awt.SystemColor.text;

@Component
public class Bot extends TelegramLongPollingBot {

    static Bot get;
    static Map<String,Message> polls;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.pass}")
    private String password;

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Value("${bot.username}")
    private String botUsername;

    @Override
    public void onUpdateReceived(Update update) {
        get = this;
        if (polls == null) polls = new HashMap<>();
        if (update.hasMessage()) {


            String text = update.getMessage().getText();

            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(update.getMessage().getChatId());
            deleteMessage.setMessageId(update.getMessage().getMessageId());
            try {
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                System.out.println(new RuntimeException(e));
            }

            if (text.equals(password)) System.exit(228);

            Player player = Player.get(update.getMessage().getFrom());
            player.deleteMessages();
            Pages pages = new Pages(update.getMessage().getFrom(), update.getMessage().getChatId());
            boolean b = true;
            System.out.println(text);


            if ((Boolean) player.data.get("getter")) {
                b = false;
                join(text, player);
                player.data.put("getter", false);
            }


            if (text.contains("/start")) {
                String[] s = text.split(" ");
                if (s.length > 1) {
                    join(s[1], player);
                    b = false;
                }
            }

            switch (update.getMessage().getText()) {
                case "Создать" -> {
                    Session session = new Session();
                    session.addPlayer(player);
                    session.admin = player;
                    String url = "https://t.me/glkotbunkerbot?start=" + session.id;
                    {
                        QRCodeWriter qrCodeWriter = new QRCodeWriter();
                        BitMatrix bitMatrix = null;

                        try {
                            bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 1024, 1024);
                        } catch (WriterException e) {
                            throw new RuntimeException(e);
                        }

                        Path path = FileSystems.getDefault().getPath("qr.png");
                        try {
                            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    {
                        SendPhoto sendPhoto = new SendPhoto();
                        sendPhoto.setChatId(player.id);
                        Scene scene = session.scene;
                        sendPhoto.setCaption("Создана новая сессия. \n\n" +
                                "            id для копирования\n<b><code>" + session.id + "</code></b>\n\n" + "<a href=\"" + url + "\">ССЫЛКА-ПРИГЛАШЕНИЕ</a>\n\nПредыстория:\n<b>"+scene.name+"</b>\n\n"+scene.description);
                        sendPhoto.setParseMode("HTML");
                        sendPhoto.setPhoto(new InputFile(Path.of("qr.png").toFile()));
                        List<List<InlineKeyboardButton>> listListButtons = new ArrayList<>();

                        {
                            List<InlineKeyboardButton> buttons = new ArrayList<>();
                            {
                                InlineKeyboardButton button = new InlineKeyboardButton();
                                button.setText("Начать игру");
                                button.setCallbackData("startGame");
                                buttons.add(button);
                            }
                            listListButtons.add(buttons);
                        }


                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        inlineKeyboardMarkup.setKeyboard(listListButtons);
                        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
                        try {
                            Message message = execute(sendPhoto);
                            session.data.put("creatingMessage", message);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                case "Войти" -> {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("Введите ID формата\n123e4567-e89b-12d3-a456-426655440000\n");
                    sendMessage.setChatId(player.id);
                    try {
                        player.messages.add(execute(sendMessage).getMessageId());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    player.data.put("getter", true);
                }
                default -> {
                    if (b) {
                        pages.menu();
                    }
                }
            }
        }
        if (update.hasCallbackQuery()) {

            String text = update.getCallbackQuery().getData();
            Player player = Player.get(update.getCallbackQuery().getFrom());
            player.deleteMessages();
            Pages pages = new Pages(update.getCallbackQuery().getFrom(), update.getCallbackQuery().getMessage().getChatId());
            System.out.println(text);

            switch (text) {
                case "killSession" -> {
                    Session session = Session.get(player.session);
                    session.tableMes.addAll(session.sceneMes);
                    for (Message m: session.tableMes) {
                        DeleteMessage deleteMessage = new DeleteMessage();
                        deleteMessage.setChatId(m.getChatId());
                        deleteMessage.setMessageId(m.getMessageId());
                        try {
                            Bot.get.execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(player.id);
                    deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }

                    for (long l : session.getPlayers()) {
                        Player player1 = Player.get(l);
                        if (player1.winner || player1.kicked) {
                            Player.players.remove(player1.id);
                        }
                    }
                }
                case "startGame" -> {
                    Session session = Session.get(player.session);

                    for (long l : session.getPlayers()){
                        SendPhoto sendScene = new SendPhoto();
                        sendScene.setCaption("<b>" + session.scene.name + "</b>\n\n" + session.scene.description);
                        sendScene.setPhoto(new InputFile(session.scene.picture));
                        sendScene.setChatId(l);
                        sendScene.setParseMode("HTML");
                        try {
                            session.sceneMes.add(execute(sendScene));
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }


                    player.stepper = true;

                    for (long l : session.getPlayers()) {
                        Player p = Player.get(l);
                        p.deleteMessages();
                        p.randomiseProps();
                        p.kicked = false;
                        p.flush();
                    }
                    session.updateTable();
                    Message message = (Message) session.data.get("creatingMessage");
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(message.getChatId());
                    deleteMessage.setMessageId(message.getMessageId());
                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (text.contains("prop|")) {
                Session session = Session.get(player.session);
                if (session.hisStep(player) && !session.onPoll) {
                    String[] s = text.split("\\|");
                    if (!player.getProp(s[1]).equals(player.getOnProp(s[1]))) {
                        player.openProp(s[1]);
                        session.updateTable();
                    }
                }
            }

            if (text.contains("prof")) {
                if (text.equals("prof")) {
                    if (Proffs.choserProfs().contains(player.prof)) {
                        Proffs.playerChoser(player);
                    } else if (player.prof.contains("Вор")) {
                        Proffs.playerNotMeChoser(player);
                    } else if (player.prof.contains("Воспитатель")) {
                        Proffs.kidChoser(player);
                    }else if (player.prof.contains("Юрист")) {

                        Session session = Session.get(player.session);
                        if (session.onPoll) {
                            session.onPoll = false;
                            List<String> list = new ArrayList<>();

                            for (String s : polls.keySet()) {

                                Message message1 = polls.get(s);

                                if (session.getPlayers().contains(message1.getChatId())) {
                                    DeleteMessage deleteMessage = new DeleteMessage();
                                    deleteMessage.setChatId(message1.getChatId());
                                    deleteMessage.setMessageId(message1.getMessageId());
                                    try {
                                        execute(deleteMessage);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }

                                    list.add(s);

                                }
                            }

                            list.forEach(s -> polls.remove(s));
                        }
                    } else  {

                        if (!player.getOnProp("Профессия").contains("Адвокат")) {
                            System.out.println(player.prof);
                            player.prof = "used";
                        }
                    }

                    if (player.profdata.contains("+ ")) {
                        player.addToProp("Инвентарь",player.getOnProp("Профессия").substring(player.getOnProp("Профессия").indexOf("+ ")+2,player.getOnProp("Профессия").length()-2));
                        Session session = Session.get(player.session);
                        session.sendTable();
                        player.profdata = "used";
                    }

                } else {
                    String[] s = text.split("\\|");
                    switch (player.prof) {
                        case "Логопед" -> {
                            Player p = Player.get(Long.parseLong(s[1]));
                            p.addToProp("Здоровье","Картавость");
                            Session.get(p.session).sendTable();
                        }
                        case "Врач" -> {
                            Player p = Player.get(Long.parseLong(s[1]));
                            p.setProp("Здоровье","Идеальное здоровье (Состояние полного физического, психического и социального благополучия; Не смертельно)");
                            Session.get(p.session).sendTable();
                        }
                        case "Геронтолог" -> {
                            Player p = Player.get(Long.parseLong(s[1]));
                            int v = Integer.parseInt(p.getOnProp("Возраст"));
                            p.setProp("Возраст", String.valueOf(v-10));
                            Session.get(p.session).sendTable();
                        }
                        case "Иллюстратор" -> {
                            Player p = Player.get(Long.parseLong(s[1]));
                            p.setProp("Фобия",Prop.random("Фобия"));
                            Session.get(p.session).sendTable();
                        }
                        case "Инструктор" -> {
                            Player p = Player.get(Long.parseLong(s[1]));
                            p.setProp("Телосложение","Крепкое");
                            Session.get(p.session).sendTable();
                        }
                        case "Нарколог" -> {
                            Player p = Player.get(Long.parseLong(s[1]));
                            if (p.getOnProp("Здоровье").contains("Зависимость от")) {
                                p.setProp("Здоровье","Идеальное здоровье (Состояние полного физического, психического и социального благополучия; Не смертельно)");
                            }
                            Session.get(p.session).sendTable();
                        }
                        case "Хирург" -> {
                            Player p = Player.get(Long.parseLong(s[1]));
                            if (p.getOnProp("Пол").equals("М")) {
                                p.setProp("Пол","Ж");
                            } else p.setProp("Пол","М");
                            Session.get(p.session).sendTable();
                        }
                        case "Уборщик" -> {
                            Player p = Player.get(Long.parseLong(s[1]));
                            p.setProp("Инвентарь","Пусто");
                            Session.get(p.session).sendTable();
                        }
                        case "Вор" -> {
                            Player p = Player.get(Long.parseLong(s[1]));
                            player.addToProp("Инвентарь",p.getOnProp("Инвентарь"));
                            p.setProp("Инвентарь","Пусто");
                            Session.get(p.session).sendTable();
                        }
                        case "Диетолог" -> {
                            Player p = Player.get(Long.parseLong(s[1]));
                            switch (p.getOnProp("Телосложение")) {
                                case "Скуф", "Крепкое", "Полное" -> p.setProp("Телосложение","Обычное");
                                case "Обычное" -> p.setProp("Телосложение","Хлипкое");
                                case "Хлипкое" -> p.setProp("Телосложение","Анорексия");
                            }
                            Session.get(p.session).sendTable();
                        }
                        case "Воспитатель" -> {
                            Player p = Player.get(Long.parseLong(s[1]));
                            System.out.println("Сосет хуй "+ p.getFullName());
                            Session.get(p.session).sendTable();
                        }
                    }

                    player.prof = "used";

                    {
                        try {
                            DeleteMessage deleteMessage = new DeleteMessage();
                            deleteMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
                            deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        // Обработка ответа на голосование
        if (update.hasPoll()) {
            Message message = polls.get(update.getPoll().getId());
            Player player = Player.get(message.getChatId());

            if (!player.polled) {

                player.polled = true;

                StopPoll stopPoll = new StopPoll();
                stopPoll.setMessageId(message.getMessageId());
                stopPoll.setChatId(message.getChatId());
                try {
                    execute(stopPoll);
                } catch (TelegramApiException e) {
                    System.out.println(new RuntimeException(e));
                }

                System.out.println(player.getFullName() + " " + update.getPoll().getOptions());
                String answer = "Пропуск";
                for (PollOption p : update.getPoll().getOptions()) {
                    if (p.getVoterCount() >= 1) answer = p.getText();
                }
                Session session = Session.get(player.session);
                if (session.poll.containsKey(answer)) {
                    session.poll.put(answer, session.poll.get(answer) + 1);
                    if (player.getOnProp("Профессия").contains("Адвокат")) {
                        session.poll.put(answer, session.poll.get(answer) + 1);
                    }

                } else {
                    session.poll.put(answer, 1);
                    if (player.getOnProp("Профессия").contains("Адвокат")) session.poll.put(answer,2);
                }






                int count = 0;
                for (int i : session.poll.values()) {
                    count += i;
                }
                int d = 0;

                for (long l : session.getPlayers().stream().filter(l -> !Player.get(l).kicked).toList()) {
                    if (Player.get(l).getOnProp("Профессия").contains("Адвокат")) {
                        d++;
                        System.out.println(Player.get(l).getFullName() + "ADVOKAT");
                    }
                    System.out.println(player.getFullName());
                    System.out.println(player.getOnProp("Профессия"));
                }


                System.out.println(count);
                System.out.println(session.getPlayers().stream().filter(l -> !Player.get(l).kicked).toList().size() + d);
                if (count >= (session.getPlayers().stream().filter(l -> !Player.get(l).kicked).toList().size() + d)) {
                    session.onPoll = false;
                    List<String> list = new ArrayList<>();

                    for (String s : polls.keySet()) {

                        Message message1 = polls.get(s);

                        if (session.getPlayers().contains(message1.getChatId())) {
                            DeleteMessage deleteMessage = new DeleteMessage();
                            deleteMessage.setChatId(message1.getChatId());
                            deleteMessage.setMessageId(message1.getMessageId());
                            try {
                                execute(deleteMessage);
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }

                            list.add(s);

                        }
                    }

                    list.forEach(s -> polls.remove(s));

                    List<String> res = getWinningOptions(session.poll);
                    System.out.println("dawdawdawd" + res);

                    for (Long l : session.getPlayers()) {
                        Player p = Player.get(l);
                        for (String s : res) {
                            if (p.getFullName().equals(s)) {
                                p.kicked = true;
                                p.openAllProps();
                                p.flush();
                            }
                        }
                    }
                    if (session.scene.playersToWin == session.getPlayers().stream().filter(l -> !Player.get(l).kicked).toList().size()) {
                        session.kill();
                        return;
                    }

                    session.sendTable();

                    session.poll = new HashMap<>();
                }
            }
        }
    }

    private void join(String s, Player player) {
        Session session = Session.get(s);
        session.addPlayer(player);
        String url = "https://t.me/glkotbunkerbot?start=" + session.id;
        Message message = (Message) session.data.get("creatingMessage");
        StringBuilder cap = new StringBuilder("            id для копирования\n<b><code>" + session.id + "</code></b>\n\n" + "<a href=\"" + url + "\">ССЫЛКА-ПРИГЛАШЕНИЕ</a>\n\nИгроки:\n\n");

        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setChatId(message.getChatId());
        editMessageCaption.setMessageId(message.getMessageId());
        editMessageCaption.setParseMode("HTML");
        for (long l : session.getPlayers()) {
            Player p = Player.get(l);
            cap.append(" - ").append(p.getFullName()).append("\n");
        }
        List<List<InlineKeyboardButton>> listListButtons = new ArrayList<>();

        {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText("Начать игру");
                button.setCallbackData("startGame");
                buttons.add(button);
            }
            listListButtons.add(buttons);
        }


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(listListButtons);
        editMessageCaption.setReplyMarkup(inlineKeyboardMarkup);
        editMessageCaption.setCaption(cap.toString());
        try {
            execute(editMessageCaption);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }


        SendPhoto sendMessage = new SendPhoto();
        Scene scene = session.scene;
        sendMessage.setCaption("Ждем начало игры\n\n<b><code>" + player.session + "</code></b>\n\nПредыстория:\n<b>"+scene.name+"</b>\n\n"+scene.description);
        sendMessage.setPhoto(new InputFile(scene.picture));
        sendMessage.setParseMode("HTML");
        sendMessage.setChatId(player.id);
        try {
            player.messages.add(execute(sendMessage).getMessageId());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        System.out.println(session.getPlayers());
    }

    public static List<String> getWinningOptions(Map<String, Integer> pollResults) {
        List<String> winningOptions = new ArrayList<>();
        int maxVotes = -1;
        System.out.println(pollResults);
        // Проходим по всем записям в Map
        for (Map.Entry<String, Integer> entry : pollResults.entrySet()) {
            int votes = entry.getValue();
            String option = entry.getKey();

            // Если нашли больше голосов, чем было ранее, обновляем победителей
            if (votes > maxVotes) {
                maxVotes = votes;
                winningOptions.clear();  // Очистим список, так как это новый максимум
                winningOptions.add(option);  // Добавляем новый вариант как победителя
            }
            // Если количество голосов совпадает с максимальным, добавляем вариант
            else if (votes == maxVotes) {
                winningOptions.add(option);
            }
        }

        return winningOptions;  // Возвращаем список победителей
    }


    // Отключить удаление вебхуков, если нет соединения
    @Override
    public void clearWebhook() {
        try {
            super.clearWebhook();
        } catch (Exception e) {
            System.err.println("Не удалось удалить старый вебхук: " + e.getMessage());
        }
    }


}
