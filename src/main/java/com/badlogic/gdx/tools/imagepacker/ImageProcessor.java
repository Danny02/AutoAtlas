/*
 * Copyright (C) 2012 daniel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.tools.imagepacker;

import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.*;
import java.util.logging.*;

import javax.imageio.ImageIO;


/**
 * 
 * @author Nathan Sweet
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 *      removed some gdx dependencys, and modified to the new Settings class
 */
public class ImageProcessor {

    static private final BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
    private final Settings settings;
    private final HashMap<String, Rect> crcs = new HashMap();
    private final List<Rect> rects = new ArrayList<>();

    public ImageProcessor(Settings settings) {
        this.settings = settings;
    }

    public void addImage(Path p) {
        try {
            addImage(Files.newInputStream(p), p.toString());
        } catch (IOException ex) {
            throw new RuntimeException("Error reading image: " + p.toString(), ex);
        }
    }
    
    public void addImage(InputStream in, String name) {
        BufferedImage image;
        try {
            image = ImageIO.read(in);
        } catch (IOException ex) {
            throw new RuntimeException("Error reading image: " + name, ex);
        }
        if (image == null) {
            throw new RuntimeException("Unable to read image: " + name);
        }

        Rect rect = createRect(image);
        if (rect == null) {
            System.out.println("Ignoring blank input image: " + name);
            return;
        }

        rect.name = name;

        if (settings.alias) {
            String crc = hash(rect.image);
            Rect existing = crcs.get(crc);
            if (existing != null) {
                System.out.println(rect.name + " (alias of " + existing.name + ")");
                existing.aliases.add(rect);
                return;
            }
            crcs.put(crc, rect);
        }

        rects.add(rect);
    }

    public List<Rect> getImages() {
        return rects;
    }

