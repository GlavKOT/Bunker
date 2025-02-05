package ru.glkot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Player {
    public static Map<Long,Player> players;

    Message card;
    final User user;
    final long id;
    private Map<String,String> props;
    private Map<String,String> openedProps;
    Map<String,Object> data;
    final String firstName;
    final String lastName;
    final String userName;
    UUID session;
    List<Integer> messages;
    boolean stepper;
    boolean kicked;
    boolean winner;
    boolean choser;
    boolean polled;
    String profdata;
    String prof;
    public int yo;

    protected Player(User user) {
        this.user = user;
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.userName = user.getUserName();
        {
            this.props = new HashMap<>();
            this.openedProps = new HashMap<>();
            this.messages = new ArrayList<>();
            this.data = new HashMap<>();
        }
        {
            stepper = false;
            kicked = false;
            choser = false;
            polled = false;
            winner = false;
        }
        yo = 0;
        prof = "null";
        profdata = "null";
        data.put("getter",false);
        flush();
    }

    public String getFullName() {
        if (stepper) {
            if (lastName == null) return "[ " + firstName + " ]";
            else return "[ " + firstName + " " + lastName + " ]";
        } else {
            if (lastName == null) return firstName;
            else return firstName + " " + lastName;
        }
    }

    public void flush() {
        players.put(id,this);
    }

    public static Player get(long id) {
        return players.get(id);
    }
    public static Player get(User user) {
        if (players.containsKey(user.getId())) {
            return players.get(user.getId());
        }
        return new Player(user);
    }

    public void deleteMessages() {
        for (int i : messages) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setMessageId(i);
            deleteMessage.setChatId(id);
            try {
                Bot.get.execute(deleteMessage);
            } catch (TelegramApiException e) {
                System.out.println(new RuntimeException(e));
            }
        }
        messages = new ArrayList<>();
    }

    public void randomiseProps() {
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Ваша карточка");

        int i = 0;
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (String s: Prop.getAllNames()) {
            String prop = Prop.random(s);
            while (prop.isBlank()) prop = Prop.random(s);
            props.put(s,prop);
            openedProps.put(s,"-");

            if (s.equals("Профессия")) prof = prop.substring(0,prop.indexOf("(")-1);
            if (s.equals("Профессия")) profdata = prop.substring(prop.indexOf("("));

            {
                InlineKeyboardButton button = new InlineKeyboardButton();
                if (prop.contains("(")) {
                    System.out.println(prop);
                    System.out.println(prop.substring(0, prop.indexOf("(")));
                    button.setText(prop.substring(0, prop.indexOf("(")));
                    stringBuilder.append("\n\n").append(prop);
                } else button.setText(prop);
                button.setCallbackData("prop|"+s);
                System.out.println(button);
                buttons.add(button);
            }
            if (i%2==1) {
                inlineButtons.add(buttons);
                buttons = new ArrayList<>();
            }
            i++;
        }
        inlineButtons.add(buttons);
        flush();

        {
            List<InlineKeyboardButton> ons = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            String text = props.get("Профессия");
            inlineKeyboardButton.setText(text.substring(text.indexOf("(")+1,text.length()-1));
            inlineKeyboardButton.setCallbackData("prof");
            ons.add(inlineKeyboardButton);
            inlineButtons.add(ons);
        }


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(stringBuilder.toString());
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setChatId(id);
        try {
            card = Bot.get.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String,String> getAllProps() {return props;}
    public String getProp(String s) {
        return openedProps.get(s);
    }
    public String getOnProp(String s) {
        return props.get(s);
    }
    public void openAllProps(){
        openedProps = props;
    }
    public void addToProp(String prop,String data) {
        openedProps.put(prop,data + " + " + openedProps.get(prop));
        props.put(prop,data + " + " + props.get(prop));
        System.out.println(openedProps);
        System.out.println(props);
    }
    public void setProp(String prop,String data) {
        openedProps.put(prop, data);
        props.put(prop, data);
    }
    public void setPropUnOpen(String prop,String data) {
        if (!openedProps.get(prop).equals("-")) openedProps.put(prop,data);
        props.put(prop, data);
    }

    public void openProp(String s1) {
        openedProps.put(s1,props.get(s1));
    }
}
