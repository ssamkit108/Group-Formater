package com.dal.catmeclone.Validation;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dal.catmeclone.SystemConfig;
import com.dal.catmeclone.exceptionhandler.UserDefinedSQLException;
import com.dal.catmeclone.model.User;

public class HistoryConstraint implements ValidationPolicy{
	
	final Logger logger = LoggerFactory.getLogger(HistoryConstraint.class);
	HistoryContraintDao historyConstraintDao;
	private String ruleValue;
	private List<String> passwordlist;

	
	public void setValue(String ruleValue) {
		this.ruleValue = ruleValue;
	}
	
	public boolean isValid(User user) throws UserDefinedSQLException, SQLException {
		try {
			boolean result=false;
			int limit=Integer.parseInt(ruleValue);
			historyConstraintDao=SystemConfig.instance().getHistoryConstraintDao();
			passwordlist = historyConstraintDao.fetchPasswordList(user,limit);
			result=!(passwordlist.contains(user.getPassword()));
			logger.info("Password History constraint validation. Result : " + result);
			return result;
			
		} catch (UserDefinedSQLException e) {
			logger.error("Error in loading Password History. ", e);
			throw new UserDefinedSQLException(e.getLocalizedMessage());
		}catch(SQLException e) {
			logger.error("Error in loading Password History. ", e);
			throw new SQLException(e.getLocalizedMessage());

		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String getError() {
		return "Password cannot be same as past " + this.ruleValue + " passwords";
	}

}

