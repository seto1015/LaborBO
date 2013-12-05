package de.hska.iwii.picturecommunity.chart;

import org.primefaces.model.chart.PieChartModel;

public class Bean {
	private PieChartModel model;

	public Bean() {
		model = new PieChartModel();
		model.set("Brand 1", 540);
		model.set("Brand 2", 325);
		model.set("Brand 3", 702);
		model.set("Brand 4", 421);
	}

	public PieChartModel getModel() {
		return model;
	}
}