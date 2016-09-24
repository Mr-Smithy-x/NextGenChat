package co.yoprice.nextgenchat.data.models;

import org.jsoup.nodes.Element;

import java.io.Serializable;

/**
 * Created by cj on 4/24/16.
 */
public class Color  implements Serializable{
    int r, g, b;
    int color;


    @Override
    public String toString() {
        return Integer.toHexString(color);
    }

    private Color() {
    }

    public static Color getColor(Element style) {
        if (style == null) return Color.fromRGB(0,0,0);
        String[] css = style.getElementsByTag("span").attr("style").split(";");
        for (String c : css) {
            if (c.contains("color")) {
                String colorStr = c.replace("color:#", "").replace("color: #", "").trim();
                return Color.fromHexString(colorStr);
            }
        }
        return Color.fromRGB(0, 0, 0);
    }

    public static Color _getColor(String hex) {
        return Color.fromHexString(hex);
    }


    public static Color getColorFont(Element font) {
        try {
            String color = font.getElementsByTag("font").attr("color");
            if (isColor(color)) return _getColor(color.substring(1));
            switch (color) {
                case "Red":
                    return Color.fromRGB(255, 0, 0);
                case "Blue":
                    return Color.fromRGB(0, 0, 255);
                case "Green":
                    return Color.fromRGB(0, 255, 0);
                case "Brown":
                    return Color.fromRGB(165, 42, 42);
                case "Orange":
                    return Color.fromRGB(255, 165, 0);
                case "Crimson":
                    return Color.fromRGB(220, 20, 60);
                case "Purple":
                    return Color.fromRGB(128, 0, 128);
                case "Magenta":
                    return Color.fromRGB(255, 0, 255);
                default:
                    return Color.fromRGB(0, 0, 0);
            }
        }catch (Exception ex){
            return Color.fromRGB(0, 0, 0);
        }
    }

    public static boolean isColor(String hex) {
        return hex.startsWith("#");
    }


    private Color(String hex) {
        this.color = Integer.parseInt(hex, 16);
        this.r = this.color >> 16 & 0xff;
        this.g = this.color >> 8 & 0xff;
        this.b = this.color & 0xff;
    }

    private Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.color = r << 16 | g << 8 | b;
    }

    public static Color fromHexString(String hex) {
        return new Color(hex);
    }

    public static Color fromRGB(int r, int g, int b) {
        return new Color(r, g, b);
    }

    public int toInt() {
        return color;
    }

    public int getR() {
        return r;
    }

    public Color setR(int r) {
        this.r = r;
        return this;
    }

    public int getG() {
        return g;
    }

    public Color setG(int g) {
        this.g = g;
        return this;
    }

    public int getB() {
        return b;
    }

    public Color setB(int b) {
        this.b = b;
        return this;
    }

    public int getColor() {
        return color;
    }

    public Color setColor(int color) {
        this.color = color;
        return this;
    }
}
