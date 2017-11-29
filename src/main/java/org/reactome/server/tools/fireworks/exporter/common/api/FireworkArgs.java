package org.reactome.server.tools.fireworks.exporter.common.api;

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
	private String token;
	private String resource;
	private Integer column;

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

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public Double getFactor() {
		return factor;
	}

	public void setFactor(Double factor) {
		this.factor = factor;
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

	public void setToken(String token) {
		this.token = token;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public Integer getColumn() {
		return column;
	}

	public void setColumn(Integer column) {
		this.column = column;
	}
}
