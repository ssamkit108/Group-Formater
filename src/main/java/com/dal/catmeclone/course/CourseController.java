package com.dal.catmeclone.course;

import com.dal.catmeclone.AbstractFactory;
import com.dal.catmeclone.SystemConfig;
import com.dal.catmeclone.exceptionhandler.UserDefinedException;
import com.dal.catmeclone.model.Course;
import com.dal.catmeclone.model.Role;
import com.dal.catmeclone.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CourseController {

    AbstractFactory abstractFactory = SystemConfig.instance().getAbstractFactory();
    CourseAbstractFactory courseAbstractFactory = abstractFactory.createCourseAbstractFactory();
    CourseService courseService;
    CourseEnrollmentService courseEnrollmentService;

    @GetMapping("mycourse/{courseid}")
    public String showCoursePage(ModelMap model, @PathVariable(name = "courseid") Integer courseid,
                                 RedirectAttributes attributes, HttpSession session) {

        courseService = courseAbstractFactory.createCourseService();
        courseEnrollmentService = courseAbstractFactory.createCourseEnrollmentService();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String responsepage = new String();

        try {
            Course course = courseService.getCourse(courseid);
            if (course != null) {
                Role role = courseEnrollmentService.getUserRoleForCourse(new User(username), course);

                if (role == null) {

                    attributes.addAttribute("InvalidAccessMessage", "Course " + courseid
                            + "doesn't exist in system or access to course " + courseid + " is not provided for you");
                    responsepage = "redirect:/access-denied";

                } else if (role.getRoleName().equals("Instructor")) {

                    session.setAttribute("role", "Instructor");
                    session.setAttribute("course", course);
                    responsepage = "CI-course";

                } else if (role.getRoleName().equals("Student")) {

                    session.setAttribute("role", "Student");
                    session.setAttribute("course", course);
                    responsepage = "coursestudentpage";

                } else if (role.getRoleName().equals("TA")) {

                    session.setAttribute("role", "TA");
                    session.setAttribute("course", course);
                    responsepage = "CI-course";
                }
            }
        } catch (UserDefinedException exception) {
            attributes.addAttribute("InvalidAccessMessage", exception.getLocalizedMessage());
            return "redirect:/access-denied";
        }
        return responsepage;
    }

    @GetMapping("/allcourses")
    public ModelAndView AllCourses(Model model) {
        courseService = courseAbstractFactory.createCourseService();
        ModelAndView modelview = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated()) {
            ArrayList<Course> allcourses = null;
            try {
                allcourses = courseService.getallcourses();
            } catch (SQLException e) {
                modelview = new ModelAndView("error");
                model.addAttribute("errormessage", "Some Error occured");
            } catch (UserDefinedException e) {
                modelview = new ModelAndView("error");
                model.addAttribute("errormessage", "Some Error occured");

            }
            modelview = new ModelAndView("guest_courses");
            if (!allcourses.isEmpty()) {
                modelview.addObject("all_courses", allcourses);
            } else {
                modelview.addObject("nocoursemessage", "No Courses available in the System");
            }

        }
        return modelview;

    }

    @GetMapping("/courses")
    public ModelAndView Courses(Model model, HttpSession session) {

        courseService = courseAbstractFactory.createCourseService();
        courseEnrollmentService = courseAbstractFactory.createCourseEnrollmentService();
        // Interface_AuthenticateUserDao validate = new AuthenticateUserDao();
        ModelAndView modelview = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        session.removeAttribute("course");
        List<Course> listofCourses = new ArrayList<Course>();
        try {
            listofCourses = courseEnrollmentService.getCourseEnrolledForUser(new User(username));

            if (!listofCourses.isEmpty()) {
                modelview = new ModelAndView("course_list");
                modelview.addObject("courses", listofCourses);

                session.setAttribute("enrolled", true);
                modelview.addObject("nocoursemessage", "");
            } else {
                modelview = new ModelAndView("guest_courses");
                session.setAttribute("enrolled", false);
                ArrayList<Course> allcourses = null;
                try {
                    allcourses = courseService.getallcourses();
                    if (!allcourses.isEmpty()) {
                        modelview.addObject("all_courses", allcourses);
                    } else {
                        modelview.addObject("nocoursemessage", "No Courses available in the System");
                    }
                } catch (SQLException e) {
                    modelview = new ModelAndView("error");
                    model.addAttribute("errormessage", "Some Error occured");
                } catch (UserDefinedException e) {
                    modelview = new ModelAndView("error");
                    model.addAttribute("errormessage", "Some Error occured");
                }

            }

        } catch (UserDefinedException e) {
            modelview = new ModelAndView("error");
            model.addAttribute("errormessage", "Some Error occured");

        }
        return modelview;
    }
}
