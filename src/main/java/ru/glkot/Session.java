package ru.glkot;

import org.telegram.telegrambots.meta.api.methods.forum.EditGeneralForumTopic;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.Path;
import java.util.*;

public class Session {
    public static Map<UUID,Session> sessions;

    final UUID id;
    private List<Long> players;
    public Map<String, Object> data;
    String state;
    List<Message> tableMes;
    List<Message> sceneMes;
    private int step;
    int round;
    boolean onPoll;
    Map<String,Integer> poll;
    Scene scene;
    Player admin;

    public Session() {
        this.id = UUID.randomUUID();
        this.players = new ArrayList<>();
        this.data = new HashMap<>();
        this.state = "starting";
        scene = Scene.scenes.get(new Random().nextInt(Scene.scenes.size()));
        onPoll = false;
        poll = new HashMap<>();
        round = 0;
        tableMes = new ArrayList<>();
        sceneMes = new ArrayList<>();
        step = 0;
        flush();
    }

    public void next() {

        if (scene.playersToWin == -1) {
            System.out.println(players.size());
            System.out.println( (int) Math.round(players.size() * 0.75));

            scene.playersToWin = new Random().nextInt(1, (int) Math.round(players.size() * 0.75));
            System.out.println(scene.playersToWin);
        }



        step++;
        if (step >= players.size()) step = 0;

        Player player = Player.get(players.get(step));
        player.stepper = true;
        player.flush();


        if (step-1 == -1) {
            Player player1 = Player.get(players.get(players.size()-1));
            player1.stepper = false;
            player1.flush();

            if (round != 0) {
                sendPoll();
                onPoll = true;
            }

            round++;

        } else {
            Player player1 = Player.get(players.get(step-1));
            player1.stepper = false;
            player1.flush();
        }
        if (Player.get(players.get(step)).kicked && players.size() > 1) next();
    }

    public void kill() {
        for (long l: players) {
            Player.get(l).openAllProps();
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(Player.get(l).card.getChatId());
            deleteMessage.setMessageId(Player.get(l).card.getMessageId());
            try {
                Bot.get.execute(deleteMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        for (long l: players.stream().filter(l -> !Player.get(l).kicked).toList()) Player.get(l).winner = true;

        sendTable();

        boolean j = false;
        List<String> whatNed = scene.whatNeeds;

        for (long l : players) {
            for (String s : Player.get(l).getAllProps().values()){
                for (String s1: whatNed) {
                    if (s.contains(s1)) j = true;
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        List<Long> niggers = players.stream().filter(p -> !Player.get(p).kicked).toList();
        if (niggers.size() == 1) {
            String name = Player.get(niggers.get(0)).getFullName();
            if (name.contains("[")) name = name.replaceAll("\\[","").replaceAll("]","");
            stringBuilder.append(name);
        }
        else for (long g: niggers) {
            String name = Player.get(g).getFullName();
            if (name.contains("[")) name = name.replaceAll("\\[","").replaceAll("]","");
            stringBuilder.append(name);
            if (niggers.size() >= niggers.indexOf(g)) {
                stringBuilder.append(", ");
            }
        }

        for (long l : players) {
            Player player = Player.get(l);
            SendMessage sendPhoto = new SendMessage();


            sendPhoto.setChatId(l);
            if (j) {
                sendPhoto.setText(scene.onEndWin.replaceAll("%name",stringBuilder.toString()));
            } else {
                sendPhoto.setText(scene.onEndDef.replaceAll("%name",stringBuilder.toString()));
            }
            sendPhoto.setParseMode("HTML");
            try {
                tableMes.add(Bot.get.execute(sendPhoto));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }




        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Завершение сессии");
        sendMessage.setChatId(admin.id);

        List<List<InlineKeyboardButton>> bb = new ArrayList<>();
        List<InlineKeyboardButton> b = new ArrayList<>();
        InlineKeyboardButton u = new InlineKeyboardButton();
        u.setCallbackData("killSession");
        u.setText("Да");
        b.add(u);
        bb.add(b);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(bb);
        sendMessage.setReplyMarkup(markup);
        try {
            Bot.get.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean hisStep(Player player) {
        return players.get(step).equals(player.id);
    }

    public void addPlayer(Player player) {
        if (!players.contains(player.id)) players.add(player.id);
        player.session = id;
        player.flush();
    }
    public void addPlayer(long id) {
        addPlayer(Player.get(id));
    }

    public List<Long> getPlayers() {
        return players;
    }

    public void flush() {
        sessions.put(id,this);
    }

    public static Session get(UUID id) {
        return sessions.get(id);
    }
    public static Session get(String id) {
        return sessions.get(UUID.fromString(id));
    }

    public void updateTable() {
        if (!state.equals("starting")) next();
        state = "game";

        for (Message m : tableMes) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setMessageId(m.getMessageId());
            deleteMessage.setChatId(m.getChatId());
            try {
                Bot.get.execute(deleteMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        tableMes = new ArrayList<>();

        TableToImage.convert(this);
        for (long l : players) {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(l);
            Player player = Player.get(l);
            if (player.stepper) {
                sendPhoto.setCaption("Ваш ход");
            } else sendPhoto.setCaption("Таблица");
            sendPhoto.setPhoto(new InputFile(Path.of("table.png").toFile()));
            try {
                tableMes.add(Bot.get.execute(sendPhoto));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendTable() {
        for (Message m : tableMes) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setMessageId(m.getMessageId());
            deleteMessage.setChatId(m.getChatId());
            try {
                Bot.get.execute(deleteMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        tableMes = new ArrayList<>();

        TableToImage.convert(this);
        for (long l : players) {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(l);
            Player player = Player.get(l);
            if (player.stepper) {
                sendPhoto.setCaption("Ваш ход");
            } else sendPhoto.setCaption("Таблица");
            sendPhoto.setPhoto(new InputFile(Path.of("table.png").toFile()));
            try {
                tableMes.add(Bot.get.execute(sendPhoto));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void sendPoll() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            List<String> list = new ArrayList<>();
            for (long l : players) {
                if (!Player.get(l).kicked) {
                    Player.get(l).polled = false;
                    list.add(Player.get(l).getFullName());
                }
            }
            list.add("Пропуск");

            for (long l : players) {
                Player player = Player.get(l);
                if (!player.kicked) {
                    SendPoll poll = new SendPoll();
                    poll.setChatId(l);
                    poll.setAllowMultipleAnswers(false);
                    poll.setQuestion("Кто выбывает в этом раунде?");
                    poll.setOptions(list);
                    try {
                        Message message = Bot.get.execute(poll);
                        Bot.polls.put(message.getPoll().getId(), message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
    }
}
