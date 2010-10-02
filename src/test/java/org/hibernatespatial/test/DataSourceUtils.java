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
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Unit testsuite-suite support class.</p>
 *
 * @author Karel Maesen, Geovise BVBA.
 */
public class DataSourceUtils {

    public final static String TEST_POLYGON_WKT = "POLYGON((0 0, 50 0, 100 100, 0 100, 0 0))";

    private static Logger LOGGER = LoggerFactory.getLogger(DataSourceUtils.class);


    private final SQLExpressionTemplate sqlExpressionTemplate;
    private final String jdbcDriver;
    private final String jdbcUrl;
    private final String jdbcUser;
    private final String jdbcPass;

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
     * @param jdbcDriver
     * @param jdbcUrl
     * @param jdbcUser
     * @param jdbcPass
     * @param sqlExpressionTemplate SQLExpressionTemplate object that generates SQL statements for this database
     */
    public DataSourceUtils(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPass, SQLExpressionTemplate sqlExpressionTemplate) {
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;
        this.jdbcPass = jdbcPass;
        this.sqlExpressionTemplate = sqlExpressionTemplate;
        createBasicDataSource();
    }

    private void createBasicDataSource() {
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName(jdbcDriver);
        bds.setUrl(jdbcUrl);
        bds.setUsername(jdbcUser);
        bds.setPassword(jdbcPass);
        dataSource = bds;
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
    public Connection getConnection() throws SQLException {
        Connection cn = getDataSource().getConnection();
        cn.setAutoCommit(false);
        return cn;
    }

    /**
     * Delete all testsuite-suite data from the database
     *
     * @throws SQLException
     */
    public void deleteTestData() throws SQLException {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            cn.setAutoCommit(false);
            PreparedStatement pmt = cn.prepareStatement("delete from GEOMTEST");
            if (!pmt.execute()) {
                int updateCount = pmt.getUpdateCount();
                LOGGER.info("Removing " + updateCount + " rows.");
            }
            cn.commit();
            pmt.close();
        } finally {
            try {
                if (cn != null) cn.close();
            } catch (SQLException e) {
                // nothing to do
            }
        }
    }

    public void insertTestData(TestData testData) throws SQLException {
        deleteTestData();
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            cn.setAutoCommit(false);
            Statement stmt = cn.createStatement();
            for (TestDataElement testDataElement : testData) {
                String sql = sqlExpressionTemplate.toInsertSql(testDataElement);
                LOGGER.debug("adding stmt: " + sql);
                stmt.addBatch(sql);
            }
            int[] insCounts = stmt.executeBatch();
            cn.commit();
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
     * Executes a SQL statement.
     * <p/>
     * This is used e.g. to drop/create a spatial index, or update the
     * geometry metadata statements.
     *
     * @param sql the (native) SQL Statement to execute
     * @throws SQLException
     */
    public void executeStatement(String sql) throws SQLException {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            cn.setAutoCommit(false);
            PreparedStatement statement = cn.prepareStatement(sql);
            LOGGER.info("Executing statement: " + sql);
            statement.execute();
            cn.commit();
            statement.close();
        } finally {
            try {
                if (cn != null) cn.close();
            } catch (SQLException e) {
            } //do nothing.
        }
    }

    /**
     * Return the geometries of the testsuite-suite objects as raw (i.e. undecoded) objects from the database.
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
     * Returns the JTS geometries that are expected of a decoding of the testsuite-suite object's geometry.
     * <p/>
     * <p>This method reads the WKT of the testsuite-suite objects and returns the result.</p>
     *
     * @param type type of geometry
     * @return map of identifier and JTS geometry
     */
    public Map<Integer, Geometry> expectedGeoms(String type, TestData testData) {
        Map<Integer, Geometry> result = new HashMap<Integer, Geometry>();
        EWKTReader parser = new EWKTReader();
        for (TestDataElement testDataElement : testData) {
            if (testDataElement.type.equalsIgnoreCase(type)) {
                try {
                    result.put(testDataElement.id, parser.read(testDataElement.wkt));
                } catch (ParseException e) {
                    System.out.println(String.format("Parsing WKT fails for case %d : %s", testDataElement.id, testDataElement.wkt));
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
