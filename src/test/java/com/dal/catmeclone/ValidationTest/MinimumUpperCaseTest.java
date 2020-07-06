package com.dal.catmeclone.ValidationTest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import com.dal.catmeclone.AbstractFactory;
import com.dal.catmeclone.SystemConfig;
import com.dal.catmeclone.Validation.MinimumUpper;
import com.dal.catmeclone.Validation.ValidationPolicy;
import com.dal.catmeclone.model.User;

class MinimumUpperCaseTest {

	AbstractFactory abstractFactory=SystemConfig.instance().getAbstractFactory();
	ValidationPolicy checkupper = abstractFactory.createValidationAbstractFactory().createMinimumUpper();


	@SuppressWarnings("deprecation")
	@Test
	public void TestMinimumUpper() throws Exception{
		User u = abstractFactory.createModelAbstractFactory().createUser();
		u.setPassword("SaMkit");
		checkupper.setValue("2");
		Assert.isTrue(checkupper.isValid(u));
		u.setPassword("SaMkit");
		checkupper.setValue("3");
		Assert.isTrue(!checkupper.isValid(u));
		u.setPassword("SaMkit");
		checkupper.setValue("1");
		Assert.isTrue(checkupper.isValid(u));

	}

}
