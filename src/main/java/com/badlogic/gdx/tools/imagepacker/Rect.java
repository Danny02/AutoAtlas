package com.badlogic.gdx.tools.imagepacker;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author Nathan Sweet
 * modified: put in own classfile
 */
public class Rect {

    public String name;
    public BufferedImage image;
    public int offsetX, offsetY, originalWidth, originalHeight;
    public int x, y, width, height;
    public boolean rotated;
    public ArrayList<Rect> aliases = new ArrayList();
    public int[] splits;
    public int[] pads;
    public boolean canRotate = true;
    int score1, score2;

    public Rect(BufferedImage source, int left, int top, int newWidth, int newHeight) {
        image = new BufferedImage(source.getColorModel(), source.getRaster().createWritableChild(left, top, newWidth, newHeight,
                                                                                                 0, 0, null), source.getColorModel().isAlphaPremultiplied(), null);
        offsetX = left;
        offsetY = top;
        originalWidth = source.getWidth();
        originalHeight = source.getHeight();
        width = newWidth;
        height = newHeight;
    }

    public Rect() {
    }

    public Rect(Rect rect) {
        setSize(rect);
    }

    void setSize(Rect rect) {
        x = rect.x;
        y = rect.y;
        width = rect.width;
        height = rect.height;
    }

    void set(Rect rect) {
        name = rect.name;
        image = rect.image;
        offsetX = rect.offsetX;
        offsetY = rect.offsetY;
        originalWidth = rect.originalWidth;
        originalHeight = rect.originalHeight;
        x = rect.x;
        y = rect.y;
        width = rect.width;
        height = rect.height;
        rotated = rect.rotated;
        aliases = rect.aliases;
        splits = rect.splits;
        pads = rect.pads;
        canRotate = rect.canRotate;
        score1 = rect.score1;
        score2 = rect.score2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Rect other = (Rect) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return name + "[" + x + "," + y + " " + width + "x" + height + "]";
    }
}
