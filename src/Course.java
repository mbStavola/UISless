public class Course {

	/**
	 * @author Kevin Most
	 * @author Matthew Stavola
	 * @dateCreated Nov 3, 2013
	 */
	private int crn;
	private String subject;
	private int courseNumber;
	
	private int section;
	private char campus;
	private int credits;
	
	private String title;
	
	private String days;
	private String startTime; // Format as hhmm (ex: 3:15 as "1515")
	private String endTime;
	private String startDate; // Format as yyyymmdd
	private String endDate;
	
	private int seatsRemaining;
	private int waitlistActual;
	private int waitlistRemaining;
	
	private String instructor;

	public int getCrn() {
		return crn;
	}

	public void setCrn(int crn) {
		this.crn = crn;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public int getCourseNumber() {
		return courseNumber;
	}

	public void setCourseNumber(int courseNumber) {
		this.courseNumber = courseNumber;
	}

	public int getSection() {
		return section;
	}

	public void setSection(int section) {
		this.section = section;
	}

	public char getCampus() {
		return campus;
	}

	public void setCampus(char campus) {
		this.campus = campus;
	}

	public int getCredits() {
		return credits;
	}

	public void setCredits(int credits) {
		this.credits = credits;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		this.days = days;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public int getSeatsRemaining() {
		return seatsRemaining;
	}

	public void setSeatsRemaining(int seatsRemaining) {
		this.seatsRemaining = seatsRemaining;
	}

	public int getWaitlistActual() {
		return waitlistActual;
	}

	public void setWaitlistActual(int waitlistActual) {
		this.waitlistActual = waitlistActual;
	}

	public int getWaitlistRemaining() {
		return waitlistRemaining;
	}

	public void setWaitlistRemaining(int waitlistRemaining) {
		this.waitlistRemaining = waitlistRemaining;
	}

	public String getInstructor() {
		return instructor;
	}

	public void setInstructor(String instructor) {
		this.instructor = instructor;
	}


}
