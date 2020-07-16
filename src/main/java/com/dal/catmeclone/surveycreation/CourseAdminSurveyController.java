package com.dal.catmeclone.surveycreation;

import com.dal.catmeclone.AbstractFactory;
import com.dal.catmeclone.SystemConfig;
import com.dal.catmeclone.exceptionhandler.UserDefinedException;
import com.dal.catmeclone.model.*;
import com.dal.catmeclone.questionmanagement.QuestionManagementAbstractFactory;
import com.dal.catmeclone.questionmanagement.QuestionManagementService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

@RequestMapping("/survey")
@Controller
public class CourseAdminSurveyController {

    private static final String AJAX_HEADER_NAME = "X-Requested-With";
    private static final String AJAX_HEADER_VALUE = "XMLHttpRequest";
    AbstractFactory abstractFactory = SystemConfig.instance().getAbstractFactory();
    SurveyCreationAbstractFactory surveyCreationAbstractFactory = abstractFactory.createSurveyCreationAbstractFactory();
    QuestionManagementAbstractFactory questionManagementAbstractFactory = abstractFactory
            .createQuestionManagerAbstractFactory();
    ModelAbstractFactory modelAbstractFactory = abstractFactory.createModelAbstractFactory();
    private Logger LOGGER = Logger.getLogger(CourseAdminSurveyController.class.getName());

    @GetMapping("/manage")
    private String displaySurveyManagementPage(Model model, HttpSession session) {

        QuestionManagementService questionManagementService = questionManagementAbstractFactory
                .createQuestionManagementService();
        CourseAdminSurveyService courseAdminSurveyService = surveyCreationAbstractFactory
                .createCourseAdminSurveyService();

        // getting Logged in user from authentication object
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Course course = (Course) session.getAttribute("course");

        try {
            LOGGER.info("Fetching the SurveyDetails to display on the view");
            Survey survey = courseAdminSurveyService.getSurveyDetailsForCourse(course);

            if (survey.isPublishedStatus() == false) {
                LOGGER.info("Fetching the list of questions from question bank for Displaying on View");
                model.addAttribute("questionList",
                        questionManagementService.getAllQuestionByUser(modelAbstractFactory.createUser(username)));
                model.addAttribute("question", modelAbstractFactory.createBasicQuestion());
                model.addAttribute("unsavedchanges", false);
            }

            model.addAttribute("survey", survey);

        } catch (UserDefinedException e) {
            model.addAttribute("errormessage", e.getLocalizedMessage());
            return "error";
        } catch (Exception e1) {
            model.addAttribute("errormessage", "Some error occured. Please try again later");
            return "error";
        }
        return "survey/surveymanagement";
    }

    @PostMapping(params = "addQuestion", path = {"/manage"})
    public String addQuestion(@ModelAttribute Survey survey, @RequestParam int questionId,
                              @RequestParam String questionTitle, @RequestParam String questionText, @RequestParam String questionType,
                              HttpServletRequest request, final BindingResult bindingResult, Model model) {

        BasicQuestion newQuestionForSurvey = new BasicQuestion(questionId, questionTitle, questionText, questionType);
        // fetching current list of questions and add question the list of survey question
        if (survey.getSurveyQuestions() == null) {
            ArrayList<SurveyQuestion> questions = new ArrayList<SurveyQuestion>();
            LOGGER.info("Adding Question: " + questionId + " to Survey " + survey.getSurveyId());
            questions.add(modelAbstractFactory.createSurveyQuestion(newQuestionForSurvey));
            survey.setSurveyQuestions(questions);
        } else {
            boolean addFlag = true;
            for (SurveyQuestion surveyQuestions : survey.getSurveyQuestions()) {
                if (surveyQuestions.getQuestionDetail().getQuestionId() == questionId) {
                    LOGGER.info("Cann't add Question: " + questionId + " to Survey " + survey.getSurveyId()
                            + ". Question is already added in survey");
                    model.addAttribute("addmessage", "Can not add Question. Question is already added in survey");
                    addFlag = false;
                    break;
                }
            }
            if (addFlag) {
                LOGGER.info("Adding Question: " + questionId + " to Survey " + survey.getSurveyId());
                survey.getSurveyQuestions().add(modelAbstractFactory.createSurveyQuestion(newQuestionForSurvey));
            }
        }
        if (AJAX_HEADER_VALUE.equals(request.getHeader(AJAX_HEADER_NAME))) {
            model.addAttribute("survey", survey);
            model.addAttribute("question", modelAbstractFactory.createBasicQuestion());
            model.addAttribute("unsavedchanges", true);
            // If It is an Ajax request, render only #details fragment of the page.
            return "survey/surveymanagement::#details";
        } else {
            // If It is a standard HTTP request, render whole page.
            return "survey/surveymanagement";
        }
    }

