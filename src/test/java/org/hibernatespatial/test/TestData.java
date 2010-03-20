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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.*;

/**
 * A <code>TestData</code> instance is a list object
 * that contains all the <code>TestDataElement</code>s that
 * are used in a unit test suite.
 *
 * @author Karel Maesen, Geovise BVBA
 */
public class TestData implements List<TestDataElement> {

    private List<TestDataElement> testDataElements;
    private InputStream in;

    private TestData(List<TestDataElement> testDataElements) {
        this.testDataElements = testDataElements;
    }

    public int size() {
        return testDataElements.size();
    }

    public boolean isEmpty() {
        return testDataElements.isEmpty();
    }

    public boolean contains(Object o) {
        return testDataElements.contains(o);
    }

    public Iterator<TestDataElement> iterator() {
        return testDataElements.iterator();
    }

    public Object[] toArray() {
        return testDataElements.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return testDataElements.toArray(a);
    }

    public boolean add(TestDataElement testDataElement) {
        return testDataElements.add(testDataElement);
    }

    public boolean remove(Object o) {
        return testDataElements.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return testDataElements.containsAll(c);
    }

    public boolean addAll(Collection<? extends TestDataElement> c) {
        return testDataElements.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends TestDataElement> c) {
        return testDataElements.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return testDataElements.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return testDataElements.retainAll(c);
    }

    public void clear() {
        testDataElements.clear();
    }

    public boolean equals(Object o) {
        return testDataElements.equals(o);
    }

    public int hashCode() {
        return testDataElements.hashCode();
    }

    public TestDataElement get(int index) {
        return testDataElements.get(index);
    }

    public TestDataElement set(int index, TestDataElement element) {
        return testDataElements.set(index, element);
    }

    public void add(int index, TestDataElement element) {
        testDataElements.add(index, element);
    }

    public TestDataElement remove(int index) {
        return testDataElements.remove(index);
    }

    public int indexOf(Object o) {
        return testDataElements.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return testDataElements.lastIndexOf(o);
    }

    public ListIterator<TestDataElement> listIterator() {
        return testDataElements.listIterator();
    }

    public ListIterator<TestDataElement> listIterator(int index) {
        return testDataElements.listIterator(index);
    }

    public List<TestDataElement> subList(int fromIndex, int toIndex) {
        return testDataElements.subList(fromIndex, toIndex);
    }

    public static TestData fromFile(String fileName) {
        if (fileName == null) throw new RuntimeException("Null test data file specified.");
        List<TestDataElement> testDataElements = new ArrayList<TestDataElement>();
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(getInputStream(fileName));
            addDataElements(document, testDataElements);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        return new TestData(testDataElements);
    }

    private static void addDataElements(Document document, List<TestDataElement> testDataElements) {
        Element root = document.getRootElement();
        for (Iterator it = root.elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            addDataElement(element, testDataElements);
        }
    }

    private static void addDataElement(Element element, List<TestDataElement> testDataElements) {
        int id = Integer.valueOf(element.selectSingleNode("id").getText());
        String type = element.selectSingleNode("type").getText();
        String wkt = element.selectSingleNode("wkt").getText();
        int srid = Integer.valueOf(element.selectSingleNode("srid").getText());
        TestDataElement testDataElement = new TestDataElement(id, type, wkt, srid);
        testDataElements.add(testDataElement);
    }

    private static InputStream getInputStream(String fileName) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (is == null) throw new RuntimeException(String.format("File %s not found on classpath.", fileName));
        return is;
    }
}
