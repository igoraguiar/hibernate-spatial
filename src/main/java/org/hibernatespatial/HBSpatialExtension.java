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
package org.hibernatespatial;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernatespatial.cfg.GeometryFactoryHelper;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.helper.PropertyFileReader;
import org.hibernatespatial.mgeom.MGeometryFactory;
import org.hibernatespatial.spi.SpatialDialectProvider;

/**
 * This is the bootstrap class that is used to get an
 * <code>SpatialDialect</code>.
 * 
 * It also provides a default <code>SpatialDialect</code>.
 * <code>GeometryUserTypes</code>s that do not have a <code>dialect</code>
 * parameter use this default.
 * 
 * The default <code>SpatialDialect</code> will be the first one that is
 * returned by the <code>getDefaultDialect</code> method of the provider at
 * least if it is non null.
 * 
 * @author Karel Maesen
 */
public class HBSpatialExtension {

	protected static Set<SpatialDialectProvider> providers = new HashSet<SpatialDialectProvider>();

	private static final Log log = LogFactory.getLog(HBSpatialExtension.class);

	private static SpatialDialect defaultSpatialDialect = null;

	private static final String DIALECT_PROP_NAME = "hibernate.spatial.dialect";

	private static HSConfiguration configuration = null;

	private static MGeometryFactory defaultGeomFactory = new MGeometryFactory();

	static {

		log.info("Initializing HBSpatialExtension");
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Enumeration<URL> resources = null;
		try {
			resources = loader.getResources("META-INF/services/"
					+ SpatialDialectProvider.class.getName());
			Set<String> names = new HashSet<String>();
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				InputStream is = url.openStream();
				try {
					names.addAll(providerNamesFromReader(is));
				} finally {
					is.close();
				}
			}

			for (String s : names) {
				try {
					log.info("Attempting to load Hibernate Spatial Provider "
							+ s);
					SpatialDialectProvider provider = (SpatialDialectProvider) loader
							.loadClass(s).newInstance();
					// we set the defaultSpatialDialect to the one provided
					// by the first loaded provider.
					if (defaultSpatialDialect == null)
						setDefaultSpatialDialect(provider.getDefaultDialect());
					providers.add(provider);
				} catch (Exception e) {
					throw new HibernateSpatialException(
							"Problem loading provider class", e);
				}

			}
		} catch (IOException e) {
			throw new HibernateSpatialException("No "
					+ SpatialDialectProvider.class.getName()
					+ " found in META-INF/services", e);
		}

		// configuration - check if there is a system property
		String dialectProp = System.getProperty(DIALECT_PROP_NAME);
		if (dialectProp != null) {
			log.info("Spatial Dialect configured as system property: "
					+ dialectProp);
			boolean found = false;
			search: for (SpatialDialectProvider provider : providers) {
				for (String dialect : provider.getSupportedDialects()) {
					if (dialect.equals(dialectProp)) {
						defaultSpatialDialect = provider
								.createSpatialDialect(dialectProp);
						found = true;
						break search;
					}
				}
			}
			if (!found)
				log
						.warn("Spatial dialect "
								+ dialectProp
								+ " configured as sytem property, but dialect not found");
		}

		// configuration - load the config file
		log.info("Looking for configuration file.");
		HSConfiguration hsConfig = new HSConfiguration();
		if (hsConfig.configure()) {
			setConfiguration(hsConfig);
		}

		if (defaultSpatialDialect == null) {
			log.warn("Hibernate Spatial Configured but no spatial dialect");
		} else {
			log.info("Hibernate Spatial configured. Using dialect: "
					+ defaultSpatialDialect.getClass().getCanonicalName());
		}
	}

	/**
	 * Make sure nobody can instantiate this class
	 */
	private HBSpatialExtension() {
	}

	public static void setConfiguration(HSConfiguration c) {
		configuration = c;
		log.info("Configuring HBSpatialExtension from " + c.getSource());

		// checking for configured dialectname
		String dialectName = configuration.getDefaultDialect();
		if (dialectName != null) {
			SpatialDialect dialect = createSpatialDialect(dialectName);
			if (dialect != null) {
				log.info("Setting Spatial Dialect to : " + dialectName);
				setDefaultSpatialDialect(dialect);
			}
		}
		// trying to create a defaultGeometryFactory
		defaultGeomFactory = GeometryFactoryHelper
				.createGeometryFactory(configuration);
		log.info("Creating default Geometry Factory");
	}

	public static HSConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * @param dialect
	 */
	public static void setDefaultSpatialDialect(SpatialDialect dialect) {
		defaultSpatialDialect = dialect;
	}

	public static SpatialDialect getDefaultSpatialDialect() {
		return defaultSpatialDialect;
	}

	public static SpatialDialect createSpatialDialect(String dialectName) {
		SpatialDialect dialect = null;
		for (SpatialDialectProvider provider : providers) {
			dialect = provider.createSpatialDialect(dialectName);
			if (dialect != null) {
				break;
			}
		}
		if (dialect == null) {
			throw new HibernateSpatialException(
					"No SpatialDialect provider for persistenceUnit "
							+ dialectName);
		}
		return dialect;
	}

	public static MGeometryFactory getDefaultGeomFactory() {
		return defaultGeomFactory;
	}

	// Helper methods

	private static Set<String> providerNamesFromReader(InputStream is)
			throws IOException {
		PropertyFileReader reader = new PropertyFileReader(is);
		return reader.getNonCommentLines();
	}

}
