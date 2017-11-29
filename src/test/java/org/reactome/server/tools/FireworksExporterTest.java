package org.reactome.server.tools;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.fireworks.exporter.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.raster.FireworksExporter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Unit test for simple FireworksExporter.
 */
public class FireworksExporterTest
		extends TestCase {
	private static final String FIREWORK_PATH = "src/test/resources/org/reactome/server/tools/fireworks/exporter/layouts";
	private static final String IMAGE_FOLDER = "test-image";

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public FireworksExporterTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(FireworksExporterTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		try {
			args.setFactor(10);
			args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
			final FireworksExporter exporter = new FireworksExporter(args, FIREWORK_PATH);
			final BufferedImage image = exporter.render();
			final File file = new File(IMAGE_FOLDER, args.getSpeciesName() + "." + args.getFormat());
			ImageIO.write(image, args.getFormat(), file);
		} catch (DeserializationException | IOException e) {
			fail(e.getMessage());
		}
	}
}
