package org.reactome.server.tools.fireworks.exporter.raster;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.w3c.dom.svg.SVGDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class FireworksOutput {


	public static void save(SVGDocument document, OutputStream os) throws TranscoderException {
		final SVGTranscoder transcoder = new SVGTranscoder();
		final TranscoderInput input = new TranscoderInput(document);
		final TranscoderOutput output = new TranscoderOutput(new OutputStreamWriter(os));
		transcoder.transcode(input, output);
	}

	public static void save(BufferedImage image, String format, OutputStream os) throws IOException {
		ImageIO.write(image, format, os);
	}
}
