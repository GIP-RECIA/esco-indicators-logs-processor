package org.esco.indicators.backend.model;

import java.util.Date;

public class SstartValue {
	private Date date;
	private boolean fin;

	public SstartValue(final LogLine logLine) {
		this.date = logLine.getDate();
		this.fin = false;
	};

	public Date getDate() {
		return this.date;
	}

	public boolean isFin() {
		return this.fin;
	}

	public void terminate() {
		this.fin = true;
	}

	@Override
	public String toString() {
		return this.date + "\t" + this.fin;
	}

}
