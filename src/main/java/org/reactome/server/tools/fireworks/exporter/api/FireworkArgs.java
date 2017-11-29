package org.reactome.server.tools.fireworks.exporter.api;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;

public class FireworkArgs {
	private final String format;
	private String speciesName;
	private String profile;
	private Double factor = 1.;
	private Color background;
	private Collection<String> selected;
	private Collection<String> flags;

	public FireworkArgs(String speciesName, String format) {
		this.speciesName = speciesName;
		this.format = format;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public String getFormat() {
		return format;
	}

	public String getProfile() {
		return profile;
	}

	public Double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public Color getBackground() {
		return background;
	}

	public Collection<String> getSelected() {
		return selected;
	}

	public void setSelected(Collection<String> selected) {
		if (selected != null)
			this.selected = new HashSet<>(selected);
	}

	public Collection<String> getFlags() {
		return flags;
	}

	public void setFlags(Collection<String> flags) {
		if (flags != null)
			this.flags = new HashSet<>(flags);
	}
}
