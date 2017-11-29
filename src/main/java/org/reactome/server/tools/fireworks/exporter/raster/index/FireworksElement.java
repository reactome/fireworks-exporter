package org.reactome.server.tools.fireworks.exporter.raster.index;

public class FireworksElement {

	private boolean selected;
	private boolean flag;
	private Double pValue;

	public void setpValue(Double pValue) {
		this.pValue = pValue;
	}

	public Double getpValue() {
		return pValue;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
