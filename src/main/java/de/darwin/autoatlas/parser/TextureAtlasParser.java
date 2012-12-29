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
package de.darwin.autoatlas.parser;

import java.io.IOException;

import darwin.resourcehandling.handle.ResourceHandle;
import darwin.resourcehandling.relative.RelativeFileFactory;

import de.darwin.autoatlas.TextureAtlas;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
public interface TextureAtlasParser {
    public TextureAtlas parseAtlas(ResourceHandle in) throws IOException;
    public void writeAtlas(RelativeFileFactory factory, TextureAtlas atlas, String name) throws IOException;
}
