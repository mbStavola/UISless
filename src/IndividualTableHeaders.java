
public enum IndividualTableHeaders {
	TYPE(0),
	TIME(1),
	DAYS(2),
	WHERE(3),
	DATE_RANGE(4),
	SCHEDULE_TYPE(5),
	INSTRUCTORS(6);
	
	private int index;
	IndividualTableHeaders(int index) {
		this.index = index;
	}
	int getIndex() {
		return index;
	}

}
