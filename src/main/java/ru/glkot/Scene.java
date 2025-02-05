package ru.glkot;

import com.sun.source.tree.TryTree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Scene {

    public String name;
    public String description;
    public List<String> whatNeeds;
    public int playersToWin;
    public int srok;
    public File picture;
    public String onEndWin;
    public String onEndDef;

    public Scene(){}


    public static List<Scene> scenes;
    public static void recognize(){
        File folder = Path.of("scenes").toFile();
        if (!folder.exists()) folder.mkdir();
        for (File file : folder.listFiles()) {
            if (file.getName().contains(".png")) continue;
            try(FileInputStream zov = new FileInputStream(file)) {
                JSONObject jsonObject = new JSONObject(new String(zov.readAllBytes()));
                Scene scene = new Scene();
                scene.name = jsonObject.getString("name");
                scene.description = jsonObject.getString("description");
                scene.onEndWin = jsonObject.getString("onEndWin");
                scene.onEndDef = jsonObject.getString("onEndDef");
                scene.whatNeeds = new ArrayList<>();
                for (Object o: jsonObject.getJSONArray("whatNeeds").toList()) {
                    if (o.getClass() == String.class) {
                        scene.whatNeeds.add((String) o);
                    }
                }
                scene.picture = Path.of(file.getPath().replaceAll(".json",".png")).toFile();
                scene.srok = jsonObject.getInt("srok");
                if (jsonObject.keySet().contains("playersToWin")) {
                    scene.playersToWin = jsonObject.getInt("playersToWin");
                } else scene.playersToWin = -1;
                scenes.add(scene);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[ ");
        for (Scene scene: scenes) {
            stringBuilder.append(scene.name).append(", ");
        }
        System.out.println(stringBuilder);
    }
}
