package org.reactome.server.tools.fireworks.exporter.raster.layers;

import org.reactome.server.tools.diagram.data.layout.impl.CoordinateFactory;
import org.reactome.server.tools.fireworks.exporter.common.profiles.ColorFactory;
import org.reactome.server.tools.fireworks.exporter.common.profiles.GradientColorProfile;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LegendBar implementation for GSA Regulation.
 * This type of legend doesn't use gradient colors.
 *
 * @author Guilherme Viteri
 */
public class RegulationBar {
    private final static Double NUMBER_OF_SHAPES = 5.0;
    private List<Shape> objects;
    private TextLayer texts;
    private Map<Integer, Shape> shapes;
    private RegulationSheet regulationSheet;
    private final static String[] SYMBOLS = {"\u25B2\u25B2",
            "\u25B2",
            "-",
            "\u25BC",
            "\u25BC\u25BC",
    };

    private final static double[][] SYMBOLS_CORRECTION = {
            {16d, 13d},
            {8d, 12d},
            {3d, 15d},
            {8d, 12d},
            {16d, 13d}
    };


    public RegulationBar(GradientColorProfile gradientSheet, double x, double y, double width, double height) {
        if (NUMBER_OF_SHAPES != SYMBOLS.length) return;

        this.regulationSheet = new RegulationSheet(gradientSheet);
        objects = new ArrayList<>(NUMBER_OF_SHAPES.intValue());
        texts = new TextLayer();
        for (int i = 0; i < NUMBER_OF_SHAPES; i++) {
            double auxY = y + ((height / 5) * i);
            Rectangle2D.Double rect = new Rectangle2D.Double(x, auxY, width, height / NUMBER_OF_SHAPES);
            objects.add(rect);
            texts.add(SYMBOLS[i], ColorFactory.parseColor("#ffeff2"), CoordinateFactory.get(rect.getCenterX() - 0, rect.getCenterY() +20));
        }

        initShapes();
    }

    public TextLayer getSymbols() {
        return texts;
    }

    public Map<Integer, Color> getColorMap() {
        return regulationSheet.getColorMap();
    }

    private void initShapes() {
        shapes = new LinkedHashMap<>(NUMBER_OF_SHAPES.intValue());
        int i = 0;
        for (int value : regulationSheet.getColorMap().keySet()) {
            shapes.put(value, objects.get(i));
            i++;
        }
    }

    public Map<Integer, Shape> getShapes() {
        if (shapes == null) initShapes();
        return shapes;
    }

}
