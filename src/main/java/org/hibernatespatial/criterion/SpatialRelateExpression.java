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
package org.hibernatespatial.criterion;

import com.vividsolutions.jts.geom.Geometry;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.TypedValue;
import org.hibernatespatial.SpatialDialect;

/**
 * An implementation of the <code>Criterion</code> interface that implements
 * spatial queries: queries to the effect that a geometry property has a
 * specific spatial relation to a testsuite-suite geometry
 *
 * @author Karel Maesen
 */
public class SpatialRelateExpression implements Criterion {

    /**
     * The geometry property
     */
    private String propertyName = null;

    /**
     * The testsuite-suite geometry
     */
    private Geometry value = null;

    /**
     * An optional (bounding box) filter geometry.
     */
    private Geometry filter = null;

    /**
     * The spatial relation that is queried for.
     */
    private int spatialRelation = -1;

    private static final long serialVersionUID = 1L;

    public SpatialRelateExpression(String propertyName, Geometry filter,
                                   Geometry value, int spatialRelation) {
        this.propertyName = propertyName;
        this.spatialRelation = spatialRelation;
        this.filter = filter;
        this.value = value;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.hibernate.criterion.Criterion#getTypedValues(org.hibernate.Criteria,
      *      org.hibernate.criterion.CriteriaQuery)
      */

    public TypedValue[] getTypedValues(Criteria criteria,
                                       CriteriaQuery criteriaQuery) throws HibernateException {
        Dialect dialect = criteriaQuery.getFactory().getDialect();
        boolean twoPhaseFiltering = false;
        if (dialect instanceof SpatialDialect) {
            SpatialDialect sDialect = (SpatialDialect) dialect;
            twoPhaseFiltering = sDialect.isTwoPhaseFiltering();
        }
        if (filter != null && twoPhaseFiltering)
            return new TypedValue[]{
                    criteriaQuery.getTypedValue(criteria, propertyName, filter),
                    criteriaQuery.getTypedValue(criteria, propertyName, value)};
        else {
            return new TypedValue[]{criteriaQuery.getTypedValue(criteria,
                    propertyName, value)};
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see org.hibernate.criterion.Criterion#toSqlString(org.hibernate.Criteria,
      *      org.hibernate.criterion.CriteriaQuery)
      */

    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
            throws HibernateException {
        SessionFactoryImplementor factory = criteriaQuery.getFactory();
        String[] columns = criteriaQuery.getColumnsUsingProjection(criteria,
                this.propertyName);
        Dialect dialect = factory.getDialect();
        if (dialect instanceof SpatialDialect) {
            SpatialDialect seDialect = (SpatialDialect) dialect;
            if (filter != null && seDialect.isTwoPhaseFiltering()) {
                return seDialect.getSpatialRelateSQL(columns[0],
                        spatialRelation, true);
            } else {
                return seDialect.getSpatialRelateSQL(columns[0],
                        spatialRelation, false);
            }

        } else {
            throw new IllegalStateException(
                    "Dialect must be spatially enabled dialect");
        }
    }

}
