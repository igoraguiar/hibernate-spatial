package org.hibernatespatial.test;

import org.hibernate.cfg.Configuration;
import org.hibernate.testing.junit.functional.FunctionalTestCase;


/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Sep 30, 2010
 */
public abstract class TestSupport {

    protected Configuration configuration;

    public DataSourceUtils createDataSourceUtil(Configuration configuration) {
        this.configuration = configuration;
        return new DataSourceUtils(driver(), url(), user(), passwd(), getSQLExpressionTemplate());
    }

    public GeometryEquality createGeometryEquality() {
        return new GeometryEquality();
    }

    public abstract TestData createTestData(FunctionalTestCase testcase);

    public abstract AbstractExpectationsFactory createExpectationsFactory(DataSourceUtils dataSourceUtils);

    public abstract SQLExpressionTemplate getSQLExpressionTemplate();

    protected String driver() {
        return configuration.getProperty("hibernate.connection.driver_class");
    }

    protected String url() {
        return configuration.getProperty("hibernate.connection.url");
    }

    protected String user() {
        return configuration.getProperty("hibernate.connection.username");
    }

    protected String passwd() {
        return configuration.getProperty("hibernate.connection.password");
    }
}