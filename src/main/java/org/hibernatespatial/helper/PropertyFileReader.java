/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
 * Copyright © 2007 Geovise BVBA
 * Copyright © 2007 K.U. Leuven LRD, Spatial Applications Division, Belgium
 *
 * This work was partially supported by the European Commission, 
 * under the 6th Framework Programme, contract IST-2-004688-STP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, visit: http://www.hibernatespatial.org/
 */
package org.hibernatespatial.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to read settings and properties files.
 * 
 * @author Karel Maesen
 * 
 */
public class PropertyFileReader {

	private static final Logger log = LoggerFactory.getLogger(PropertyFileReader.class);

	/**
	 * pattern for comment lines. If it matches, it is a comment.
	 */
	private static final Pattern nonCommentPattern = Pattern
			.compile("^([^#]+)");

	private InputStream is = null;

	public PropertyFileReader(InputStream is) {
		this.is = is;
	}

	public Properties getProperties() throws IOException {
		if (is == null)
			return null;
		Properties props = new Properties();
		props.load(is);
		return props;
	}

	/**
	 * Returns the non-comment lines in a file.
	 * 
	 * @return set of non-comment strings.
	 * @throws IOException
	 */
	public Set<String> getNonCommentLines() throws IOException {
		Set<String> lines = new HashSet<String>();
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			Matcher m = nonCommentPattern.matcher(line);
			if (m.find()) {
				lines.add(m.group().trim());
			}
		}
		return lines;
	}

	public void close() {
		try {
			this.is.close();
		} catch (IOException e) {
			log.warn("Exception when closing PropertyFileReader: " + e);
		}
	}

}
