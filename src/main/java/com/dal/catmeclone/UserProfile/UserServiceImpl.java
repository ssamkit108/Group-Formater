package com.dal.catmeclone.UserProfile;

import com.dal.catmeclone.AbstractFactory;
import com.dal.catmeclone.SystemConfig;
import com.dal.catmeclone.Validation.ValidatePassword;
import com.dal.catmeclone.Validation.ValidationAbstractFactory;
import com.dal.catmeclone.exceptionhandler.DuplicateEntityException;
import com.dal.catmeclone.exceptionhandler.UserDefinedSQLException;
import com.dal.catmeclone.exceptionhandler.ValidationException;
import com.dal.catmeclone.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl implements UserService {

    final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    AbstractFactory abstractFactory = SystemConfig.instance().getAbstractFactory();
    UserProfileAbstractFactory userProfileAbstractFactory = abstractFactory.createUserProfileAbstractFactory();
    ValidationAbstractFactory validationAbstractFactory = abstractFactory.createValidationAbstractFactory();
    ValidatePassword validatepassword;
    UserDao userDb;
    Boolean flag = false;

    @Override
    public boolean Create(User u) throws Exception {
        userDb = userProfileAbstractFactory.createUserDao();
        validatepassword = validationAbstractFactory.createValidatePassword();
        try {
            validatepassword.validatepassword(u);
            logger.info("Accessing DAO layer to create user to given banner id" + u.getBannerId());
            flag = userDb.createUser(u);
            return flag;
        } catch (ValidationException e) {
            flag = false;
            throw new ValidationException(e.getMessage());
        } catch (DuplicateEntityException e) {
            logger.error(e.getMessage());
            throw new DuplicateEntityException(e.getMessage());
        } catch (UserDefinedSQLException e) {
            logger.error(e.getMessage());
            throw new UserDefinedSQLException(e.getMessage());
        } catch (Exception e) {
            flag = false;
            logger.error(e.getLocalizedMessage());
            throw new Exception(e.getLocalizedMessage());
        }
    }

    @Override
    public List<User> findAllMatchingUser(String bannerId) throws UserDefinedSQLException {
        userDb = userProfileAbstractFactory.createUserDao();
        List<User> listOfUser = new ArrayList<User>();
        logger.info("Accessing DAO layer to get matching list of user to given banner id");
        listOfUser = userDb.findAllMatchingUser(bannerId);
        return listOfUser;
    }
}
