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
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * An <code>AbstractExpectationsFactory</code> provides the expected
 * values to be used in the unit tests of the spatial functions
 * provided by specific providers.
 * <p/>
 * The expected values are returned as a map of (identifier, expected value) pairs.
 *
 * @author Karel Maesen, Geovise BVBA
 * @see TestSpatialFunctions
 */
public abstract class AbstractExpectationsFactory {

    public final static int INTEGER = 1;
    public final static int DOUBLE = 2;
    public final static int GEOMETRY = 3;
    public final static int STRING = 4;
    public final static int BOOLEAN = 5;
    public final static int OBJECT = -1;

    private final static int TEST_SRID = 4326;

    private final DataSourceUtils dataSourceUtils;
    private static final int MAX_BYTE_LEN = 1024;

    public AbstractExpectationsFactory(String propertiesFile, SQLExpressionTemplate sqlExpressionTemplate) {
        this.dataSourceUtils = new DataSourceUtils(propertiesFile, sqlExpressionTemplate);

    }

    protected DataSourceUtils getDataSourceUtils() {
        return this.dataSourceUtils;
    }

    /**
     * Returns the SRID in which all tests are conducted. This is for now 4326;
     *
     * @return
     */
    public int getTestSrid() {
        return TEST_SRID;
    }


    /**
     * Returns the expected dimensions of all test geometries.
     *
     * @return map of identifier, dimension
     * @throws SQLException
     */
    public Map<Integer, Integer> getDimension() throws SQLException {
        return retrieveExpected(createNativeDimensionSQL(), INTEGER);
    }

    /**
     * Returns the expected WKT of all test geometries.
     *
     * @return map of identifier, WKT-string
     * @throws SQLException
     */
    public Map<Integer, String> getAsText() throws SQLException {
        return retrieveExpected(createNativeAsTextStatement(), STRING);

    }


    /**
     * Returns the expected WKB representations of all test geometries
     *
     * @return map of identifier, WKB representation
     * @throws SQLException
     */
    public Map<Integer, byte[]> getAsBinary() throws SQLException {
        return retrieveExpected(createNativeAsBinaryStatement(), OBJECT);
    }

    /**
     * Returns the expected type names of all test geometries
     *
     * @return map of identifier, type name
     * @throws SQLException
     */
    public Map<Integer, String> getGeometryType() throws SQLException {
        return retrieveExpected(createNativeGeometryTypeStatement(), STRING);
    }

    /**
     * Returns the expected SRID codes of all test geometries
     *
     * @return map of identifier, SRID
     * @throws SQLException
     */
    public Map<Integer, Integer> getSrid() throws SQLException {
        return retrieveExpected(createNativeSridStatement(), INTEGER);
    }

    /**
     * Returns whether the test geometries are simple
     *
     * @return map of identifier and whether test geometry is simple
     * @throws SQLException
     */
    public Map<Integer, Boolean> getIsSimple() throws SQLException {
        return retrieveExpected(createNativeIsSimpleStatement(), BOOLEAN);
    }

    /**
     * Returns whether the test geometries are empty
     *
     * @return map of identifier and whether test geometry is empty
     * @throws SQLException
     */
    public Map<Integer, Boolean> getIsEmpty() throws SQLException {
        return retrieveExpected(createNativeIsEmptyStatement(), BOOLEAN);
    }

    /**
     * Returns the expected boundaries of all test geometries
     *
     * @return map of identifier and boundary geometry
     * @throws SQLException
     */
    public Map<Integer, Geometry> getBoundary() throws SQLException {
        return retrieveExpected(createNativeBoundaryStatement(), GEOMETRY);
    }

    /**
     * Returns the expected envelopes of all test geometries
     *
     * @return map of identifier and envelope
     * @throws SQLException
     */
    public Map<Integer, Geometry> getEnvelope() throws SQLException {
        return retrieveExpected(createNativeEnvelopeStatement(), GEOMETRY);
    }

