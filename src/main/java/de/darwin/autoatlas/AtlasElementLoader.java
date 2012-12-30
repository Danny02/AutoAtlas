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
import java.io.IOException;
import java.nio.file.*;

import darwin.annotations.ServiceProvider;
import darwin.resourcehandling.factory.*;
import darwin.resourcehandling.handle.*;
import darwin.util.misc.Throw;

import de.darwin.autoatlas.parser.TextureAtlasParserTAI;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
@ServiceProvider(ResourceFromHandleProvider.class)
public class AtlasElementLoader extends ResourceFromHandleProvider<TextureAtlasElement>
        implements ResourceFromHandle<TextureAtlasElement> {

    private TextureAtlas atlas;

    public AtlasElementLoader() {
        super(TextureAtlasElement.class);
    }

    @Override
    public TextureAtlasElement create(ResourceHandle handle) throws IOException {
        return findAtlas().getElement(handle.getName());
    }

    @Override
    public void update(ResourceHandle changed, TextureAtlasElement wrapper) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TextureAtlas findAtlas() {
        if (atlas == null) {
            Path p = Paths.get(AtlasProducer.ATLAS_FILENAME + ".tai");
            try {
                atlas = new TextureAtlasParserTAI().parseAtlas(new ClasspathFileHandler(p));
            } catch (IOException ex) {
                Throw.unchecked(ex);
            }
        }
        return atlas;
    }

    @Override
    public TextureAtlasElement getFallBack() {
        return new TextureAtlasElement(0, 0, 1, 1, "test", new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    }

    @Override
    public ResourceFromHandle<TextureAtlasElement> get(String[] options) {
        return new AtlasElementLoader();
    }
}
