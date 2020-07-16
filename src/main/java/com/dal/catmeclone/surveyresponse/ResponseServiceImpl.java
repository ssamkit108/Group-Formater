package com.dal.catmeclone.surveyresponse;

import com.dal.catmeclone.AbstractFactory;
import com.dal.catmeclone.SystemConfig;
import com.dal.catmeclone.exceptionhandler.UserDefinedSQLException;
import com.dal.catmeclone.model.SurveyQuestion;
import com.dal.catmeclone.model.SurveyQuestionResponse;
import com.dal.catmeclone.model.User;
import com.dal.catmeclone.model.UserSurveyResponse;

import java.util.List;
import java.util.logging.Logger;

public class ResponseServiceImpl implements ResponseService {

    Logger LOGGER = Logger.getLogger(ResponseServiceImpl.class.getName());

    AbstractFactory abstractFactory = SystemConfig.instance().getAbstractFactory();
    SurveyResponseAbstractFactory surveyResponseAbstractFactory = abstractFactory.createSurveyResponseAbstractFactory();

    public List<SurveyQuestionResponse> getAllQuestion(int courseid) throws Exception {
        ResponseDao responseDao = surveyResponseAbstractFactory.createResponseDao();
        List<SurveyQuestionResponse> listofquestion;
        try {
            listofquestion = responseDao.getAllQuestion(courseid);
            return listofquestion;
        } catch (UserDefinedSQLException e) {
            LOGGER.warning("SQL error encountered" + e.getLocalizedMessage());
            throw new UserDefinedSQLException("SQL error encountered" + e.getLocalizedMessage());
        } catch (Exception e) {
            LOGGER.warning("Generic error encountered" + e.getLocalizedMessage());
            throw new Exception("Generic error encountered" + e.getLocalizedMessage());
        }
    }

    public Boolean checkPublished(int courseid) throws Exception {
        try {
            ResponseDao responseDao = surveyResponseAbstractFactory.createResponseDao();
            return responseDao.checkPublished(courseid);
        } catch (UserDefinedSQLException e) {
            LOGGER.warning("SQL error encountered" + e.getLocalizedMessage());
            throw new UserDefinedSQLException("SQL error encountered" + e.getLocalizedMessage());
        } catch (Exception e) {
            LOGGER.warning("Generic error encountered" + e.getLocalizedMessage());
            throw new Exception("Generic error encountered" + e.getLocalizedMessage());
        }
    }

    @Override
    public Boolean checkSubmitted(String bannerid, int courseid) throws Exception {
        try {
            ResponseDao responseDao = surveyResponseAbstractFactory.createResponseDao();
            return responseDao.checkSubmitted(bannerid, courseid);

        } catch (UserDefinedSQLException e) {
            LOGGER.warning("SQL error encountered" + e.getLocalizedMessage());
            throw new UserDefinedSQLException("SQL error encountered" + e.getLocalizedMessage());
        } catch (Exception e) {
            LOGGER.warning("Generic error encountered" + e.getLocalizedMessage());
            throw new Exception("Generic error encountered" + e.getLocalizedMessage());
        }
    }

    public void setAllresponses(UserSurveyResponse userSurveyResponse) throws Exception {
        try {
            ResponseDao responseDao = surveyResponseAbstractFactory.createResponseDao();
            User user = userSurveyResponse.getUser();
            List<SurveyQuestionResponse> surveyQuestionResponses = userSurveyResponse.getSurveyResponse();
            for (SurveyQuestionResponse questionResponse : surveyQuestionResponses) {
                SurveyQuestion surveyQuestion = questionResponse.getSurveyQuestion();
                responseDao.createResponseId(surveyQuestion.getSurveyQuestionId(), user.getBannerId(), userSurveyResponse.getResponseDate(), userSurveyResponse.getSubmitted()
                        , userSurveyResponse.getSurvey().getCourse().getCourseID());
                responseDao.insertResponse(surveyQuestion.getSurveyQuestionId(), user.getBannerId(), questionResponse.getResponse());
            }
        } catch (UserDefinedSQLException e) {
            LOGGER.warning("SQL error encountered" + e.getLocalizedMessage());
            throw new UserDefinedSQLException("SQL error encountered" + e.getLocalizedMessage());
        } catch (Exception e) {
            LOGGER.warning("Generic error encountered" + e.getLocalizedMessage());
            throw new Exception("Generic error encountered" + e.getLocalizedMessage());
        }

    }
}