    /**
     * Returns the expected results of the within operator
     *
     * @param geom test geometry
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getWithin(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeWithinStatement(geom), BOOLEAN);
    }

    /**
     * Returns the expected results of the equals operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getEquals(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeEqualsStatement(geom), BOOLEAN);
    }

    /**
     * Returns the expected results of the crosses operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getCrosses(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeCrossesStatement(geom), BOOLEAN);
    }

    /**
     * Returns the expected results of the contains operator
     */
    public Map<Integer, Boolean> getContains(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeContainsStatement(geom), BOOLEAN);
    }

    /**
     * Returns the expected results of the disjoint operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getDisjoint(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeDisjointStatement(geom), BOOLEAN);
    }

    /**
     * Returns the expected results of the intersects operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getIntersects(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeIntersectsStatement(geom), BOOLEAN);
    }

    /**
     * Returns the expected results of the touches operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getTouches(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeTouchesStatement(geom), BOOLEAN);
    }

    /**
     * Returns the expected results of the overlaps operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getOverlaps(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeOverlapsStatement(geom), BOOLEAN);
    }

    /**
     * Returns the expected results of the relate operator
     *
     * @param geom
     * @param matrix
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getRelate(Geometry geom, String matrix) throws SQLException {
        return retrieveExpected(createNativeRelateStatement(geom, matrix), BOOLEAN);
    }

    /**
     * Returns the expected results for the geometry filter
     *
     * @param geom filter Geometry
     * @return
     */
    public Map<Integer, Boolean> getFilter(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeFilterStatement(geom), BOOLEAN);
    }

    /**
     * Returns the expected results of the distance function
     *
     * @param geom geometry parameter to distance function
     * @return
     * @throws SQLException
     */
    public Map<Integer, Double> getDistance(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeDistanceStatement(geom), DOUBLE);
    }

    /**
     * Returns the expected results of the buffering function
     *
     * @param distance distance parameter to the buffer function
     * @return
     * @throws SQLException
     */
    public Map<Integer, Geometry> getBuffer(Double distance) throws SQLException {
        return retrieveExpected(createNativeBufferStatement(distance), GEOMETRY);
    }

    /**
     * Returns the expected results of the convexhull function
     *
     * @param geom geometry with which each test geometry is unioned before convexhull calculation
     * @return
     * @throws SQLException
     */
    public Map<Integer, Geometry> getConvexHull(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeConvexHullStatement(geom), GEOMETRY);
    }

    /**
     * Returns the expected results of the intersection function
     *
     * @param geom parameter to the intersection function
     * @return
     * @throws SQLException
     */
    public Map<Integer, Geometry> getIntersection(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeIntersectionStatement(geom), GEOMETRY);
    }

    /**
     * Returns the expected results of the difference function
     *
     * @param geom parameter to the difference function
     * @return
     * @throws SQLException
     */
    public Map<Integer, Geometry> getDifference(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeDifferenceStatement(geom), GEOMETRY);
    }

    /**
     * Returns the expected results of the symdifference function
     *
     * @param geom parameter to the symdifference function
     * @return
     * @throws SQLException
     */

    public Map<Integer, Geometry> getSymDifference(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeSymDifferenceStatement(geom), GEOMETRY);
    }

    /**
     * Returns the expected results of the geomunion function
     *
     * @param geom parameter to the geomunion function
     * @return
     * @throws SQLException
     */
    public Map<Integer, Geometry> getGeomUnion(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeGeomUnionStatement(geom), GEOMETRY);
    }

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, touches(geom, :filter) from GeomEntity where touches(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeTouchesStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, overlaps(geom, :filter) from GeomEntity where overlaps(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeOverlapsStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, relate(geom, :filter, :matrix) from GeomEntity where relate(geom, :filter, :matrix) = true and srid(geom) = 4326"
     *
     * @param geom   the geometry corresponding to the ':filter' query parameter
     * @param matrix the string corresponding to the ':matrix' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeRelateStatement(Geometry geom, String matrix);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, intersects(geom, :filter) from GeomEntity where intersects(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeIntersectsStatement(Geometry geom);

    /**
     * Returns the statement corresponding to the SpatialRestrictions.filter() method.
     *
     * @param geom filter geometry
     * @return
     */
    protected abstract NativeSQLStatement createNativeFilterStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, distance(geom, :filter) from GeomEntity where srid(geom) = 4326"
     *
     * @param geom
     * @return
     */
    protected abstract NativeSQLStatement createNativeDistanceStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, dimension(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement createNativeDimensionSQL();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, buffer(geom, :distance) from GeomEntity where srid(geom) = 4326"
     *
     * @param distance parameter corresponding to the ':distance' query parameter
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeBufferStatement(Double distance);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, convexhull(geomunion(geom, :polygon)) from GeomEntity where srid(geom) = 4326"
     *
     * @param geom parameter corresponding to the ':polygon' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeConvexHullStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, intersection(geom, :polygon) from GeomEntity where srid(geom) = 4326"
     *
     * @param geom parameter corresponding to the ':polygon' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeIntersectionStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, difference(geom, :polygon) from GeomEntity where srid(geom) = 4326"26"
     *
     * @param geom parameter corresponding to the ':polygon' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeDifferenceStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, symdifference(geom, :polygon) from GeomEntity where srid(geom) = 4326"26"
     *
     * @param geom parameter corresponding to the ':polygon' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeSymDifferenceStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, geomunion(geom, :polygon) from GeomEntity where srid(geom) = 4326"26"
     *
     * @param geom parameter corresponding to the ':polygon' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeGeomUnionStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, astext(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeAsTextStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, srid(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeSridStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, issimple(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeIsSimpleStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, isempty(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeIsEmptyStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, boundary(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeBoundaryStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, envelope(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeEnvelopeStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, asbinary(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeAsBinaryStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, geometrytype(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement createNativeGeometryTypeStatement();

    /**
     * Returns a statement corresponding to the HQL statement
     * "SELECT id, within(geom, :filter) from GeomEntity where within(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param testPolygon the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeWithinStatement(Geometry testPolygon);

    /**
     * Returns a statement corresponding to the HQL statement
     * "SELECT id, equals(geom, :filter) from GeomEntity where equals(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeEqualsStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement
     * "SELECT id, crosses(geom, :filter) from GeomEntity where crosses(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeCrossesStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, contains(geom, :filter) from GeomEntity where contains(geom, :geom) = true and srid(geom) = 4326";
     *
     * @param geom
     * @return
     */
    protected abstract NativeSQLStatement createNativeContainsStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement
     * "SELECT id, disjoint(geom, :filter) from GeomEntity where disjoint(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeDisjointStatement(Geometry geom);

    /**
     * Creates a connection to the database
     *
     * @return a Connection
     * @throws SQLException
     */
    protected Connection createConnection() throws SQLException {
        return this.dataSourceUtils.createConnection();
    }

    /**
     * Decodes a native database object to a JTS <code>Geometry</code> instance
     *
     * @param o native database object
     * @return decoded geometry
     */
    protected abstract Geometry decode(Object o);

    /**
     * Return a test polygon (filter, ...)
     *
     * @return a test polygon
     */
    public Polygon getTestPolygon() {
        WKTReader reader = new WKTReader();
        try {
            Polygon polygon = (Polygon) reader.read(dataSourceUtils.TEST_POLYGON_WKT);
            polygon.setSRID(getTestSrid());
            return polygon;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> Map<Integer, T> retrieveExpected(NativeSQLStatement nativeSQLStatement, int type) throws SQLException {
        PreparedStatement preparedStatement = null;
        Connection cn = null;
        Map<Integer, T> expected = new HashMap<Integer, T>();
        try {
            cn = createConnection();
            preparedStatement = nativeSQLStatement.prepare(cn);
            ResultSet results = preparedStatement.executeQuery();
            while (results.next()) {
                int id = results.getInt(1);
                switch (type) {
                    case GEOMETRY:
                        expected.put(id, (T) decode(results.getObject(2)));
                        break;
                    case STRING:
                        expected.put(id, (T) results.getString(2));
                        break;
                    case INTEGER:
                        expected.put(id, (T) Long.valueOf(results.getLong(2)));
                        break;
                    case DOUBLE:
                        Double value = Double.valueOf(results.getDouble(2));
                        if (results.wasNull())
                            value = null; //this is required because SQL Server converts automatically null to 0.0
                        expected.put(id, (T) value);
                        break;
                    case BOOLEAN:
                        expected.put(id, (T) Boolean.valueOf(results.getBoolean(2)));
                        break;
                    default:
                        T val = (T) results.getObject(2);
                        //this code is a hack to deal with Oracle Spatial that returns Blob's for asWKB() function
                        //TODO -- clean up
                        if (val instanceof Blob) {
                            val = (T) ((Blob) val).getBytes(1, MAX_BYTE_LEN);
                        }
                        expected.put(id, val);
                }
            }
            return expected;
        } finally {
            if (preparedStatement != null) preparedStatement.close();
            if (cn != null) cn.close();
        }
    }

    protected NativeSQLStatement createNativeSQLStatement(final String sql) {
        return new NativeSQLStatement() {
            public PreparedStatement prepare(Connection connection) throws SQLException {
                return connection.prepareStatement(sql);
            }
        };
    }

    protected NativeSQLStatement createNativeSQLStatementAllWKTParams(final String sql, final String wkt) {
        return new NativeSQLStatement() {
            public PreparedStatement prepare(Connection connection) throws SQLException {
                PreparedStatement pstmt = connection.prepareStatement(sql);
                for (int i = 1; i <= numPlaceHoldersInSQL(sql); i++) {
                    pstmt.setString(i, wkt);
                }
                return pstmt;
            }
        };
    }

    protected NativeSQLStatement createNativeSQLStatement(final String sql, final Object[] params) {
        return new NativeSQLStatement() {
            public PreparedStatement prepare(Connection connection) throws SQLException {
                PreparedStatement pstmt = connection.prepareStatement(sql);
                int i = 1;
                for (Object param : params) {
                    pstmt.setObject(i++, param);
                }
                return pstmt;
            }
        };
    }


    protected int numPlaceHoldersInSQL(String sql) {
        return sql.replaceAll("[^?]", "").length();
    }
}
