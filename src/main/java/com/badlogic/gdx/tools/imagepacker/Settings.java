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

/**
 *
 * @author Nathan Sweet
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 *      removed some not needed options
 */
public class Settings {
    public boolean pot = true;
    public int paddingX = 2, paddingY = 2;
    public boolean edgePadding = true;
    public boolean rotation;
    public int minWidth = 16, minHeight = 16;
    public int maxWidth = 1024, maxHeight = 1024;
    public boolean stripWhitespaceX, stripWhitespaceY;
    public int alphaThreshold;
    public boolean alias = true;
    public boolean ignoreBlankImages = true;
}
