package com.cst438.controllers;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;

@RestController
@CrossOrigin(origins = {"http://localhost:3000"})
public class AssignmentController {
	
	@Autowired
	CourseRepository courseRepository;
	
	@Autowired
	AssignmentRepository assignmentRepository;
	
	// Add new assignment
	@PostMapping("/assignment")
	@Transactional
	public AssignmentListDTO.AssignmentDTO addAssignment(@RequestBody AssignmentListDTO.AssignmentDTO assignmentDTO ) {
		System.out.println("check called");
		// look up the course
		Course c = courseRepository.findById(assignmentDTO.courseId).get();
		
		if (c==null) {
			// invalid error
			throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Course not found. "+assignmentDTO.courseId );
		}
		// create a new assignment entity
		Assignment assignment = new Assignment();
		
		// copy data from assignmentDTO to assignment
		assignment.setName(assignmentDTO.assignmentName);
		
		// convert dueDate string to dueDate java.sql.Date
		Date date = Date.valueOf(assignmentDTO.dueDate);
		assignment.setDueDate(date);
		
		assignment.setCourse(c);
		
		// save the assignment entity, save returns an updated assignment entity with assignment id primary key
		Assignment newAssignment = assignmentRepository.save(assignment);
		
		assignmentDTO.assignmentId = newAssignment.getId();
		
		// return assignmentDTO that now contains the primary key
		return assignmentDTO;
	}
	
	// Change the name of the assignment for my course 
	@PutMapping("/assignment/{assignmentId}")
	@Transactional
	public AssignmentListDTO.AssignmentDTO changeAssignmentName(@PathVariable int assignmentId, @RequestBody AssignmentListDTO.AssignmentDTO assignmentDTO) {
		
		String email = "dwisneski@csumb.edu";
		
		//Find assignment by ID
		Assignment assignment = checkAssignment(assignmentId,email);
		
		if(assignment == null) {
			throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Not Authorized. " );
		}
		else {
		// Update assignment name and save 
		assignment.setName(assignmentDTO.assignmentName);
		assignmentRepository.save(assignment);
		}
		return assignmentDTO;
	}
	
	// Delete an assignment for the course (only if there are no grades for the assignment)
	@DeleteMapping("/assignment/{assignmentId}")
	@Transactional
	public void deleteAssignment(@PathVariable int assignmentId) {
		
		String email = "dwisneski@csumb.edu";
		
		//Find assignment by ID
		Assignment assignment = checkAssignment(assignmentId,email);
		
		if(assignment.getNeedsGrading() == 0) {
			assignmentRepository.delete(assignment);
		} else {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Can't delete, assignment has been graded.");
		}
		
	}

	private Assignment checkAssignment(int assignmentId, String email) {
		// get assignment 
		Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
		if (assignment == null) {
			throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Assignment not found. "+assignmentId );
		}
		// check that user is the course instructor
		if (!assignment.getCourse().getInstructor().equals(email)) {
			throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Not Authorized. " );
		}
		
		return assignment;
	}
	
}
