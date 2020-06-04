/**
 * 
 */
package com.dal.catmeclone.course;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dal.catmeclone.UserProfile.UserDao;
import com.dal.catmeclone.exceptionhandler.CourseException;
import com.dal.catmeclone.exceptionhandler.DuplicateUserRelatedException;
import com.dal.catmeclone.exceptionhandler.UserDefinedSQLException;
import com.dal.catmeclone.model.Course;
import com.dal.catmeclone.model.Role;
import com.dal.catmeclone.model.User;
import com.dal.catmeclone.notification.NotificationService;

/**
 * @author Mayank
 *
 */
@Service
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CourseEnrollmentServiceImpl.class);
	
	public List<String> recordsSuccessMessage = new ArrayList<String>();
	public List<String> recordsFailureMessage = new ArrayList<String>();

	@Autowired
	NotificationService notificationService;

	@Autowired
	UserDao userDB;

	@Autowired
	CourseEnrollmentDao courseEnrollDB;



	private static final String email_regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

	public List<String> getRecordsSuccessMessage() {
		return recordsSuccessMessage;
	}

	public List<String> getRecordsFailureMessage() {
		return recordsFailureMessage;
	}

	/*
	 * Function to iterate data obatined from loadDataFromCSV over the student list and make a call to enroll
	 * student one by one.
	 */
	@Override
	public boolean enrollStudentForCourse(MultipartFile file, Course course) {
		// TODO Auto-generated method stub
		recordsSuccessMessage = new ArrayList<String>();
		recordsFailureMessage = new ArrayList<String>();
		Set<User> usersToBeEnrolled = new HashSet<User>();
		usersToBeEnrolled = loadDataFromCSV(file);

		Iterator<User> listIterator = usersToBeEnrolled.iterator();
		while (listIterator.hasNext()) {
			User student = listIterator.next();
			try {
				// call enrollStudent method to enroll student
				enrollStudent(student, course);
			} catch (UserDefinedSQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}

	/*
	 * function to load and parse the data from given csv file into set of student objects.
	 */
	private Set<User> loadDataFromCSV(MultipartFile file) {
		Set<User> usersToBeEnrolled = new HashSet<User>();
		BufferedReader br = null;
		try {

			br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			String line = br.readLine();
			int count = 1;
			// reading the file line by line 
			LOGGER.info("START: Parsing the Provided CSV File: "+file.getName());
			while ((line = br.readLine()) != null) {
				// considering comma as separator for csv
				String[] details = line.split(",");
				if (4 == details.length) {
					// validating if banner id field is null or empty
					if ( null != details[0] && !details[0].isEmpty()) {
						// validating if email field is null or empty
						if (null != details[3] && !details[3].isEmpty()) {
							Pattern pattern = Pattern.compile(email_regex);
							Matcher matcher = pattern.matcher(details[3]);
							// validating email pattern
							if (matcher.matches()) {
								User student = new User(details[0], details[1], details[2], details[3]);
								usersToBeEnrolled.add(student);
							} else {
								LOGGER.info("Email provided at row " + (count) + " is not valid email");
								recordsFailureMessage.add("Email provided at row " + (count) + " is not valid email");
							}
						} else {
							LOGGER.info("Email provided at row " + (count) + "is not valid");
							recordsFailureMessage.add("Email provided at row " + (count) + "is not valid");
						}
					} else {
						LOGGER.info("Banner ID provided at row " + (count) + " is not valid");
						recordsFailureMessage.add("Banner ID provided at row " + (count) + " is not valid");
					}
				} else {
					LOGGER.info("Details provided at row " + (count) + " are not valid");
					recordsFailureMessage.add("Details provided at row " + (count) + " is	 not valid");
				}
				count++;
			}
			LOGGER.info("END: Parsing the Provided CSV File");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return usersToBeEnrolled;
	}

	/*
	 * function to check if user exists and enroll or create user accordingly
	 */
	private void enrollStudent(User user, Course course) throws UserDefinedSQLException {

		// Check if user exists with the given banner id or not
		User u = userDB.findUserByBannerID(user.getBannerId());

		if (null != u) {
			
			// Checking is user has already enrolled in the course
			if (!courseEnrollDB.hasEnrolledInCourse(user.getBannerId(), course.getCourseID() )) {
				Role role = new Role("Student");
				courseEnrollDB.enrollUserForCourse(user, course, role);
				LOGGER.info(
						"User with BannerId: " + user.getBannerId() + " enroll sucessfully as student to the course");
				recordsSuccessMessage.add(
						"User with BannerId: " + user.getBannerId() + " enroll sucessfully as student to the course");
			} else {
				LOGGER.info("User with " + user.getBannerId() + " already enrolled in the course");
				recordsFailureMessage.add("User with " + user.getBannerId() + " already enrolled in the course");
			}
		}
		// If user is not existing. create a profile for user and enroll user in course
		else {
			user.setPassword("Password123");
			boolean isCreated = false;
			try {
				// Create User in System
				isCreated = userDB.createUser(user);
			} catch (DuplicateUserRelatedException e) {
				// Handle error for error thrown is another exist with same email id
				recordsFailureMessage.add("User already exists with " + user.getEmail());
			}
			if (isCreated) {
				LOGGER.info("User created successfully");
				Role role = new Role("Student");
				courseEnrollDB.enrollUserForCourse(user, course, role);
				LOGGER.info(
						"User with BannerId: " + user.getBannerId() + " enroll sucessfully as student to the course");
				notificationService.sendNotificationToNewuser(user, course);
				LOGGER.info("Notification email send to user");
				recordsSuccessMessage.add("User with BannerId: " + user.getBannerId()
						+ " created and enroll sucessfully as student to the course");
			}

		}
	}

	@Override
	public boolean enrollTAForCourse(User Ta, Course course) {
		// TODO Auto-generated method stub
		Role role = new Role("TA");
		boolean response = false;
		try {
			response = courseEnrollDB.enrollUserForCourse(Ta, course, role);

		} catch (UserDefinedSQLException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return response;
	}


	
	@Override
	public List<Course> getCourseEnrolledForUser(User user) throws UserDefinedSQLException {
		
		//calling Database access layer to get the list of user enrolled in course
		List<Course> listofCourses = new ArrayList<Course>();
		listofCourses= courseEnrollDB.getAllEnrolledCourse(user);
		return listofCourses;
		
	}

	@Override
	public Role getUserRoleForCourse(User user, Course course) throws UserDefinedSQLException {
		// TODO Auto-generated method stub
		Role role=null;
		role = courseEnrollDB.getUserRoleForCourse(user, course);
		return role;
	}

}