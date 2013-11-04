public class Course {

	/**
	 * @author Kevin Most
	 * @author Matthew Stavola
	 * @dateCreated Nov 3, 2013
	 */
	private boolean isOpen;
	private String crn;
	private String subject;
	private String courseNumber;
	
	private String section;
	private char campus;
	private String credits;
	
	private String title;
	
	private String days;
	private String startTime; // Format as hhmm (ex: 3:15 as "1515")
	private String endTime;
	private String startDate; // Format as yyyymmdd
	private String endDate;
	
	private String seatsRemaining;
	private String waitlistActual;
	private String waitlistRemaining;
	
	private String instructor;

	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	public String getCrn() {
		return crn;
	}

	public void setCrn(String crn) {
		this.crn = crn;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getCourseNumber() {
		return courseNumber;
	}

	public void setCourseNumber(String courseNumber) {
		this.courseNumber = courseNumber;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public char getCampus() {
		return campus;
	}

	public void setCampus(char campus) {
		this.campus = campus;
	}

	public String getCredits() {
		return credits;
	}

	public void setCredits(String credits) {
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

	public String getSeatsRemaining() {
		return seatsRemaining;
	}

	public void setSeatsRemaining(String seatsRemaining) {
		this.seatsRemaining = seatsRemaining;
	}

	public String getWaitlistActual() {
		return waitlistActual;
	}

	public void setWaitlistActual(String waitlistActual) {
		this.waitlistActual = waitlistActual;
	}

	public String getWaitlistRemaining() {
		return waitlistRemaining;
	}

	public void setWaitlistRemaining(String waitlistRemaining) {
		this.waitlistRemaining = waitlistRemaining;
	}

	public String getInstructor() {
		return instructor;
	}

	public void setInstructor(String instructor) {
		this.instructor = instructor;
	}
}
