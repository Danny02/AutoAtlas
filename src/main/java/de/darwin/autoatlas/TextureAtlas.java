/*
 * Copyright (C) 2012 Daniel Heinrich <dannynullzwo@gmail.com>
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
package de.darwin.autoatlas;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
public class TextureAtlas implements Iterable<TextureAtlasElement> {

    public static class Builder {

        private final TextureAtlas atlas = new TextureAtlas();

        public Page nextPage(BufferedImage baseImage) {
            return new Page(baseImage);
        }

        public TextureAtlas create() {
            return atlas;
        }

        public class Page extends Builder {

            private final BufferedImage img;

            private Page(BufferedImage img) {
                this.img = img;
            }

            public Page addElement(String name, float woffset, float hoffset, float width, float heigth) {
                atlas.elements.put(name, new TextureAtlasElement(woffset, hoffset, width, heigth, name, img));
                return this;
            }
        }
    }

    public static Builder build() {
        return new Builder();
    }
    private final Map<String, TextureAtlasElement> elements = new HashMap<>();

    private TextureAtlas() {
    }

    public TextureAtlasElement getElement(String name) {
        return elements.get(name);
    }

    @Override
    public Iterator<TextureAtlasElement> iterator() {
        return elements.values().iterator();
    }
}
