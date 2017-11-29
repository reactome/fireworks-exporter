package org.reactome.server.tools.fireworks.exporter.raster.index;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ContentServiceResponse {

	@JsonIgnore
	private String stId;

	private Long dbId;

	@JsonIgnore
	private String displayName;

	@JsonIgnore
	private String schemaClass;

	@JsonIgnore
	private String className;

	@JsonIgnore
	private String speciesName;

	@JsonIgnore
	private String name;

	@JsonIgnore
	private Boolean isInDisease;

	@JsonIgnore
	private Boolean isInferred;

	@JsonIgnore
	private Boolean hasDiagram;

	public String getStId() {
		return stId;
	}

	public Long getDbId() {
		return dbId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getSchemaClass() {
		return schemaClass;
	}

	public String getName() {
		return name;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public String getClassName() {
		return className;
	}

	public Boolean getInDisease() {
		return isInDisease;
	}

	public Boolean getInferred() {
		return isInferred;
	}

	public Boolean getHasDiagram() {
		return hasDiagram;
	}
}
