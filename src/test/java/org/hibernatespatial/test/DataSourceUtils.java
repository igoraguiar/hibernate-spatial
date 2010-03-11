/*
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2007-2010 Geovise BVBA
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

package org.hibernatespatial.test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>Unit test support class.</p>
 *
 * @author Karel Maesen, Geovise BVBA.
 */
public class DataSourceUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(DataSourceUtils.class);

    private final String propertyFile;
    private final SQLExpressionTemplate sqlExpressionTemplate;

    private TestGeometries testGeometries;
    private Properties properties;
    private DataSource dataSource;

    /**
     * Constructor for the DataSourceUtils object.
     * <p/>
     * <p>The following entities are required in the property file:
     * <il>
     * <li> jdbcUrl: jdbc connection URL</li>
     * <li> dbUsername: username for the database</li>
     * <li> dbPassword: password for the database</li>
     * <li> driver: fully-qualified class name for the JDBC Driver</li>
     * </il>
     *
     * @param propertyFile          properties file for the database properties
     * @param sqlExpressionTemplate SQLExpressionTemplate object that generates SQL statements for this database
     */
    public DataSourceUtils(String propertyFile, SQLExpressionTemplate sqlExpressionTemplate) {
        this.propertyFile = propertyFile;
        this.sqlExpressionTemplate = sqlExpressionTemplate;
        readProperties();
        createBasicDataSource();
        loadTestObjects();
    }

    private void readProperties() {
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFile);
            properties = new Properties();
            properties.load(is);
        } catch (IOException e) {
            throw (new RuntimeException(e));
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                //nothing to do
            }
        }
    }

    private void createBasicDataSource() {
        String url = properties.getProperty("jdbcUrl");
        String user = properties.getProperty("dbUsername");
        String pwd = properties.getProperty("dbPassword");
        String driverName = properties.getProperty("driver");
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName(driverName);
        bds.setUrl(url);
        bds.setUsername(user);
        bds.setPassword(pwd);
        dataSource = bds;
    }

    /**
     * loads the test objects
     * <p/>
     * TODO -- make the source of the TestGeometries configurable.
     */
    private void loadTestObjects() {
        testGeometries = new TestGeometries();
        testGeometries.prepare();
    }

    /**
     * Returns a DataSource for the configured database.
     *
     * @return a DataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Returns a JDBC connection to the database
     *
     * @return a JDBC Connection object
     * @throws SQLException
     */
    public Connection createConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Delete all test data from the database
     *
     * @throws SQLException
     */
    public void deleteTestData() throws SQLException {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            PreparedStatement pmt = cn.prepareStatement("delete from geomtest");
            if (!pmt.execute()) {
                int updateCount = pmt.getUpdateCount();
                LOGGER.info("Removing " + updateCount + " rows.");
            }
            pmt.close();
        } finally {
            try {
                if (cn != null) cn.close();
            } catch (SQLException e) {
                // nothing to do
            }
        }
    }

    /**
     * Insert all test data in the database
     *
     * @throws SQLException
     */
    public void insertTestData() throws SQLException {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            Statement stmt = cn.createStatement();
            for (TestGeometry testGeometry : testGeometries) {
                String sql = sqlExpressionTemplate.toInsertSql(testGeometry);
                LOGGER.debug("adding stmt: " + sql);
                stmt.addBatch(sql);
            }
            int[] insCounts = stmt.executeBatch();
            stmt.close();
            LOGGER.info("Loaded " + sum(insCounts) + " rows.");
        } finally {
            try {
                if (cn != null) cn.close();
            } catch (SQLException e) {
                // nothing to do
            }
        }
    }

    /**
     * Return the geometries of the test objects as raw (i.e. undecoded) objects from the database.
     *
     * @param type type of geometry
     * @return map of identifier, undecoded geometry object
     */
    public Map<Integer, Object> rawDbObjects(String type) {
        Map<Integer, Object> map = new HashMap<Integer, Object>();
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            PreparedStatement pstmt = cn.prepareStatement("select id, geom from geomtest where type = ? order by id");
            pstmt.setString(1, type);
            ResultSet results = pstmt.executeQuery();
            while (results.next()) {
                Integer id = results.getInt(1);
                Object obj = results.getObject(2);
                map.put(id, obj);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (cn != null) cn.close();
            } catch (SQLException e) {
                // nothing we can do.
            }
        }
        return map;

    }

    /**
     * Returns the JTS geometries that are expected of a decoding of the test object's geometry.
     * <p/>
     * <p>This method reads the WKT of the test objects and returns the result.</p>
     *
     * @param type type of geometry
     * @return map of identifier and JTS geometry
     */
    public Map<Integer, Geometry> expectedGeoms(String type) {
        Map<Integer, Geometry> result = new HashMap<Integer, Geometry>();
        EWKTReader parser = new EWKTReader();
        for (TestGeometry testGeometry : testGeometries) {
            if (testGeometry.type.equalsIgnoreCase(type)) {
                try {
                    result.put(testGeometry.id, parser.read(testGeometry.wkt));
                } catch (ParseException e) {
                    System.out.println(String.format("Parsing WKT fails for case %d : %s", testGeometry.id, testGeometry.wkt));
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    private static int sum(int[] insCounts) {
        int result = 0;
        for (int idx = 0; idx < insCounts.length; idx++) {
            result += insCounts[idx];
        }
        return result;
    }

}
