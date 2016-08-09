/*
 * Copyright (c) 2016 Gábor Kövesdán
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.kovesdan.minihelp.xml;

import java.io.File;
import java.nio.file.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Helper class to easily parser the configuration data.
 * 
 * @author Gábor Kövesdán
 *
 */
public class ConfigurationReader {
	private static Object parseObject(File file, Class<?> c) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(c);
		Unmarshaller unmarshaller = ctx.createUnmarshaller();
		return (Object) unmarshaller.unmarshal(file);
	}

	/**
	 * Parses the configuration from a given file.
	 * 
	 * @param file
	 *            the configuration file to parse.
	 * @return an object representing the parsed configuration.
	 * @throws JAXBException
	 *             when the configuration file in not well-formed or invalid.
	 */
	public static Configuration parseConfiguration(File file) throws JAXBException {
		return (Configuration) parseObject(file, Configuration.class);
	}

	/**
	 * Parses the configuration from a given file.
	 * 
	 * @param path
	 *            the configuration file to parse.
	 * @return an object representing the parsed configuration.
	 * @throws JAXBException
	 *             when the configuration file in not well-formed or invalid.
	 */
	public static Configuration parseConfiguration(Path path) throws JAXBException {
		return (Configuration) parseObject(path.toFile(), Configuration.class);
	}
}
