package com.cst438;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;

/*
 * This example shows how to use selenium testing using the web driver 
 * with Chrome browser.
 * 
 *  - Buttons, input, and anchor elements are located using XPATH expression.
 *  - onClick( ) method is used with buttons and anchor tags.
 *  - Input fields are located and sendKeys( ) method is used to enter test data.
 *  - Spring Boot JPA is used to initialize, verify and reset the database before
 *      and after testing.
 *      
 *  In SpringBootTest environment, the test program may use Spring repositories to 
 *  setup the database for the test and to verify the result.
 */

@SpringBootTest
public class EndToEndTestCreateAssignment {

	public static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver_win32/chromedriver.exe";

	public static final String URL = "http://localhost:3000";
	public static final String TEST_USER_EMAIL = "test@csumb.edu";
	public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
	public static final int SLEEP_DURATION = 1000; // 1 second.
	public static final String TEST_ASSIGNMENT_NAME = "Test Assignment Week 5";
	public static final String TEST_ASSIGNMENT_DUE_DATE = "2022-10-04";
	public static final int TEST_COURSE_ID = 123456;

	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	AssignmentGradeRepository assignnmentGradeRepository;

	@Autowired
	AssignmentRepository assignmentRepository;

	@Test
	public void createAssignmentTest() throws Exception {
		
		Assignment ca = null;

		// set the driver location and start driver
		//@formatter:off
		// browser	property name 				Java Driver Class
		// edge 	webdriver.edge.driver 		EdgeDriver
		// FireFox 	webdriver.firefox.driver 	FirefoxDriver
		// IE 		webdriver.ie.driver 		InternetExplorerDriver
		//@formatter:on
		
		/*
		 * initialize the WebDriver and get the home page. 
		 */

		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
		WebDriver driver = new ChromeDriver();
		// Puts an Implicit wait for 10 seconds before throwing exception
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		driver.get(URL);
		Thread.sleep(SLEEP_DURATION);
		

		try {
			WebElement we;
			
			// find and click on Add Assignment button
			driver.findElement(By.xpath("//a[last()]")).click();
			Thread.sleep(SLEEP_DURATION);
			
			// locate input fields for add assignment and enter data
			we = driver.findElement(By.name("assignmentName"));
			we.sendKeys(TEST_ASSIGNMENT_NAME);
			we = driver.findElement(By.name("dueDate"));
			we.sendKeys(TEST_ASSIGNMENT_DUE_DATE);
			we = driver.findElement(By.name("courseId"));
			we.sendKeys("123456");
			
			/*
			 *  Locate submit button and click
			 */
			driver.findElement(By.id("submit")).click();
			Thread.sleep(SLEEP_DURATION);
			
			// verify new assignment has been created in the database
			ca = assignmentRepository.findByCourseIdAndAssignmentName(TEST_COURSE_ID, TEST_ASSIGNMENT_NAME);
			assertNotNull(ca);

		} catch (Exception ex) {
			throw ex;
		} finally {

			/*
			 *  clean up database so the test is repeatable.
			 */
			ca = assignmentRepository.findByCourseIdAndAssignmentName(TEST_COURSE_ID, TEST_ASSIGNMENT_NAME);
			if (ca != null)
				assignmentRepository.delete(ca);
			
			driver.quit();
		}

	}
}
