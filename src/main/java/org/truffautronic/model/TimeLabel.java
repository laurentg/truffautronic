package org.truffautronic.model;

public class TimeLabel implements Comparable<TimeLabel> {

	private Duration position;
	private String name;

	public TimeLabel(Duration position, String name) {
		this.position = position;
		this.name = name;
	}

	public Duration getPosition() {
		return position;
	}

	public String getName() {
		return name;
	}

	@Override
	public int compareTo(TimeLabel o) {
		return Long.compare(position.getMs(), o.position.getMs());
	}
}
