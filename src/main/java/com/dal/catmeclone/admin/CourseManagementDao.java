package com.dal.catmeclone.admin;

import com.dal.catmeclone.exceptionhandler.UserDefinedSQLException;
import com.dal.catmeclone.model.Course;

import java.sql.SQLException;
import java.util.List;

public interface CourseManagementDao {
    public List<Course> getAllCourses() throws SQLException, UserDefinedSQLException;

    public boolean deleteCourse(int courseID) throws Exception;

    public boolean insertCourse(Course course) throws UserDefinedSQLException, SQLException, Exception;

    public boolean checkCourseExists(Course course) throws UserDefinedSQLException, SQLException, Exception;
}
