/**
 * $Id$
 *
 * This file is part of Spatial Hibernate, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
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
 * For more information, visit: http://www.cadrie.com/
 */
 
package com.cadrie.hibernate.spatial.criterion;

import com.cadrie.hibernate.spatial.SpatialRelation;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Static Factory Class for creating spatial criterion types.
 * 
 * <p>
 * The criterion types created by this class implement the spatial query
 * expressions of the OpenGIS Simple Features Specification for SQL, Revision
 * 1.1.
 * 
 * In addition, it provides for a simple spatial <code>filter</code> that
 * works mostly using the spatial index. This corresponds to the Oracle
 * Spatial's "SDO_FILTER" function, or the "&&" operator of PostGIS.
 * </p>
 * 
 * @author Karel Maesen
 * 
 */
public class SpatialRestrictions {

    SpatialRestrictions() {
    }

    public static SpatialRelateExpression eq(String propertyName,
            Geometry filter, Geometry value) {
        return new SpatialRelateExpression(propertyName, filter, value,
                SpatialRelation.EQUALS);
    }

    public static SpatialRelateExpression within(String propertyName,
            Geometry filter, Geometry value) {
        return new SpatialRelateExpression(propertyName, filter, value,
                SpatialRelation.WITHIN);
    }

    public static SpatialRelateExpression contains(String propertyName,
            Geometry filter, Geometry value) {
        return new SpatialRelateExpression(propertyName, filter, value,
                SpatialRelation.CONTAINS);
    }

    public static SpatialRelateExpression crosses(String propertyName,
            Geometry filter, Geometry value) {
        return new SpatialRelateExpression(propertyName, filter, value,
                SpatialRelation.CROSSES);
    }

    public static SpatialRelateExpression disjoint(String propertyName,
            Geometry filter, Geometry value) {
        return new SpatialRelateExpression(propertyName, filter, value,
                SpatialRelation.DISJOINT);
    }

    public static SpatialRelateExpression intersects(String propertyName,
            Geometry filter, Geometry value) {
        return new SpatialRelateExpression(propertyName, filter, value,
                SpatialRelation.INTERSECTS);
    }

    public static SpatialRelateExpression overlaps(String propertyName,
            Geometry filter, Geometry value) {
        return new SpatialRelateExpression(propertyName, filter, value,
                SpatialRelation.OVERLAPS);
    }

    public static SpatialRelateExpression touches(String propertyName,
            Geometry filter, Geometry value) {
        return new SpatialRelateExpression(propertyName, filter, value,
                SpatialRelation.TOUCHES);
    }

    public static SpatialFilter filter(String propertyName, Geometry filter) {
        return new SpatialFilter(propertyName, filter);
    }

}
