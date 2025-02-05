package ru.glkot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TableToImage {
    public static void convert(Session session) {

        List<Integer> cellSizes = new ArrayList<>();

        // Заголовки столбцов
        List<String> names = new ArrayList<>();
        names.add("Имя");
        names.addAll(Prop.getAllNames());

        for (String s : names) {
            if (s.contains("(")) s = s.substring(0, s.indexOf("("));

            cellSizes.add(s.length() * 12 + 35); // Инициализируем размер столбцов на основе заголовков
        }

        List<List<String>> table = new ArrayList<>();

        for (long l : session.getPlayers()) {
            Player player = Player.get(l);

            List<String> row = new ArrayList<>();
            String fullName = player.getFullName();
            row.add(fullName);

            // Учитываем длину имени при расчете ширины первой колонки
            int nameWidth = fullName.length() * 12 + 35;
            if (cellSizes.get(0) < nameWidth) {
                cellSizes.set(0, nameWidth);
            }

            for (String propName : Prop.getAllNames()) {
                String value = player.getProp(propName);
                row.add(value);

                int colIndex = row.size() - 1;
                if (value.contains("(")) value = value.substring(0, value.indexOf("("));
                int cellWidth = value.length() * 12 + 35;

                if (cellSizes.get(colIndex) < cellWidth) {
                    cellSizes.set(colIndex, cellWidth);
                }
            }

            table.add(row);
        }

        System.out.println(cellSizes);

        // Размеры клетки
        int cellHeight = 40;

        // Размеры изображения
        int width = cellSizes.stream().mapToInt(Integer::intValue).sum() + 15;
        int height = (table.size() + 1) * cellHeight;
        if (height < 320) height = 320;

        // Создание пустого изображения
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Настройка сглаживания
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Заливка фона
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Рисование заголовков
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, width, cellHeight);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        int x = 0;
        for (int i = 0; i < names.size(); i++) {
            int colWidth = cellSizes.get(i);
            if (i == names.size() - 1) {
                colWidth += 15;
            }
            g2d.drawRect(x, 0, colWidth, cellHeight);
            g2d.drawString(names.get(i), x + 10, cellHeight / 2 + 5);
            x += colWidth;
        }

        // Рисование строк таблицы
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        for (int row = 0; row < table.size(); row++) {
            x = 0;
            for (int col = 0; col < table.get(row).size(); col++) {
                String value = table.get(row).get(col);
                if (!value.equals("-") && value.contains("(")) {
                    value = value.substring(0, value.indexOf("("));
                }

                int colWidth = cellSizes.get(col);
                int y = (row + 1) * cellHeight;
                if (col == table.get(row).size() - 1) {
                    colWidth += 15;
                }
                g2d.drawRect(x, y, colWidth, cellHeight);

                // Отступы для текста
                int textOffset = 10;
                if (col == table.get(row).size() - 1) {
                    textOffset += 10; // Увеличенный отступ для последнего столбца
                }

                g2d.drawString(value, x + textOffset, y + cellHeight / 2 + 5);
                x += colWidth;
            }

        }
        for (long l : session.getPlayers()) {
            Player player = Player.get(l);
            // Зачёркивание строки, если игрок исключён
            if (player.kicked) { // Предполагается, что у Player есть метод isKicked()
                g2d.setColor(new Color(255,0,0, 64)); // Зачёркивание красной линией
                int yCenter = (session.getPlayers().indexOf(l) + 1) * cellHeight + cellHeight / 2; // Центр строки

                // Устанавливаем толщину линии
                g2d.setStroke(new BasicStroke(cellHeight)); // Толщина линии = 3 пикселя
                g2d.drawLine(0, yCenter, width, yCenter); // Линия по всей ширине строки

                // Возвращаем толщину линии к стандартной
                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(Color.BLACK); // Возвращаем цвет на чёрный для последующей отрисовки
            }


            if (player.winner) { // Предполагается, что у Player есть метод isKicked()
                g2d.setColor(new Color(34, 255,0, 64)); // Зачёркивание красной линией
                int yCenter = (session.getPlayers().indexOf(l) + 1) * cellHeight + cellHeight / 2; // Центр строки

                // Устанавливаем толщину линии
                g2d.setStroke(new BasicStroke(cellHeight)); // Толщина линии = 3 пикселя
                g2d.drawLine(0, yCenter, width, yCenter); // Линия по всей ширине строки

                // Возвращаем толщину линии к стандартной
                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(Color.BLACK); // Возвращаем цвет на чёрный для последующей отрисовки
            }
        }
        // Освобождение ресурсов
        g2d.dispose();

        // Сохранение в PNG
        try {
            File file = new File("table.png");
            ImageIO.write(image, "png", file);
            System.out.println("Изображение таблицы сохранено как table.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
