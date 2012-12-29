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

import darwin.util.math.base.vector.Vector2;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
public class TextureAtlasElement {

    public final float woffset, hoffset, width, heigth;
    public final String name;
    public final BufferedImage base;

    public TextureAtlasElement(float woffset, float hoffset, float width, float heigth, String name, BufferedImage base) {
        this.woffset = woffset;
        this.hoffset = hoffset;
        this.width = width;
        this.heigth = heigth;
        this.name = name;
        this.base = base;
    }

    public Vector2 getTexCoord(float s, float t) {
        return new Vector2(woffset + s * width, hoffset + t * heigth);
    }

    public Vector2 getRelativeTexCoord(float s, float t) {
        return new Vector2(s * width, t * heigth);
    }

    public BufferedImage getSubImage() {
        return base.getSubimage(Math.round(woffset), Math.round(hoffset),
                                Math.round(width), Math.round(heigth));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TextureAtlasElement other = (TextureAtlasElement) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