    @PostMapping(params = "removeQuestion", path = {"/manage"})
    public String removeQuestion(@ModelAttribute Survey survey, @RequestParam("removeQuestion") int questionId,
                                 HttpServletRequest request, final BindingResult bindingResult, Model model) {

        List<SurveyQuestion> listOfSurveyQuestion = survey.getSurveyQuestions();
        Iterator<SurveyQuestion> surveyQuestionIterator = listOfSurveyQuestion.iterator();
        LOGGER.info("Adding Question: " + questionId + " to Survey " + survey.getSurveyId());
        while (surveyQuestionIterator.hasNext()) {
            SurveyQuestion surveyQuestion = surveyQuestionIterator.next();
            if (surveyQuestion.getQuestionDetail().getQuestionId() == questionId) {
                surveyQuestionIterator.remove();
                break;
            }
        }
        if (AJAX_HEADER_VALUE.equals(request.getHeader(AJAX_HEADER_NAME))) {
            model.addAttribute("survey", survey);
            model.addAttribute("question", modelAbstractFactory.createBasicQuestion());
            model.addAttribute("unsavedchanges", true);
            return "survey/surveymanagement::#details";
        } else {
            return "survey/surveymanagement";
        }
    }

    @PostMapping("/save")
    private String saveSurvey(@ModelAttribute Survey survey, Model model, RedirectAttributes attributes) {

        CourseAdminSurveyService courseAdminSurveyService = surveyCreationAbstractFactory
                .createCourseAdminSurveyService();

        if (survey.getSurveyQuestions() == null) {
            attributes.addFlashAttribute("errormessage", "Atleast add one question to survey.");
            model.addAttribute("unsavedchanges", false);
            return "redirect:/survey/manage";
        }
        try {
            LOGGER.info("Calling Services to save or update the survey details");
            courseAdminSurveyService.saveSurvey(survey);
            model.addAttribute("unsavedchanges", false);
            attributes.addFlashAttribute("message", "Survey updated and saved Successfully.");
            return "redirect:/survey/manage";
        } catch (UserDefinedException e) {
            attributes.addFlashAttribute("errormessage", "Some Error occurred. Couldn't save survey.");
            return "redirect:/survey/manage";
        }

    }

    @PostMapping("/publish")
    private String publishSurvey(@RequestParam int surveyId, Model model, RedirectAttributes attributes) {

        CourseAdminSurveyService courseAdminSurveyService = surveyCreationAbstractFactory
                .createCourseAdminSurveyService();
        boolean published;
        try {
            LOGGER.info("Calling Services to publish the survey details");
            published = courseAdminSurveyService.publishSurvey(surveyId);
            if (published) {
                attributes.addFlashAttribute("message", "Survey Published for the Course Successfully.");
            } else {
                attributes.addFlashAttribute("errormessage",
                        "Couldn't publish Survey. Please verify surveydetails and try again");
            }
            return "redirect:/survey/manage";
        } catch (UserDefinedException e) {
            attributes.addFlashAttribute("errormessage", "Some Error occurred. Couldn't Publish survey.");
            return "redirect:/survey/manage";
        }
    }
}
