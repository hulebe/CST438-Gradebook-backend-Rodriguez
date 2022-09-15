package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cst438.controllers.AssignmentController;
import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { AssignmentController.class })
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest
public class TestAddAssignment {
	static final String URL = "http://localhost:8080";
	public static final int TEST_COURSE_ID = 40442;
	public static final String TEST_STUDENT_EMAIL = "test@csumb.edu";
	public static final String TEST_STUDENT_NAME = "test";
	public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
	public static final int TEST_YEAR = 2021;
	public static final String TEST_SEMESTER = "Fall";
	
	@MockBean
	AssignmentRepository assignmentRepository;

	@MockBean
	CourseRepository courseRepository; // must have this to keep Spring test happy

	
	@Autowired
	private MockMvc mvc;

	@Test
	public void addAssignment() throws Exception {

		MockHttpServletResponse response;

		// mock database data

		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
	
		// given -- stubs for database repositories that return test data
		given(courseRepository.findById(TEST_COURSE_ID)).willReturn(Optional.of(course));
		// end of mock data
		
		// set up a mock for the assignment repository save method that returns an Assignment entity with a primary key
		Assignment a = new Assignment();
		a.setId(123);
		
		given(assignmentRepository.save(any())).willReturn(a);

		// then do an http post to add an assignment
		AssignmentListDTO.AssignmentDTO aDTO = new AssignmentListDTO.AssignmentDTO();
		
		// setting values for name, courseId, dueDate
		aDTO.assignmentName = "test assignment";
		aDTO.courseId = TEST_COURSE_ID;
		aDTO.dueDate = "2022-09-12";
		
		// make the post call to add the assignment
		response = mvc.perform(MockMvcRequestBuilders.post("/assignment")
				.accept(MediaType.APPLICATION_JSON)
				.content(asJsonString(aDTO))
				.contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify return data with entry for one student without no score
		assertEquals(200, response.getStatus());
		
		// get response body and convert to Java object
		AssignmentListDTO.AssignmentDTO returnedDTO = fromJsonString(response.getContentAsString(), AssignmentListDTO.AssignmentDTO.class);
		
		// check that returned assignmentId is not 0
		assertEquals(123, returnedDTO.assignmentId);

		// verify that a save was called on repository
		verify(assignmentRepository, times(1)).save(any()); // verify that assignment Controller actually did a save to the database.

	}
	
	@Test
	public void changeAssignmentName() throws Exception {
		MockHttpServletResponse response;

		// mock database data

		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setAssignments(new java.util.ArrayList<Assignment>());

		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		course.getAssignments().add(assignment);
		assignment.setId(1);
		assignment.setName("Assignment 1");
		assignment.setNeedsGrading(1);
		
		AssignmentListDTO.AssignmentDTO aDTO = new AssignmentListDTO.AssignmentDTO();
		aDTO.assignmentName = "Updated Assignment Name";

		// given -- stubs for database repositories that return test data
		given(assignmentRepository.findById(1)).willReturn(Optional.of(assignment));

		// end of mock data

		// make call to update assignment name
		response = mvc.perform(MockMvcRequestBuilders
				.put("/assignment/1")
				.accept(MediaType.APPLICATION_JSON)
				.content(asJsonString(aDTO))
				.contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify return data with entry for one student without no score
		assertEquals(200, response.getStatus());
		
		// verify that a save was called on repository
		verify(assignmentRepository, times(1)).save(any()); // verify that assignment Controller actually did a save to the database.

	}
	
	@Test
	public void deleteAssignment() throws Exception {
		MockHttpServletResponse response;

		// mock database data

		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setAssignments(new java.util.ArrayList<Assignment>());

		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		assignment.setId(1);
		assignment.setNeedsGrading(0);

		// given -- stubs for database repositories that return test data
		given(assignmentRepository.findById(1)).willReturn(Optional.of(assignment));

		// end of mock data
	    response = mvc.perform(MockMvcRequestBuilders.delete("/assignment/1"))
	              .andReturn().getResponse();

	    // verify that return status = OK (value 200)
	    assertEquals(200, response.getStatus());
	     
	}
	
	private static String asJsonString(final Object obj) {
		try {

			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T fromJsonString(String str, Class<T> valueType) {
		try {
			return new ObjectMapper().readValue(str, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
