/*
 * $Id: TestStoreRetrieve.java 242 2010-09-22 20:40:07Z maesenka $
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

package org.hibernatespatial.testsuite;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernatespatial.test.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This testsuite-suite class verifies whether the <code>Geometry</code>s retrieved
 * are equal to the <code>Geometry</code>s stored.
 */
public class TestStoreRetrieve extends SpatialFunctionalTestCase {

    private static Logger LOGGER = LoggerFactory.getLogger(TestStoreRetrieve.class);

    public TestStoreRetrieve(String string){
        super(string);
    }


    protected Logger getLogger(){
        return LOGGER;
    }


    public void test_store_retrieve() throws ParseException {
        Map<Integer, GeomEntity> stored = new HashMap<Integer, GeomEntity>();
        storeTestObjects(stored);
        retrieveAndCompare(stored);
    }

    public void test_store_retrieve_null_geometry() {
        storeNullGeometry();
        retrieveNullGeometry();
    }

    private void retrieveAndCompare(Map<Integer, GeomEntity> stored) {
        int id = -1;
        Transaction tx = null;
        Session session = null;
        try {
            session = openSession();
            tx = session.beginTransaction();
            for (GeomEntity storedEntity : stored.values()) {
                id = storedEntity.getId();
                GeomEntity retrievedEntity = (GeomEntity) session.get(GeomEntity.class, id);
                Geometry retrievedGeometry = retrievedEntity.getGeom();
                Geometry storedGeometry = storedEntity.getGeom();
                String msg = createFailureMessage(storedEntity.getId(), storedGeometry, retrievedGeometry);
                assertTrue(msg, geometryEquality.test(storedGeometry, retrievedGeometry));
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(String.format("Failure on case: %d", id), e);
        }
        finally {
            if (session != null) session.close();
        }
    }

    private String createFailureMessage(int id, Geometry storedGeometry, Geometry retrievedGeometry) {
        String expectedText = (storedGeometry != null ? storedGeometry.toText() : "NULL");
        String retrievedText = (retrievedGeometry != null ? retrievedGeometry.toText() : "NULL");
        return String.format("Equality testsuite-suite failed for %d.\nExpected: %s\nReceived:%s", id, expectedText, retrievedText);
    }

    private void storeTestObjects(Map<Integer, GeomEntity> stored) {
        Session session = null;
        Transaction tx = null;
        int id = -1;
        try {
            session = openSession();
            // Every testsuite-suite instance is committed seperately
            // to improve feedback in case of failure
            for (TestDataElement element : testData) {
                id = element.id;
                tx = session.beginTransaction();
                GeomEntity entity = GeomEntity.createFrom(element);
                stored.put(entity.getId(), entity);
                session.save(entity);
                tx.commit();
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed storing testsuite-suite object with id:" + id, e);
        } finally {
            if (session != null) session.close();
        }
    }

    private void storeNullGeometry() {
        GeomEntity entity = null;
        Session session = null;
        Transaction tx = null;
        try {
            session = openSession();
            tx = session.beginTransaction();
            entity = new GeomEntity();
            entity.setId(1);
            entity.setType("NULL OBJECT");
            session.save(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed storing testsuite-suite object with id:" + entity.getId(), e);
        } finally {
            if (session != null) session.close();
        }
    }


    private void retrieveNullGeometry() {
        Transaction tx = null;
        Session session = null;
        try {
            session = openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(GeomEntity.class);
            List<GeomEntity> retrieved = criteria.list();
            assertEquals("Expected exactly one result", 1, retrieved.size());
            GeomEntity entity = retrieved.get(0);
            assertNull(entity.getGeom());
            tx.commit();
        } catch(Exception e){
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        } finally {
            if (session != null) session.close();
        }
    }


}