package org.truffautronic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeLabelsBundle {

	private List<TimeLabel> timeLabels = new ArrayList<TimeLabel>();

	public TimeLabelsBundle() {
	}

	public void addLabel(TimeLabel timeLabel) {
		timeLabels.add(timeLabel);
		Collections.sort(timeLabels);
	}

	public List<TimeLabel> getLabels() {
		return Collections.unmodifiableList(timeLabels);
	}

	public TimeLabel getNearestLabel(Duration position) {
		long minDelta = Long.MAX_VALUE;
		TimeLabel nearest = null;
		// Brute-force, could use bisect
		for (TimeLabel timeLabel : timeLabels) {
			long delta = Math.abs(timeLabel.getPosition().getMs()
					- position.getMs());
			if (delta < minDelta) {
				minDelta = delta;
				nearest = timeLabel;
			}
		}
		return nearest; // Can be null
	}

	public void deleteTimeLabel(TimeLabel timeLabel) {
		timeLabels.remove(timeLabel);
	}

}
