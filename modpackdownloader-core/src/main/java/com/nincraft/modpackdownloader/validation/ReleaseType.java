package com.nincraft.modpackdownloader.validation;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import lombok.val;

import java.util.Arrays;
import java.util.List;

public class ReleaseType implements IParameterValidator {
	@Override
	public void validate(String name, String value) throws ParameterException {
		if (!Arrays.asList("release", "beta", "alpha").contains(value.toLowerCase())) {
			throw new ParameterException(String.format("Parameter %s is not a valid release type (Found %s)", name, value));
		}
	}
}
