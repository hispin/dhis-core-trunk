package org.hisp.dhis.system.util;

/*
 * Copyright (c) 2004-2013, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.common.collect.Maps;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.annotation.Description;
import org.hisp.dhis.system.util.functional.Function1;
import org.hisp.dhis.system.util.functional.Predicate;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hisp.dhis.system.util.PredicateUtils.alwaysTrue;

/**
 * @author Lars Helge Overland
 */
public class ReflectionUtils
{
    /**
     * Invokes method getId() for this object and returns the return value. An
     * int return type is expected. If the operation fails -1 is returned.
     *
     * @param object object to call getId() on.
     * @return The identifier.
     */
    public static int getId( Object object )
    {
        try
        {
            Method method = object.getClass().getMethod( "getId" );

            return (Integer) method.invoke( object );
        }
        catch ( Exception ex )
        {
            return -1;
        }
    }

    /**
     * Fetch a property off the object. Returns null if the operation fails.
     *
     * @param object   the object.
     * @param property name of the property to get.
     * @return the value of the property or null.
     */
    public static String getProperty( Object object, String property )
    {
        try
        {
            property = property.substring( 0, 1 ).toUpperCase() + property.substring( 1, property.length() );

            Method method = object.getClass().getMethod( "get" + property );

            return (String) method.invoke( object );
        }
        catch ( Exception ex )
        {
            return null;
        }
    }

    /**
     * Sets a property for the supplied object. Throws an
     * UnsupportedOperationException if the operation fails.
     *
     * @param object Object to modify
     * @param name   Name of property to set
     * @param value  Value the property will be set to
     */
    public static void setProperty( Object object, String name, String value )
    {
        Object[] arguments = new Object[]{ value };

        Class<?>[] parameterTypes = new Class<?>[]{ String.class };

        if ( name.length() > 0 )
        {
            name = "set" + name.substring( 0, 1 ).toUpperCase() + name.substring( 1, name.length() );

            try
            {
                Method concatMethod = object.getClass().getMethod( name, parameterTypes );

                concatMethod.invoke( object, arguments );
            }
            catch ( Exception ex )
            {
                throw new UnsupportedOperationException( "Failed to set property", ex );
            }
        }
    }

    /**
     * Sets a property for the supplied object. Throws an
     * UnsupportedOperationException if the operation fails.
     *
     * @param object     Object to modify
     * @param namePrefix prefix of the property name to set
     * @param name       Name of property to set
     * @param value      Value the property will be set to
     */
    public static void setProperty( Object object, String namePrefix, String name, String value )
    {
        String prefixed = namePrefix + name.substring( 0, 1 ).toUpperCase() + name.substring( 1, name.length() );

        setProperty( object, prefixed, value );
    }

    /**
     * Returns the name of the class that the object is an instance of
     * org.hisp.dhis.indicator.Indicactor returns Indicator.
     *
     * @param object object to determine className for.
     * @return String containing the class name.
     */
    public static String getClassName( Object object )
    {
        return object.getClass().getSimpleName();
    }

