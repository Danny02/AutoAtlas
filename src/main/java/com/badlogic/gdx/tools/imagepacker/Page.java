package com.badlogic.gdx.tools.imagepacker;

import java.util.List;


/**
 *
 * @author Nathan Sweet
 * modified: put in own classfile
 */
public class Page {
    public String imageName;
    public List<Rect> outputRects, remainingRects;
    public float occupancy;
    public int x, y, width, height;
}
