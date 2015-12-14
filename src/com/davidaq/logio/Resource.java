package com.davidaq.logio;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Resource {
    public static final Image getImage(String name) {
        return getImage(name, -1, -1);
    }

    public static final Image getImage(String name, int w, int h) {
        try {
            Image img = ImageIO.read(Resource.class.getResource("images/" + name));
            if (w == -1 && h == -1)
                return img;
            int iw = img.getWidth(null);
            int ih = img.getHeight(null);
            if (iw != w || ih != h) {
                img = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            }
            return img;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
