package org.reactome.server.tools.fireworks.exporter.common.api;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;

public class FireworkArgs {
	private final String format;
	private final String speciesName;
	private String profile;
	private Color background;
	private Collection<String> selected;
	private Collection<String> flags;
	private String token;
	private String resource;
	private Integer column;
	private Boolean writeTitle;
	private Integer quality = 5;
	private Boolean coverage;
	private Double factor = scale(quality);
	private Integer margin = 15;


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

	public FireworkArgs setProfile(String profile) {
		this.profile = profile;
		return this;
	}

	public Double getFactor() {
		return factor;
	}

	public Color getBackground() {
		return background;
	}

	public void setBackground(Color background) {
		this.background = background;
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

	public String getToken() {
		return token;
	}

	public FireworkArgs setToken(String token) {
		this.token = token;
		return this;
	}

	public String getResource() {
		return resource;
	}

	public FireworkArgs setResource(String resource) {
		this.resource = resource;
		return this;
	}

	public Integer getColumn() {
		return column;
	}

	public FireworkArgs setColumn(Integer column) {
		this.column = column;
		return this;
	}

	public Boolean getWriteTitle() {
		return writeTitle;
	}

	public FireworkArgs setWriteTitle(Boolean writeTitle) {
		this.writeTitle = writeTitle;
		return this;
	}

	public Integer getQuality() {
		return quality;
	}

	public FireworkArgs setQuality(Integer quality) {
		if (quality != null) {
			this.quality = quality;
			this.factor = scale(quality);
		}
		return this;
	}

	public Integer getMargin() {
		return margin;
	}

	public FireworkArgs setMargin(Integer margin) {
		if (margin != null)
			this.margin = Math.max(0, Math.min(20, margin));
		return this;
	}

	private double scale(int quality) {
		if (quality < 1 || quality > 10)
			throw new IllegalArgumentException("quality must be in the range [1-10]");
		if (quality < 5) {
			return interpolate(quality, 1, 5, 0.1, 1);
		} else return interpolate(quality, 5, 10, 1, 3);
	}

	private double interpolate(double x, double min, double max, double dest_min, double dest_max) {
		return (x - min) / (max - min) * (dest_max - dest_min) + dest_min;
	}


	public Boolean getCoverage() {
		return coverage == null ? false : coverage;
	}

	public void setCoverage(Boolean coverage) {
		this.coverage = coverage;
	}
}
