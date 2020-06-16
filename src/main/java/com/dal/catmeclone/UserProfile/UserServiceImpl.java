package com.dal.catmeclone.UserProfile;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dal.catmeclone.SystemConfig;
import com.dal.catmeclone.DBUtility.DataBaseConnection;
import com.dal.catmeclone.DBUtility.DatabaseConnectionImpl;
import com.dal.catmeclone.exceptionhandler.DuplicateUserRelatedException;
import com.dal.catmeclone.exceptionhandler.UserDefinedSQLException;

import com.dal.catmeclone.model.User;


public class UserServiceImpl implements UserService {


	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
	
	UserDao userDb ;

	Boolean flag=false;
	
	final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public boolean Create(User u) throws Exception {
		userDb = SystemConfig.instance().getUserDao();
		try {
			//validating user details
			if (User.isBannerIDValid(u.getBannerId()) &&
					 User.isEmailValid(u.getEmail()) &&
					 User.isFirstNameValid(u.getFirstName()) &&
					 User.isLastNameValid(u.getLastName()) &&
					 !u.getPassword().isEmpty())
			{
				LOGGER.info("Accessing DAO layer to create user to given banner id");
				flag=userDb.createUser(u);
			}
			return flag;
		}catch(DuplicateUserRelatedException e) {
			logger.error(e.getMessage());
			throw new DuplicateUserRelatedException(e.getMessage());
		}catch (UserDefinedSQLException e) {
			logger.error(e.getMessage());
			throw new UserDefinedSQLException(e.getMessage());
		} 
		catch (Exception e) {
			flag=false;
			logger.error(e.getLocalizedMessage());
			throw new Exception(e.getLocalizedMessage());
		}

	}
	

	@Override
	public List<User> findAllMatchingUser(String bannerId) throws UserDefinedSQLException {
		
		userDb = SystemConfig.instance().getUserDao();
		
		// Service layer method making a call to data access layer to retrieve the matching list of user
		List<User> listOfUser= new ArrayList<User>();
		LOGGER.info("Accessing DAO layer to get matching list of user to given banner id");
		listOfUser=userDb.findAllMatchingUser(bannerId);
		return listOfUser;
	}
}


