package com.dal.catmeclone.Validation;

import com.dal.catmeclone.exceptionhandler.UserDefinedException;
import com.dal.catmeclone.model.User;

import java.sql.SQLException;
import java.util.List;

public interface HistoryConstraintDao {
    public List<String> fetchPasswordList(User u, int limit) throws UserDefinedException, SQLException;
}
