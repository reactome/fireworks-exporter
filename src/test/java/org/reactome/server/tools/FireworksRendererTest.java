package org.reactome.server.tools;


import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.tools.fireworks.exporter.FireworksExporter;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Unit test for simple FireworksRenderer.
 */
public class FireworksRendererTest {
	private static final String TOKEN_OVER_1 = "MjAyMDA4MTExNTU2NDBfMzg%253D"; // uniprot (GBM Uniprot)
	private static final String TOKEN_EXPRESSION_1 = "MjAyMDA4MTExNTU3MTZfMzk%253D";  // microarray (probeset)
	private static final String TOKEN_EXPRESSION_2 = "MjAyMDA4MTExNTU3NTNfNDA%253D";  // HPA (GeneName)
	private static final String TOKEN_SPECIES = "MjAyMDA4MTExNTU4MjBfNDE%253D"; // canis

	private static final String TOKEN_TISSUE = "MjAyMDA3MjQyMDM5NTlfMTM%253D"; // tissue with 35 columns
	private static final String TOKEN_COSMIC = "MjAyMDA3MjQxOTE1NTJfMTI%253D"; // COSMIC
	private static final String TOKEN_GSA = "MjAyMDA3MTYxMjA5MTNfNw%253D%253D";

	private static final String[] IMAGE_FORMAT = {"jpg", "gif", "png", "svg"};
	private static List<String> ALL_TOKENS;
	private static final String ANALYSIS_PATH = "src/test/resources/org/reactome/server/tools/fireworks/exporter/analysis";
	private static final String FIREWORK_PATH = "src/test/resources/org/reactome/server/tools/fireworks/exporter/layouts";
	private static final File IMAGE_FOLDER = new File("test-image");
	private static final boolean save = true;

	private static final TokenUtils TOKEN_UTILS = new TokenUtils(ANALYSIS_PATH);
	private static final FireworksExporter exporter = new FireworksExporter(FIREWORK_PATH, ANALYSIS_PATH);


	@BeforeClass
	public static void beforeClass() {
		createImageDir();
		ALL_TOKENS = new ArrayList<>();
		ALL_TOKENS.addAll(Arrays.asList(TOKEN_TISSUE, TOKEN_COSMIC, TOKEN_GSA));

	}

	private static void createImageDir() {
		if (!IMAGE_FOLDER.exists() && !IMAGE_FOLDER.mkdirs())
			System.err.println("Couldn't create test dir " + IMAGE_FOLDER);
	}

	@AfterClass
	public static void afterClass() {
		if (!save) removeDir(IMAGE_FOLDER);
	}

	private static void removeDir(File dir) {
		if (!dir.exists()) return;
		final File[] files = dir.listFiles();
		if (files == null) return;
		for (File child : files)
			if (!child.delete())
				System.err.println("Couldn't delete file " + child);
		if (!dir.delete())
			System.err.println("Couldn't delete dir " + dir);
	}

