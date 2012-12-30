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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;

import darwin.annotations.ServiceProvider;
import darwin.resourcehandling.*;
import darwin.resourcehandling.relative.RelativeFileFactory;
import darwin.util.misc.Throw;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tools.imagepacker.*;
import de.darwin.autoatlas.TextureAtlas.Builder;
import de.darwin.autoatlas.parser.*;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
@ServiceProvider(ResourceProcessor.class)
public class AtlasProducer implements ResourceProcessor {
    public static final String ATLAS_FILENAME = "resources/atlas";

    @Override
    public Collection<ResourceTupel> process(Collection<ResourceTupel> resource, RelativeFileFactory filer) {
        Settings set = new Settings();
        set.maxHeight = 2048;
        set.maxWidth = 2048;

        MaxRectsPacker packer = new MaxRectsPacker(set);
        ImageProcessor proc = new ImageProcessor(set);
        
        for (ResourceTupel tu : resource) {
            try {
                String name = tu.path.toString();
                proc.addImage(filer.readRelative(tu.path.toString()), name);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        if (proc.getImages().size() > 0) {
            List<Page> pages = packer.pack(proc.getImages());
            TextureAtlas atlas = createAtlas(pages, set);
            saveAtlas(atlas, filer);
        }
        
        return resource;
    }

    @Override
    public Class[] supportedResourceTypes() {
        return new Class[]{TextureAtlasElement.class};
    }

    @Override
    public String[] supportedFileExtensions() {
        return null;
    }

    public static TextureAtlas createAtlas(Iterable<Path> images) {
        Settings set = new Settings();
        set.maxHeight = 2048;
        set.maxWidth = 2048;

        ImageProcessor proc = new ImageProcessor(set);

        for (Path tu : images) {
            proc.addImage(tu);
        }

        MaxRectsPacker packer = new MaxRectsPacker(set);
        List<Page> pages = packer.pack(proc.getImages());
        return createAtlas(pages, set);
    }

    /*
     * based on com.badlogic.gdx.tools.imagepacker.TexturePacker2 by Nathan Sweet
     */
    private static TextureAtlas createAtlas(List<Page> pages, Settings settings) {
        Builder builder = TextureAtlas.build();

        int fileIndex = 0;
        for (Page page : pages) {
            fileIndex++;

            int width = page.width, height = page.height;
            int paddingX = settings.paddingX;
            int paddingY = settings.paddingY;

            width -= settings.paddingX;
            height -= settings.paddingY;
            if (settings.edgePadding) {
                page.x = paddingX;
                page.y = paddingY;
                width += paddingX * 2;
                height += paddingY * 2;
            }
            if (settings.pot) {
                width = MathUtils.nextPowerOfTwo(width);
                height = MathUtils.nextPowerOfTwo(height);
            }
            width = Math.max(settings.minWidth, width);
            height = Math.max(settings.minHeight, height);

            boolean opaque = true;
            for (Rect rect : page.outputRects) {
                if (rect.image.getColorModel().hasAlpha()) {
                    opaque = false;
                    break;
                }
            }

            BufferedImage canvas = new BufferedImage(width, height, opaque ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) canvas.getGraphics();

            Builder.Page nextPage = builder.nextPage(canvas);
            for (Rect rect : page.outputRects) {
                int rectX = page.x + rect.x, rectY = page.y + page.height - rect.y - rect.height;
                if (rect.rotated) {
                    g.translate(rectX, rectY);
                    g.rotate(-90 * MathUtils.degreesToRadians);
                    g.translate(-rectX, -rectY);
                    g.translate(-(rect.height - settings.paddingY), 0);
                }
                BufferedImage image = rect.image;
                g.drawImage(image, rectX, rectY, null);
                if (rect.rotated) {
                    g.translate(rect.height - settings.paddingY, 0);
                    g.translate(rectX, rectY);
                    g.rotate(90 * MathUtils.degreesToRadians);
                    g.translate(-rectX, -rectY);
                }

                nextPage.addElement(rect.name, rectX, rectY, rect.width - settings.paddingX, rect.height - settings.paddingY);
            }
        }
        return builder.create();
    }

    private void saveAtlas(TextureAtlas atlas, RelativeFileFactory filer) {
        try {
            TextureAtlasParser parser = new TextureAtlasParserTAI();
            parser.writeAtlas(filer, atlas, ATLAS_FILENAME);
        } catch (IOException ex) {
            Throw.unchecked(ex);
        }
    }
}
