package org.reactome.server.tools.fireworks.exporter.raster;

import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.diagram.data.fireworks.graph.FireworksGraph;
import org.reactome.server.tools.diagram.data.fireworks.profile.FireworksProfile;
import org.reactome.server.tools.fireworks.exporter.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.factory.ResourcesFactory;
import org.reactome.server.tools.fireworks.exporter.profiles.FireworksColorProfile;
import org.reactome.server.tools.fireworks.exporter.raster.index.FireworksIndex;
import org.reactome.server.tools.fireworks.exporter.raster.layers.FireworksCanvas;
import org.reactome.server.tools.fireworks.exporter.raster.renderers.FireworksRenderer;
import org.reactome.server.tools.fireworks.exporter.raster.properties.FontProperties;
import org.reactome.server.tools.fireworks.exporter.profiles.ProfilesFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;

public class FireworksExporter {
	private static final int MARGIN = 15;
	private static final Set<String> TRANSPARENT_FORMATS = new HashSet<>(Collections.singletonList("png"));
	private static final Set<String> NO_TRANSPARENT_FORMATS = new HashSet<>(Arrays.asList("jpg", "jpeg", "gif"));
	private final FireworkArgs args;
	private final FireworksGraph layout;
	private final FireworksColorProfile profile;
	private final FireworksCanvas canvas = new FireworksCanvas();

	/**
	 * Initialize a new {@link FireworksExporter}
	 *
	 * @param args       specs of the resulting diagram.
	 * @param layoutPath where to find the species layout.
	 */
	public FireworksExporter(FireworkArgs args, String layoutPath) throws DeserializationException {
		this.args = args;
		layout = ResourcesFactory.getGraph(layoutPath, args.getSpeciesName());
		profile = ProfilesFactory.getProfile(args.getProfile());
	}

	/**
	 * Creates a {@link BufferedImage} using the specification in args.
	 */

	public BufferedImage render() {
		final FireworksIndex index = new FireworksIndex(layout, profile, args);
		final FireworksRenderer renderer = new FireworksRenderer(layout, canvas, profile, args, index);
		renderer.layout();
		final BufferedImage image = createImage();
		final Graphics2D graphics = createGraphics(image);
		canvas.render(graphics);
		return image;
	}

	private BufferedImage createImage() {
		final double factor = args.getFactor();
		final Rectangle2D bounds = canvas.getBounds();
		final int width = (int) (factor * bounds.getWidth() + 0.5);
		final int height = (int) (factor * bounds.getHeight() + 0.5);
		final String ext = args.getFormat();
		if (TRANSPARENT_FORMATS.contains(ext))
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		else if (NO_TRANSPARENT_FORMATS.contains(ext))
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		else
			throw new IllegalArgumentException("Unsupported file extension " + ext);
	}

	private Graphics2D createGraphics(BufferedImage image) {
		final String ext = args.getFormat();
		final Graphics2D graphics = image.createGraphics();
		if (NO_TRANSPARENT_FORMATS.contains(ext)) {
			Color bgColor = args.getBackground() == null
					? Color.WHITE
					: args.getBackground();
			graphics.setBackground(bgColor);
			graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
		}
		// This transformation allows elements to use their own dimensions,
		// isn't it nice?
		final Rectangle2D bounds = canvas.getBounds();
		final double factor = args.getFactor();
		final double offsetX = factor * (MARGIN - bounds.getMinX());
		final double offsetY = factor * (MARGIN - bounds.getMinY());
		graphics.translate(offsetX, offsetY);
		graphics.scale(factor, factor);

		graphics.setFont(FontProperties.DEFAULT_FONT);
		graphics.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		return graphics;
	}

}
