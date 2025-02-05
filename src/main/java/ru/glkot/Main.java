package ru.glkot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        Player.players = new HashMap<>();
        Session.sessions = new HashMap<>();
        Scene.scenes = new ArrayList<>();
        Scene.recognize();
        Prop.createFiles();

        for (int i = 0; i < 100; i++) {
            System.out.println(Prop.random("Профессия"));
        }

        new Thread(() -> {
            while (true) {
                try {
                    for (Player player: Player.players.values()) {
                        player.yo++;
                        if (player.yo >= 60) {
                            Player.players.remove(player.id);
                            System.out.println("REMOVE PLAYER "+player.id);
                            break;
                        }
                    }


                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        SpringApplication.run(Main.class, args);
    }
}