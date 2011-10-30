/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.metadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * XMP property definition. Each instance of this class defines a single
 * metadata property like "dc:format". In addition to the property name,
 * the {@link ValueType value type} and category (internal or external)
 * of the property are included in the property definition. The available
 * choice values are also stored for open and closed choice value types.
 *
 * @since Apache Tika 0.7
 */
public final class Property implements Comparable<Property> {

    public static enum PropertyType {
        SIMPLE, STRUCTURE, BAG, SEQ, ALT
    }

    public static enum ValueType {
        BOOLEAN, OPEN_CHOICE, CLOSED_CHOICE, DATE, INTEGER, LOCALE,
        MIME_TYPE, PROPER_NAME, RATIONAL, REAL, TEXT, URI, URL, XPATH
    }

    private static final Map<String, Property> properties =
            new HashMap<String, Property>();

    private final String name;

    private final boolean internal;

    private final PropertyType propertyType;

    private final ValueType valueType;

    /**
     * The available choices for the open and closed choice value types.
     */
    private final Set<String> choices;

    /**
     * The canonical property of which this one is an alias, or
     * <code>null</code> if this is not an alias property.
     */
    private final Property aliasOf;

    private Property(
            String name, boolean internal, PropertyType propertyType,
            ValueType valueType, Set<String> choices, Property aliasOf) {
        this.name = name;
        this.internal = internal;
        this.propertyType = propertyType;
        this.valueType = valueType;
        this.choices = choices;
        this.aliasOf = aliasOf;

        synchronized (properties) {
            properties.put(name, this);
        }
    }

    private static Set<String> unmodifiableSet(String[] array) {
        if (array != null) {
            Set<String> set = new HashSet<String>();
            set.addAll(Arrays.asList(array));
            return Collections.unmodifiableSet(set);
        } else {
            return null;
        }
    }

    private Property(String name, Property aliasOf) {
        this(name, aliasOf.internal, aliasOf.propertyType,
                aliasOf.valueType, aliasOf.choices, aliasOf);
    }

    private Property(
            String name, boolean internal, PropertyType propertyType,
            ValueType valueType, String[] choices) {
        this(name, internal, propertyType, valueType,
                unmodifiableSet(choices), null);
    }

    private Property(
            String name, boolean internal,
            ValueType valueType, String[] choices) {
        this(name, internal, PropertyType.SIMPLE, valueType, choices);
    }

    private Property(String name, boolean internal, ValueType valueType) {
        this(name, internal, PropertyType.SIMPLE, valueType, null);
    }

    private Property(
            String name, boolean internal,
            PropertyType propertyType, ValueType valueType) {
        this(name, internal, propertyType, valueType, null);
    }

    public String getName() {
        return name;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isExternal() {
        return !internal;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public ValueType getValueType() {
        return valueType;
    }

    /**
     * Returns the (immutable) set of choices for the values of this property.
     * Only defined for {@link ValueType#OPEN_CHOICE open} and
     * {@link ValueType#CLOSED_CHOICE closed choice} value types.
     *
     * @return available choices, or <code>null</code>
     */
    public Set<String> getChoices() {
        return choices;
    }

    /**
     * Returns the canonical property of which this one is an alias,
     * or <code>null</code> if this is not an alias property.
     *
     * @since Apache Tika 1.0
     * @return canonical property, or <code>null</code>
     */
    public Property getAliasOf() {
        return aliasOf;
    }

    public static SortedSet<Property> getProperties(String prefix) {
        SortedSet<Property> set = new TreeSet<Property>();
        String p = prefix + ":";
        synchronized (properties) {
            for (String name : properties.keySet()) {
                if (name.startsWith(p)) {
                    set.add(properties.get(name));
                }
            }
        }
        return set;
    }

    public static Property getProperty(String name) {
        synchronized (properties) {
            return properties.get(name);
        }
    }

    public static Property alias(String name, Property aliasOf) {
        return new Property(name, aliasOf);
    }

    public static Property internalBoolean(String name) {
        return new Property(name, true, ValueType.BOOLEAN);
    }

    public static Property internalClosedChoise(
            String name, String... choices) {
        return new Property(name, true, ValueType.CLOSED_CHOICE, choices);
    }

    public static Property internalDate(String name) {
        return new Property(name, true, ValueType.DATE);
    }

    public static Property internalInteger(String name) {
        return new Property(name, true, ValueType.INTEGER);
    }

    public static Property internalIntegerSequence(String name) {
        return new Property(name, true, PropertyType.SEQ, ValueType.INTEGER);
    }

    public static Property internalRational(String name) {
        return new Property(name, true, ValueType.RATIONAL);
    }

    public static Property internalOpenChoise(
            String name, String... choices) {
        return new Property(name, true, ValueType.OPEN_CHOICE, choices);
    }
    public static Property internalReal(String name) {
        return new Property(name, true, ValueType.REAL);
    }

    public static Property internalText(String name) {
        return new Property(name, true, ValueType.TEXT);
    }

    /** @since Apache Tika 1.0 */
    public static Property internalMIMEType(String name) {
        return new Property(name, true, ValueType.MIME_TYPE);
    }

    public static Property internalURI(String name) {
        return new Property(name, true, ValueType.URI);
    }

    public static Property externalClosedChoise(
            String name, String... choices) {
        return new Property(name, false, ValueType.CLOSED_CHOICE, choices);
    }

    public static Property externalDate(String name) {
        return new Property(name, false, ValueType.DATE);
    }

    public static Property externalReal(String name) {
       return new Property(name, false, ValueType.REAL);
   }

    public static Property externalInteger(String name) {
        return new Property(name, false, ValueType.INTEGER);
    }

    public static Property externalBoolean(String name) {
       return new Property(name, false, ValueType.BOOLEAN);
   }

    public static Property externalText(String name) {
        return new Property(name, false, ValueType.TEXT);
    }

    //----------------------------------------------------------< Comparable >

    public int compareTo(Property o) {
        return name.compareTo(o.name);
    }

    //--------------------------------------------------------------< Object >

    public boolean equals(Object o) {
        return o instanceof Property && name.equals(((Property) o).name);
    }

    public int hashCode() {
        return name.hashCode();
    }

}
