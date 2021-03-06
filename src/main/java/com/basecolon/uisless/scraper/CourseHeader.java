package com.basecolon.uisless.scraper;

public enum CourseHeader {
	OPEN(0),
	CRN(1),
	SUBJECT(2),
	COURSE_NUMBER(3),
	SECTION(4),
	CAMPUS(5),
	CREDITS(6),
	TITLE(7),
	DAYS(8),
	TIME(9),
	REMAINING_SEATS(10),
	WL_ACT(11),
	WL_REM(12),
	INSTRUCTOR(13),
	DATE (14);
	
	private int index;
	CourseHeader(int index) {
		this.index = index;
	}
	int getIndex() {
		return index;
	}
}