	@Test
	public void testSimple() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "jpg");
		args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
		render(args, null, "Homo_sapiens");
	}

	@Test
	public void testToSvg() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "svg");
		try {
			final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens.svg"));
			exporter.render(args, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testFlags() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setQuality(8);
		args.setFlags(Collections.singletonList("R-HSA-450294"));
		args.setWriteTitle(true);
		render(args, null, "Homo_sapiens_flags");
	}

	@Test
	public void testEnrichment() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setQuality(8);
		args.setSelected(Collections.singletonList("R-HSA-196783"));
		args.setToken(TOKEN_OVER_1);
		render(args, null, "Homo_sapiens_overrpresentation");
	}

	@Test
	public void testExpression() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		final AnalysisStoredResult result = TOKEN_UTILS.getFromToken(TOKEN_EXPRESSION_1);
		render(args, result, "Homo_sapiens_expression1");
	}

	@Test
	public void testExpressionSettingArguments() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setQuality(8);
		args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
		args.setProfile("Calcium Salts");
		args.setColumn(1);
		args.setToken(TOKEN_EXPRESSION_1);
		render(args, null, "Homo_sapiens_expression1_column1_profile");
	}

	@Test
	public void testAnimatedGif() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "gif");
		args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
		args.setProfile("Copper plus");
		args.setWriteTitle(true);
		args.setToken(TOKEN_EXPRESSION_1);
		renderGif(args, null, "Homo_sapiens_expression1");
	}

	@Test
	public void testTitleNoAnalysis() {
		final FireworkArgs args = new FireworkArgs("Canis_familiaris", "png");
		args.setWriteTitle(true);
		render(args, null, "Canis_familiaris");
	}

	@Test
	public void testTitleExpression() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setWriteTitle(true);
		render(args, TOKEN_UTILS.getFromToken(TOKEN_EXPRESSION_1), "Homo_sapiens_expression1_title");
	}

	@Test
	public void testTitleOverRepresentation() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setWriteTitle(true);
		render(args, TOKEN_UTILS.getFromToken(TOKEN_OVER_1), "Homo_sapiens_overrepresentation_title");
	}

	@Test
	public void testTitleSpeciesComparison() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setWriteTitle(true);
		render(args, TOKEN_UTILS.getFromToken(TOKEN_SPECIES), "Homo_sapiens_species");
	}

	@Test
	public void testToSvgWithAnalysis() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "svg");
		args.setToken(TOKEN_OVER_1);
		args.setSelected(Arrays.asList("R-HSA-169911", "R-HSA-3560792"));
		try {
			final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens_overrepresentation.svg"));
			exporter.render(args, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testPdf() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "pdf");
		final AnalysisStoredResult result = new TokenUtils(ANALYSIS_PATH).getFromToken(TOKEN_EXPRESSION_2);
		try {
			final OutputStream os = save
					? new NullOutputStream()
					: new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens.pdf"));
			exporter.render(args, result, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGSAAnalysisJPG() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "jpg");
		args.setToken(TOKEN_GSA);
		try {
			final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens_GSA.jpg"));
			exporter.render(args, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGSAAnalysisSVG() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "svg");
		args.setToken(TOKEN_GSA);
		args.setProfile("Calcium Salts");
		args.setSelected(Collections.singletonList("R-HSA-380612"));
		try {
			final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens_GSA.svg"));
			exporter.render(args, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGSAAnalysisPNG() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "png");
		args.setToken(TOKEN_GSA);
		args.setProfile("Calcium Salts");
		args.setSelected(Collections.singletonList("R-HSA-8963678"));//"R-HSA-5676590"));//, "R-HSA-3560792"));
		try {
			final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens_GSA.png"));
			exporter.render(args, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGSAAnalysisGIF() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "gif");
		args.setToken(TOKEN_GSA)
				.setProfile("Cooper Plus")
				.setWriteTitle(true)
				.setQuality(10)
				.setSelected(Collections.singletonList("R-HSA-198753"));
		try {
			final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens_GSA.gif"));
			exporter.render(args, null, os);
		} catch (AnalysisServerError | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		catch (TranscoderException ee) {
			//
		}
	}

	@Test
	public void testTissueAnalysisSVG() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "svg");
		args.setToken(TOKEN_TISSUE);
		args.setProfile("Calcium Salts");
		args.setSelected(Collections.singletonList("R-HSA-380612"));
		try {
			final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens_TISSUE.svg"));
			exporter.render(args, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testOtherSpecies() {
		final FireworkArgs args = new FireworkArgs("Mycobacterium_tuberculosis", "svg");
		args.setWriteTitle(true);
		try {
			final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Mycobacterium_tuberculosis.svg"));
			exporter.render(args, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testTissueAnalysisGIF() {
		final FireworkArgs args = new FireworkArgs("Homo_sapiens", "gif");
		args.setToken(TOKEN_TISSUE);
		args.setProfile("Calcium Salts");
		args.setSelected(Collections.singletonList("R-HSA-380612"));
		try {
			final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens_TISSUE.gif"));
			exporter.render(args, os);
		} catch (AnalysisServerError | TranscoderException | IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	//@Test
	public void testFireworksWithAnalysisALL() {
		for (String format : IMAGE_FORMAT) {
			for (String token : ALL_TOKENS) {
				final FireworkArgs args = new FireworkArgs("Homo_sapiens", format);
				args.setToken(token);
				try {
					final FileOutputStream os = new FileOutputStream(new File(IMAGE_FOLDER, "Homo_sapiens_" + token + "." + format));
					exporter.render(args, os);
				} catch (AnalysisServerError | TranscoderException | IOException e) {
					e.printStackTrace();
					Assert.fail(e.getMessage());
				}
			}
		}
	}


	private void renderGif(FireworkArgs args, AnalysisStoredResult result, String fileName) {
		try {
			final File file = new File(IMAGE_FOLDER, fileName + "." + args.getFormat());
			final FileOutputStream outputStream = new FileOutputStream(file);
			exporter.renderGif(args, result, outputStream);
		} catch (IOException | AnalysisServerError e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void render(FireworkArgs args, AnalysisStoredResult result, String filename) {
		try {
			final BufferedImage image = exporter.renderRaster(args, result);
			if (save) saveToDisk(filename, args.getFormat(), image);
		} catch (IOException | AnalysisServerError e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void saveToDisk(String fileName, String format, BufferedImage image) throws IOException {
		final File file = new File(IMAGE_FOLDER, fileName + "." + format);
		ImageIO.write(image, format, file);
	}

}
