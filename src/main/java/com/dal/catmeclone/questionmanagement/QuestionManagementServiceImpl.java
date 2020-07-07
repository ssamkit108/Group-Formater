package com.dal.catmeclone.questionmanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.dal.catmeclone.AbstractFactory;
import com.dal.catmeclone.SystemConfig;
import com.dal.catmeclone.exceptionhandler.UserDefinedSQLException;
import com.dal.catmeclone.model.BasicQuestion;
import com.dal.catmeclone.model.MultipleChoiceQuestion;
import com.dal.catmeclone.model.User;

public class QuestionManagementServiceImpl implements QuestionManagementService {

	AbstractFactory abstractFactory=SystemConfig.instance().getAbstractFactory();
	QuestionManagementAbstractFactory questionManagementAbstractFactory=abstractFactory.createQuestionManagerAbstractFactory();
	private Logger LOGGER = Logger.getLogger(QuestionManagementServiceImpl.class.getName());
	QuestionManagementDao questionManagementDao = null;

	/*
	 * Service Layer method to get all list of question for the user
	 */
	@Override
	public List<BasicQuestion> getAllQuestionByUser(User user) throws UserDefinedSQLException {

		questionManagementDao = questionManagementAbstractFactory.createQuestionManagementDao();

		List<BasicQuestion> listOfQuestion = new ArrayList<BasicQuestion>();
		// Calling Dao layer to perform interaction wwith DB to fetch list of question
		listOfQuestion = questionManagementDao.getAllQuestionByUser(user);
		return listOfQuestion;
	}

	/*
	 * Service Layer method to get list of sorted question based, sorted by title
	 */
	@Override
	public List<BasicQuestion> getSortedQuestionsByTitle(User user) throws UserDefinedSQLException {

		questionManagementDao = questionManagementAbstractFactory.createQuestionManagementDao();

		List<BasicQuestion> listOfQuestion = new ArrayList<BasicQuestion>();
		// Calling Dao layer to perform interaction wwith DB to fetch list of question
		listOfQuestion = questionManagementDao.getAllQuestionByUser(user);
		LOGGER.info("Sorting the Question based on Title");
		// Using the Comparator to sort the list of question based on the Title
		Collections.sort(listOfQuestion, new Comparator<BasicQuestion>() {
			public int compare(BasicQuestion o1, BasicQuestion o2) {
				return o1.getQuestionTitle().toLowerCase().compareTo(o2.getQuestionTitle().toLowerCase());
			}
		});
		return listOfQuestion;
	}

	/*
	 * Service Layer method to get list of sorted question based, sorted by date
	 */
	@Override
	public List<BasicQuestion> getSortedQuestionsByDate(User user) throws UserDefinedSQLException {

		questionManagementDao = questionManagementAbstractFactory.createQuestionManagementDao();

		List<BasicQuestion> listOfQuestion = new ArrayList<BasicQuestion>();
		// Calling Dao layer to perform interaction wwith DB to fetch list of question
		listOfQuestion = questionManagementDao.getAllQuestionByUser(user);
		LOGGER.info("Sorting the Question based on Title");
		// Using the Comparator to sort the list of question based on the Date
		Collections.sort(listOfQuestion, new Comparator<BasicQuestion>() {
			public int compare(BasicQuestion o1, BasicQuestion o2) {
				if (o1.getCreationDate() == null || o2.getCreationDate() == null)
					return 0;
				return o1.getCreationDate().compareTo(o2.getCreationDate());
			}
		});
		return listOfQuestion;
	}

	@Override
	public boolean createMultipleChoiceQuestion(MultipleChoiceQuestion multipleChoice) throws UserDefinedSQLException {

		questionManagementDao = questionManagementAbstractFactory.createQuestionManagementDao();

		multipleChoice.filterOptions();
		boolean isQuestionCreated = questionManagementDao.createMultipleChoiceQuestion(multipleChoice);
		return isQuestionCreated;
	}

	@Override
	public boolean createNumericOrTextQuestion(BasicQuestion basicQuestion) throws UserDefinedSQLException {

		questionManagementDao = questionManagementAbstractFactory.createQuestionManagementDao();

		boolean isQuestionCreated = questionManagementDao.createNumericOrTextQuestion(basicQuestion);
		return isQuestionCreated;
	}

	@Override
	public boolean ifQuestionTitleandTextExists(BasicQuestion basicQuestion) throws UserDefinedSQLException {

		questionManagementDao = questionManagementAbstractFactory.createQuestionManagementDao();

		boolean isQuestionExists = questionManagementDao.isQuestionExistForUserWithTitleandText(basicQuestion);
		return isQuestionExists;
	}

	@Override
	public boolean deleteQuestion(int questionId) throws UserDefinedSQLException {

		questionManagementDao = questionManagementAbstractFactory.createQuestionManagementDao();

		boolean isQuestionDeleted = questionManagementDao.deleteQuestion(questionId);
		return isQuestionDeleted;
	}

}