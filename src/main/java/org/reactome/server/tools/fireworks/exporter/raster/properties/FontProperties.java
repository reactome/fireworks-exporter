package org.reactome.server.tools.fireworks.exporter.raster.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FontProperties {

	private static final Logger logger = LoggerFactory.getLogger("infoLogger");

	public static final Font DEFAULT_FONT;

	static {
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, FontProperties.class.getResourceAsStream("fonts/arial.ttf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, FontProperties.class.getResourceAsStream("fonts/arialbd.ttf")));
		} catch (FontFormatException | IOException e) {
			// resources shouldn't throw exceptions
			logger.error("Couldn't load font", e);
		}
		DEFAULT_FONT = new Font("arial", Font.BOLD, 8);
	}

}