    /**
     * Test whether the object is an array or a Collection.
     *
     * @param value the object.
     * @return true if the object is an array or a Collection, false otherwise.
     */
    public static boolean isCollection( Object value )
    {
        if ( value != null )
        {
            if ( value.getClass().isArray() || value instanceof Collection<?> )
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isCollection( String fieldName, Object object, Class<?> type )
    {
        return isCollection( fieldName, object, type, null );
    }

    public static boolean isCollection( String fieldName, Object object, Class<?> type, Class<? extends Annotation> annotation )
    {
        Field field;

        field = _findField( object.getClass(), fieldName );

        if ( field == null )
        {
            return false;
        }

        if ( annotation != null )
        {
            if ( !field.isAnnotationPresent( annotation ) )
            {
                return false;
            }
        }

        try
        {
            if ( Collection.class.isAssignableFrom( field.getType() ) )
            {
                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if ( actualTypeArguments.length > 0 )
                {
                    if ( type.isAssignableFrom( (Class<?>) actualTypeArguments[0] ) )
                    {
                        return true;
                    }
                }
            }

        }
        catch ( ClassCastException e )
        {
            return false;
        }

        return false;
    }

    public static Method findGetterMethod( String fieldName, Object target )
    {
        return findGetterMethod( fieldName, target.getClass() );
    }

    public static Method findGetterMethod( String fieldName, Class<?> clazz )
    {
        String[] getterNames = new String[]{
            "get",
            "is",
            "has"
        };

        Field field = _findField( clazz, StringUtils.uncapitalize( fieldName ) );
        Method method;

        if ( field != null )
        {
            for ( String getterName : getterNames )
            {
                method = _findMethod( clazz, getterName + StringUtils.capitalize( field.getName() ) );

                if ( method != null )
                {
                    return method;
                }
            }
        }

        return null;

    }

    public static <T> T invokeGetterMethod( String fieldName, Object target )
    {
        Method method = findGetterMethod( fieldName, target );

        if ( method != null )
        {
            return invokeMethod( target, method );
        }

        return null;
    }

    public static Method findSetterMethod( String fieldName, Object target )
    {
        String[] setterNames = new String[]{
            "set"
        };

        Field field = _findField( target.getClass(), StringUtils.uncapitalize( fieldName ) );
        Method method;

        if ( field != null )
        {
            for ( String setterName : setterNames )
            {
                method = _findMethod( target.getClass(), setterName + StringUtils.capitalize( field.getName() ), field.getType() );

                if ( method != null )
                {
                    return method;
                }
            }
        }

        return null;
    }

    public static <T> T invokeSetterMethod( String fieldName, Object target, Object... args )
    {
        Method method = findSetterMethod( fieldName, target );

        if ( method != null )
        {
            return invokeMethod( target, method, args );
        }

        return null;
    }

    public static boolean isType( Field field, Class<?> clazz )
    {
        Class<?> type = field.getType();

        return clazz.isAssignableFrom( type );
    }

    private static Field _findField( Class<?> clazz, String name )
    {
        return _findField( clazz, name, null );
    }

    private static Field _findField( Class<?> clazz, String name, Class<?> type )
    {
        Class<?> searchType = clazz;

        while ( !Object.class.equals( searchType ) && searchType != null )
        {
            Field[] fields = searchType.getDeclaredFields();

            for ( Field field : fields )
            {
                // && (type == null || type.equals( field.getType() ))
                if ( (name == null || name.equals( field.getName() )) )
                {
                    return field;
                }
            }

            searchType = searchType.getSuperclass();
        }

        return null;
    }

    public static List<Field> getAllFields( Class<?> clazz )
    {
        Class<?> searchType = clazz;
        List<Field> fields = new ArrayList<Field>();

        while ( !Object.class.equals( searchType ) && searchType != null )
        {
            fields.addAll( Arrays.asList( searchType.getDeclaredFields() ) );
            searchType = searchType.getSuperclass();
        }

        return fields;
    }

    private static Method _findMethod( Class<?> clazz, String name )
    {
        return _findMethod( clazz, name, new Class[0] );
    }

    private static Method _findMethod( Class<?> clazz, String name, Class<?>... paramTypes )
    {
        Class<?> searchType = clazz;

        while ( searchType != null )
        {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());

            for ( Method method : methods )
            {
                if ( name.equals( method.getName() ) && (paramTypes == null || Arrays.equals( paramTypes, method.getParameterTypes() )) )
                {
                    return method;
                }
            }

            searchType = searchType.getSuperclass();
        }

        return null;
    }

    public static List<Method> getAllMethods( Class<?> clazz )
    {
        Class<?> searchType = clazz;
        List<Method> methods = new ArrayList<Method>();

        while ( searchType != null )
        {
            Method[] methodArray = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
            methods.addAll( Arrays.asList( methodArray ) );
            searchType = searchType.getSuperclass();
        }

        return methods;
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod( Object target, Method method, Object... args )
    {
        try
        {
            return (T) method.invoke( target, args );
        }
        catch ( InvocationTargetException e )
        {
            throw new RuntimeException( e );
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( e );
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldObject( Field field, T target )
    {
        return (T) invokeGetterMethod( field.getName(), target );
    }

    public static void executeOnFields( Class<?> clazz, Function1<Field> function )
    {
        executeOnFields( clazz, function, null );
    }

    public static void executeOnFields( Class<?> clazz, Function1<Field> function, Predicate<Field> predicate )
    {
        Class<?> currentType = clazz;

        while ( !Object.class.equals( currentType ) && currentType != null )
        {
            Field[] fields = currentType.getDeclaredFields();

            for ( Field field : fields )
            {
                if ( Modifier.isStatic( field.getModifiers() ) || (predicate != null && !predicate.evaluate( field )) )
                {
                    continue;
                }

                function.apply( field );
            }

            currentType = currentType.getSuperclass();
        }
    }

    public static Collection<Field> collectFields( Class<?> clazz )
    {
        return collectFields( clazz, alwaysTrue );
    }

    public static Collection<Field> collectFields( Class<?> clazz, Predicate<Field> predicate )
    {
        Class<?> type = clazz;
        Collection<Field> fields = new ArrayList<Field>();

        while ( !Object.class.equals( type ) && type != null )
        {
            Field[] declaredFields = type.getDeclaredFields();

            for ( Field field : declaredFields )
            {
                if ( Modifier.isStatic( field.getModifiers() ) || (predicate != null && !predicate.evaluate( field )) )
                {
                    continue;
                }

                fields.add( field );
            }

            type = type.getSuperclass();
        }

        return fields;
    }

    public static <E> Collection<E> newCollectionInstance( Class<?> clazz )
    {
        if ( List.class.isAssignableFrom( clazz ) )
        {
            return new ArrayList<E>();
        }
        else if ( Set.class.isAssignableFrom( clazz ) )
        {
            return new HashSet<E>();
        }
        else
        {
            throw new RuntimeException( "Unknown Collection type." );
        }
    }

    public static String getJacksonAlias( String fieldName, Class<?> clazz )
    {
        Method method = findGetterMethod( fieldName, clazz );

        if ( method == null )
        {
            return fieldName;
        }

        JacksonXmlProperty xmlProperty = method.getAnnotation( JacksonXmlProperty.class );

        if ( xmlProperty != null )
        {
            return StringUtils.isEmpty( xmlProperty.localName() ) ? fieldName : xmlProperty.localName();
        }

        JsonProperty jsonProperty = method.getAnnotation( JsonProperty.class );

        if ( jsonProperty != null )
        {
            return StringUtils.isEmpty( jsonProperty.value() ) ? fieldName : jsonProperty.value();
        }

        return fieldName;
    }

    private static Map<Class<?>, Map<String, PropertyDescriptor>> classMapCache = Maps.newHashMap();

    public static class PropertyDescriptor
    {
        private String name;

        private String description;

        private String xmlName;

        private boolean xmlAttribute;

        private String xmlCollectionName;

        private Class<?> clazz;

        private Method method;

        private boolean collection;

        private boolean identifiableObject;

        private PropertyDescriptor( Method method )
        {
            this.method = method;
        }

        @JsonProperty
        public String getName()
        {
            return name;
        }

        public void setName( String name )
        {
            this.name = name;
        }

        @JsonProperty
        public String getDescription()
        {
            return description;
        }

        public void setDescription( String description )
        {
            this.description = description;
        }

        @JsonProperty
        public String getXmlName()
        {
            return xmlName;
        }

        public void setXmlName( String xmlName )
        {
            this.xmlName = xmlName;
        }

        @JsonProperty
        public boolean isXmlAttribute()
        {
            return xmlAttribute;
        }

        public void setXmlAttribute( boolean xmlAttribute )
        {
            this.xmlAttribute = xmlAttribute;
        }

        @JsonProperty
        public String getXmlCollectionName()
        {
            return xmlCollectionName;
        }

        public void setXmlCollectionName( String xmlCollectionName )
        {
            this.xmlCollectionName = xmlCollectionName;
        }

        @JsonProperty
        public Class<?> getClazz()
        {
            return clazz;
        }

        public void setClazz( Class<?> clazz )
        {
            this.clazz = clazz;
        }

        public Method getMethod()
        {
            return method;
        }

        public void setMethod( Method method )
        {
            this.method = method;
        }

        @JsonProperty
        public boolean isCollection()
        {
            return collection;
        }

        public void setCollection( boolean collection )
        {
            this.collection = collection;
        }

        @JsonProperty
        public boolean isIdentifiableObject()
        {
            return identifiableObject;
        }

        public void setIdentifiableObject( boolean identifiableObject )
        {
            this.identifiableObject = identifiableObject;
        }

        @Override
        public String toString()
        {
            return "PropertyDescriptor{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", xmlName='" + xmlName + '\'' +
                ", xmlAttribute=" + xmlAttribute +
                ", xmlCollectionName='" + xmlCollectionName + '\'' +
                ", clazz=" + clazz +
                ", method=" + method +
                ", collection=" + collection +
                ", identifiableObject=" + identifiableObject +
                '}';
        }
    }

    public static Map<String, PropertyDescriptor> getJacksonClassMap( Class<?> clazz )
    {
        if ( classMapCache.containsKey( clazz ) )
        {
            return classMapCache.get( clazz );
        }

        Map<String, PropertyDescriptor> output = Maps.newLinkedHashMap();
        List<Method> allMethods = getAllMethods( clazz );

        for ( Method method : allMethods )
        {
            if ( method.isAnnotationPresent( JsonProperty.class ) )
            {
                JsonProperty jsonProperty = method.getAnnotation( JsonProperty.class );
                PropertyDescriptor descriptor = new PropertyDescriptor( method );

                String name = jsonProperty.value();

                if ( StringUtils.isEmpty( name ) )
                {
                    String[] getters = new String[]{
                        "is", "has", "get"
                    };

                    name = method.getName();

                    for ( String getter : getters )
                    {
                        if ( name.startsWith( getter ) )
                        {
                            name = name.substring( getter.length() );
                        }
                    }

                    name = StringUtils.uncapitalize( name );
                }

                if ( method.isAnnotationPresent( Description.class ) )
                {
                    Description description = method.getAnnotation( Description.class );
                    descriptor.setDescription( description.value() );
                }

                if ( method.isAnnotationPresent( JacksonXmlProperty.class ) )
                {
                    JacksonXmlProperty jacksonXmlProperty = method.getAnnotation( JacksonXmlProperty.class );

                    if ( jacksonXmlProperty.localName().isEmpty() )
                    {
                        descriptor.setXmlName( name );
                    }
                    else
                    {
                        descriptor.setXmlName( jacksonXmlProperty.localName() );
                    }

                    descriptor.setXmlAttribute( jacksonXmlProperty.isAttribute() );
                }

                if ( method.isAnnotationPresent( JacksonXmlElementWrapper.class ) )
                {
                    JacksonXmlElementWrapper jacksonXmlElementWrapper = method.getAnnotation( JacksonXmlElementWrapper.class );
                    descriptor.setXmlCollectionName( jacksonXmlElementWrapper.localName() );
                }

                descriptor.setName( name );
                output.put( name, descriptor );

                Class<?> returnType = method.getReturnType();
                descriptor.setClazz( returnType );

                if ( IdentifiableObject.class.isAssignableFrom( returnType ) )
                {
                    descriptor.setIdentifiableObject( true );
                }
                else if ( Collection.class.isAssignableFrom( returnType ) )
                {
                    descriptor.setCollection( true );

                    Type type = method.getGenericReturnType();

                    if ( ParameterizedType.class.isInstance( type ) )
                    {
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        Class<?> klass = (Class<?>) parameterizedType.getActualTypeArguments()[0];

                        if ( IdentifiableObject.class.isAssignableFrom( klass ) )
                        {
                            descriptor.setIdentifiableObject( true );
                        }
                    }
                }
            }
        }

        classMapCache.put( clazz, output );

        return output;
    }
}