    /**
     * Strips whitespace and returns the rect, or null if the image should be
     * ignored.
     */
    private Rect createRect(BufferedImage source) {
        WritableRaster alphaRaster = source.getAlphaRaster();
        if (alphaRaster == null || (!settings.stripWhitespaceX && !settings.stripWhitespaceY)) {
            return new Rect(source, 0, 0, source.getWidth(), source.getHeight());
        }
        final byte[] a = new byte[1];
        int top = 0;
        int bottom = source.getHeight();
        if (settings.stripWhitespaceX) {
            outer:
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    alphaRaster.getDataElements(x, y, a);
                    int alpha = a[0];
                    if (alpha < 0) {
                        alpha += 256;
                    }
                    if (alpha > settings.alphaThreshold) {
                        break outer;
                    }
                }
                top++;
            }
            outer:
            for (int y = source.getHeight(); --y >= top;) {
                for (int x = 0; x < source.getWidth(); x++) {
                    alphaRaster.getDataElements(x, y, a);
                    int alpha = a[0];
                    if (alpha < 0) {
                        alpha += 256;
                    }
                    if (alpha > settings.alphaThreshold) {
                        break outer;
                    }
                }
                bottom--;
            }
        }
        int left = 0;
        int right = source.getWidth();
        if (settings.stripWhitespaceY) {
            outer:
            for (int x = 0; x < source.getWidth(); x++) {
                for (int y = top; y < bottom; y++) {
                    alphaRaster.getDataElements(x, y, a);
                    int alpha = a[0];
                    if (alpha < 0) {
                        alpha += 256;
                    }
                    if (alpha > settings.alphaThreshold) {
                        break outer;
                    }
                }
                left++;
            }
            outer:
            for (int x = source.getWidth(); --x >= left;) {
                for (int y = top; y < bottom; y++) {
                    alphaRaster.getDataElements(x, y, a);
                    int alpha = a[0];
                    if (alpha < 0) {
                        alpha += 256;
                    }
                    if (alpha > settings.alphaThreshold) {
                        break outer;
                    }
                }
                right--;
            }
        }
        int newWidth = right - left;
        int newHeight = bottom - top;
        if (newWidth <= 0 || newHeight <= 0) {
            if (settings.ignoreBlankImages) {
                return null;
            } else {
                return new Rect(emptyImage, 0, 0, 1, 1);
            }
        }
        return new Rect(source, left, top, newWidth, newHeight);
    }

    private String splitError(int x, int y, int[] rgba, String name) {
        throw new RuntimeException("Invalid " + name + " ninepatch split pixel at " + x + ", " + y + ", rgba: " + rgba[0] + ", "
                                   + rgba[1] + ", " + rgba[2] + ", " + rgba[3]);
    }

    /**
     * Returns the splits, or null if the image had no splits or the splits were
     * only a single region. Splits are an int[4] that has left, right, top,
     * bottom.
     */
    private int[] getSplits(BufferedImage image, String name) {
        WritableRaster raster = image.getRaster();

        int startX = getSplitPoint(raster, name, 1, 0, true, true);
        int endX = getSplitPoint(raster, name, startX, 0, false, true);
        int startY = getSplitPoint(raster, name, 0, 1, true, false);
        int endY = getSplitPoint(raster, name, 0, startY, false, false);

        // Ensure pixels after the end are not invalid.
        getSplitPoint(raster, name, endX + 1, 0, true, true);
        getSplitPoint(raster, name, 0, endY + 1, true, false);

        // No splits, or all splits.
        if (startX == 0 && endX == 0 && startY == 0 && endY == 0) {
            return null;
        }

        // Subtraction here is because the coordinates were computed before the 1px border was stripped.
        if (startX != 0) {
            startX--;
            endX = raster.getWidth() - 2 - (endX - 1);
        } else {
            // If no start point was ever found, we assume full stretch.
            endX = raster.getWidth() - 2;
        }
        if (startY != 0) {
            startY--;
            endY = raster.getHeight() - 2 - (endY - 1);
        } else {
            // If no start point was ever found, we assume full stretch.
            endY = raster.getHeight() - 2;
        }

        return new int[]{startX, endX, startY, endY};
    }

    /**
     * Returns the pads, or null if the image had no pads or the pads match the
     * splits. Pads are an int[4] that has left, right, top, bottom.
     */
    private int[] getPads(BufferedImage image, String name, int[] splits) {
        WritableRaster raster = image.getRaster();

        int bottom = raster.getHeight() - 1;
        int right = raster.getWidth() - 1;

        int startX = getSplitPoint(raster, name, 1, bottom, true, true);
        int startY = getSplitPoint(raster, name, right, 1, true, false);

        // No need to hunt for the end if a start was never found.
        int endX = 0;
        int endY = 0;
        if (startX != 0) {
            endX = getSplitPoint(raster, name, startX + 1, bottom, false, true);
        }
        if (startY != 0) {
            endY = getSplitPoint(raster, name, right, startY + 1, false, false);
        }

        // Ensure pixels after the end are not invalid.
        getSplitPoint(raster, name, endX + 1, bottom, true, true);
        getSplitPoint(raster, name, right, endY + 1, true, false);

        // No pads.
        if (startX == 0 && endX == 0 && startY == 0 && endY == 0) {
            return null;
        }

        // -2 here is because the coordinates were computed before the 1px border was stripped.
        if (startX == 0 && endX == 0) {
            startX = -1;
            endX = -1;
        } else {
            if (startX > 0) {
                startX--;
                endX = raster.getWidth() - 2 - (endX - 1);
            } else {
                // If no start point was ever found, we assume full stretch.
                endX = raster.getWidth() - 2;
            }
        }
        if (startY == 0 && endY == 0) {
            startY = -1;
            endY = -1;
        } else {
            if (startY > 0) {
                startY--;
                endY = raster.getHeight() - 2 - (endY - 1);
            } else {
                // If no start point was ever found, we assume full stretch.
                endY = raster.getHeight() - 2;
            }
        }

        int[] pads = new int[]{startX, endX, startY, endY};

        if (splits != null && Arrays.equals(pads, splits)) {
            return null;
        }

        return pads;
    }

    /**
     * Hunts for the start or end of a sequence of split pixels. Begins
     * searching at (startX, startY) then follows along the x or y axis
     * (depending on value of xAxis) for the first non-transparent pixel if
     * startPoint is true, or the first transparent pixel if startPoint is
     * false. Returns 0 if none found, as 0 is considered an invalid split point
     * being in the outer border which will be stripped.
     */
    private int getSplitPoint(WritableRaster raster, String name, int startX, int startY, boolean startPoint, boolean xAxis) {
        int[] rgba = new int[4];

        int next = xAxis ? startX : startY;
        int end = xAxis ? raster.getWidth() : raster.getHeight();
        int breakA = startPoint ? 255 : 0;

        int x = startX;
        int y = startY;
        while (next != end) {
            if (xAxis) {
                x = next;
            } else {
                y = next;
            }

            raster.getPixel(x, y, rgba);
            if (rgba[3] == breakA) {
                return next;
            }

            if (!startPoint && (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0 || rgba[3] != 255)) {
                splitError(x, y, rgba, name);
            }

            next++;
        }

        return 0;
    }

    static private String hash(BufferedImage image) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            int width = image.getWidth();
            int height = image.getHeight();
            if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                newImage.getGraphics().drawImage(image, 0, 0, null);
                image = newImage;
            }
            WritableRaster raster = image.getRaster();
            int[] pixels = new int[width];
            for (int y = 0; y < height; y++) {
                raster.getDataElements(0, y, width, 1, pixels);
                for (int x = 0; x < width; x++) {
                    int rgba = pixels[x];
                    digest.update((byte) (rgba >> 24));
                    digest.update((byte) (rgba >> 16));
                    digest.update((byte) (rgba >> 8));
                    digest.update((byte) rgba);
                }
            }
            return new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}
