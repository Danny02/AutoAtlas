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

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import darwin.resourcehandling.handle.ResourceHandle;
import darwin.resourcehandling.relative.RelativeFileFactory;

import de.darwin.autoatlas.TextureAtlas.Builder;
import de.darwin.autoatlas.TextureAtlas.Builder.Page;
import de.darwin.autoatlas.*;
import javax.imageio.ImageIO;

/**
 *
 * @author Daniel Heinrich <dannynullzwo@gmail.com>
 */
public class TextureAtlasParserTAI implements TextureAtlasParser {

    @Override
    public TextureAtlas parseAtlas(ResourceHandle in) throws IOException {
        Map<ResourceHandle, List<String[]>> pages = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in.getStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }
                String[] eles = line.split(",");
                String[] name = eles[0].split("\\s+");

                ResourceHandle image = in.resolve(name[1]);
                List<String[]> l = pages.get(image);
                if (l == null) {
                    l = new LinkedList<>();
                    pages.put(image, l);
                }

                l.add(new String[]{name[0], eles[3], eles[4], eles[6], eles[7]});
            }
        }

        Builder builder = TextureAtlas.build();
        for (Entry<ResourceHandle, List<String[]>> entry : pages.entrySet()) {
            Page page = builder.nextPage(ImageIO.read(entry.getKey().getStream()));
            for (String[] data : entry.getValue()) {
                page.addElement(data[0], Float.parseFloat(data[1].trim()),
                                Float.parseFloat(data[2].trim()),
                                Float.parseFloat(data[3].trim()),
                                Float.parseFloat(data[4].trim()));
            }
        }

        return builder.create();
    }

    @Override
    public void writeAtlas(RelativeFileFactory factory, TextureAtlas atlas, String name) throws IOException {
        OutputStream o = factory.writeRelative(name + ".tai");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(o))) {
            Map<BufferedImage, String> files = new HashMap<>();
            int fileIndex = 0;
            for (TextureAtlasElement element : atlas) {
                String file = files.get(element.base);
                if (file == null) {
                    boolean alpha = element.base.getColorModel().hasAlpha();
                    String format = alpha ? "png" : "jpg";
                    file = name + "/page" + fileIndex + '.' + format;
                    OutputStream image = factory.writeRelative(file);
                    ImageIO.write(element.base, format, image);
                    files.put(element.base, file);
                }

//# <filename>        <atlas filename>, <atlas idx>, <atlas type>, <woffset>, <hoffset>, <depth offset>, <width>, <height>
                writer.append(element.name);
                writer.append("\t\t");
                writer.append(file);
                writer.append(", 0, 2D, ");
                writer.append(Integer.toString(Math.round(element.woffset)));
                writer.append(", ");
                writer.append(Integer.toString(Math.round(element.hoffset)));
                writer.append(", 0, ");
                writer.append(Integer.toString(Math.round(element.width)));
                writer.append(", ");
                writer.append(Integer.toString(Math.round(element.heigth)));
                writer.newLine();
            }
        }
    }
}
