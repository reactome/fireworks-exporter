package org.reactome.server.tools;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.IOUtils;
import org.reactome.server.tools.diagram.data.exception.DeserializationException;
import org.reactome.server.tools.fireworks.exporter.api.FireworkArgs;
import org.reactome.server.tools.fireworks.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.analysis.model.AnalysisResult;
import org.reactome.server.tools.fireworks.exporter.raster.FireworksExporter;
import org.reactome.server.tools.fireworks.exporter.raster.index.ContentServiceClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
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
			args.setFactor(10.);
			args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
			final FireworksExporter exporter = new FireworksExporter(args, FIREWORK_PATH);
			final BufferedImage image = exporter.render();
			saveToDisk(args, image);
		} catch (AnalysisServerError | DeserializationException | IOException | AnalysisException e) {
			fail(e.getMessage());
		}
	}

	public void testFlags() {
		ContentServiceClient.setHost("https://reactomedev.oicr.on.ca/");
		final FireworkArgs args = new FireworkArgs("Canis_familiaris", "png");
		args.setFlags(Arrays.asList("CTP"));
		args.setFactor(7.);
		try {
			final FireworksExporter exporter = new FireworksExporter(args, FIREWORK_PATH);
			final BufferedImage image = exporter.render();
			saveToDisk(args, image);
		} catch (DeserializationException | IOException | AnalysisServerError | AnalysisException e) {
			fail(e.getMessage());
		}
	}

	public void testEnrichment() {
//		AnalysisClient.setServer("https://reactomedev.oicr.on.ca/");
		AnalysisClient.setServer("http://localhost:8080");
		AnalysisClient.setService("");
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setFactor(7.);
		try {
			args.setToken(createEnrichmentToken());
			final FireworksExporter exporter = new FireworksExporter(args, FIREWORK_PATH);
			final BufferedImage image = exporter.render();
			saveToDisk(args, image);
		} catch (DeserializationException | IOException | AnalysisServerError | AnalysisException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testExpression() {
		ContentServiceClient.setHost("https://reactomerelease.oicr.on.ca/");
		AnalysisClient.setServer("http://localhost:8080");
		AnalysisClient.setService("");
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setFactor(7.);
		args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
//		args.setFlags(Arrays.asList("CTP"));
		args.setProfile("Calcium Salts");
		args.setColumn(1);
		try {
			args.setToken(createExpressionToken());
			final FireworksExporter exporter = new FireworksExporter(args, FIREWORK_PATH);
			final BufferedImage image = exporter.render();
			saveToDisk(args, image);
		} catch (DeserializationException | IOException | AnalysisServerError | AnalysisException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void saveToDisk(FireworkArgs args, BufferedImage image) throws IOException {
		final File file = new File(IMAGE_FOLDER, args.getSpeciesName() + "." + args.getFormat());
		ImageIO.write(image, args.getFormat(), file);
	}

	private String createEnrichmentToken() throws IOException, AnalysisException, AnalysisServerError {
		final URL resource = getClass().getResource("enrichment.txt");
		final String text = IOUtils.toString(resource, Charset.defaultCharset());
		final AnalysisResult result = AnalysisClient.performAnalysis(text);
		return result.getSummary().getToken();
	}

	private String createExpressionToken() throws IOException, AnalysisException, AnalysisServerError {
		final URL resource = getClass().getResource("expression.txt");
		final String text = IOUtils.toString(resource, Charset.defaultCharset());
		final AnalysisResult result = AnalysisClient.performAnalysis(text);
		return result.getSummary().getToken();
	}

}
