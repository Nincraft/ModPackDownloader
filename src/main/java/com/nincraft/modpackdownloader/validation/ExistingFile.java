package com.nincraft.modpackdownloader.validation;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.io.File;

public class ExistingFile implements IParameterValidator {
	@Override
	public void validate(String name, String value) throws ParameterException {
		if (!new File(value).exists()) {
			throw new ParameterException(String.format("Parameter %s is not a valid file (found %s)", name, value));
		}
	}
}
