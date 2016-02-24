package org.hisp.dhis.preheat;

/*
 * Copyright (c) 2004-2016, University of Oslo
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

import com.google.common.collect.Lists;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.query.Query;
import org.hisp.dhis.query.QueryService;
import org.hisp.dhis.query.Restrictions;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.PropertyType;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.system.util.ReflectionUtils;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Transactional
public class DefaultPreheatService implements PreheatService
{
    @Autowired
    private SchemaService schemaService;

    @Autowired
    private QueryService queryService;

    @Autowired
    private IdentifiableObjectManager manager;

    @Override
    @SuppressWarnings( "unchecked" )
    public Preheat preheat( PreheatParams params )
    {
        Preheat preheat = new Preheat();
        preheat.setDefaults( manager.getDefaults() );
        preheat.setUsernames( getUsernames() );

        if ( PreheatMode.ALL == params.getPreheatMode() )
        {
            if ( params.getClasses().isEmpty() )
            {
                schemaService.getMetadataSchemas().stream().filter( Schema::isIdentifiableObject )
                    .forEach( schema -> params.getClasses().add( (Class<? extends IdentifiableObject>) schema.getKlass() ) );
            }

            for ( Class<? extends IdentifiableObject> klass : params.getClasses() )
            {
                Query query = Query.from( schemaService.getDynamicSchema( klass ) );
                List<? extends IdentifiableObject> objects = queryService.query( query );

                if ( PreheatIdentifier.UID == params.getPreheatIdentifier() || PreheatIdentifier.AUTO == params.getPreheatIdentifier() )
                {
                    preheat.put( PreheatIdentifier.UID, objects );
                }

                if ( PreheatIdentifier.CODE == params.getPreheatIdentifier() || PreheatIdentifier.AUTO == params.getPreheatIdentifier() )
                {
                    preheat.put( PreheatIdentifier.CODE, objects );
                }
            }
        }
        else if ( PreheatMode.REFERENCE == params.getPreheatMode() )
        {
            Map<Class<? extends IdentifiableObject>, Set<String>> uidMap = params.getReferences().get( PreheatIdentifier.UID );
            Map<Class<? extends IdentifiableObject>, Set<String>> codeMap = params.getReferences().get( PreheatIdentifier.CODE );

            if ( uidMap != null && (PreheatIdentifier.UID == params.getPreheatIdentifier() || PreheatIdentifier.AUTO == params.getPreheatIdentifier()) )
            {
                for ( Class<? extends IdentifiableObject> klass : uidMap.keySet() )
                {
                    Collection<String> identifiers = uidMap.get( klass );
                    Query query = Query.from( schemaService.getDynamicSchema( klass ) );
                    query.add( Restrictions.in( "id", identifiers ) );
                    List<? extends IdentifiableObject> objects = queryService.query( query );
                    preheat.put( PreheatIdentifier.UID, objects );
                }
            }

            if ( codeMap != null && (PreheatIdentifier.CODE == params.getPreheatIdentifier() || PreheatIdentifier.AUTO == params.getPreheatIdentifier()) )
            {
                for ( Class<? extends IdentifiableObject> klass : codeMap.keySet() )
                {
                    Collection<String> identifiers = codeMap.get( klass );
                    Query query = Query.from( schemaService.getDynamicSchema( klass ) );
                    query.add( Restrictions.in( "code", identifiers ) );
                    List<? extends IdentifiableObject> objects = queryService.query( query );
                    preheat.put( PreheatIdentifier.CODE, objects );
                }
            }
        }

        return preheat;
    }

    @Override
    public void validate( PreheatParams params ) throws PreheatException
    {
        if ( PreheatMode.ALL == params.getPreheatMode() || PreheatMode.NONE == params.getPreheatMode() )
        {
            // nothing to validate for now, if classes is empty it will get all metadata classes
        }
        else if ( PreheatMode.REFERENCE == params.getPreheatMode() )
        {
            if ( params.getReferences().isEmpty() )
            {
                throw new PreheatException( "PreheatMode.REFERENCE, but no references was provided." );
            }
        }
        else
        {
            throw new PreheatException( "Invalid preheat mode." );
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Map<PreheatIdentifier, Map<Class<? extends IdentifiableObject>, Set<String>>> collectReferences( IdentifiableObject object )
    {
        if ( object == null )
        {
            return new HashMap<>();
        }

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> map = new HashMap<>();
        map.put( object.getClass(), Lists.newArrayList( (IdentifiableObject) object ) );

        return collectReferences( map );
    }

    @Override
    public Map<PreheatIdentifier, Map<Class<? extends IdentifiableObject>, Set<String>>> collectReferences( Collection<IdentifiableObject> objects )
    {
        if ( objects == null || objects.isEmpty() )
        {
            return new HashMap<>();
        }

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> map = new HashMap<>();
        map.put( objects.iterator().next().getClass(), Lists.newArrayList( objects ) );

        return collectReferences( map );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Map<PreheatIdentifier, Map<Class<? extends IdentifiableObject>, Set<String>>> collectReferences( Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> objects )
    {
        Map<PreheatIdentifier, Map<Class<? extends IdentifiableObject>, Set<String>>> map = new HashMap<>();

        map.put( PreheatIdentifier.UID, new HashMap<>() );
        map.put( PreheatIdentifier.CODE, new HashMap<>() );

        Map<Class<? extends IdentifiableObject>, Set<String>> uidMap = map.get( PreheatIdentifier.UID );
        Map<Class<? extends IdentifiableObject>, Set<String>> codeMap = map.get( PreheatIdentifier.CODE );

        if ( objects.isEmpty() )
        {
            return map;
        }

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> scanObjects = new HashMap<>();
        scanObjects.putAll( objects ); // clone objects list, we don't want to modify it

        if ( scanObjects.containsKey( User.class ) )
        {
            List<IdentifiableObject> users = scanObjects.get( User.class );
            List<IdentifiableObject> userCredentials = new ArrayList<>();

            for ( IdentifiableObject identifiableObject : users )
            {
                User user = (User) identifiableObject;

                if ( user.getUserCredentials() != null )
                {
                    userCredentials.add( user.getUserCredentials() );
                }
            }

            scanObjects.put( UserCredentials.class, userCredentials );
        }

        for ( Class<? extends IdentifiableObject> objectClass : scanObjects.keySet() )
        {
            Schema schema = schemaService.getDynamicSchema( objectClass );
            List<Property> properties = schema.getProperties().stream()
                .filter( p -> p.isPersisted() && p.isOwner() && (PropertyType.REFERENCE == p.getPropertyType() || PropertyType.REFERENCE == p.getItemPropertyType()) )
                .collect( Collectors.toList() );

            List<IdentifiableObject> identifiableObjects = scanObjects.get( objectClass );

            if ( !uidMap.containsKey( objectClass ) ) uidMap.put( objectClass, new HashSet<>() );
            if ( !codeMap.containsKey( objectClass ) ) codeMap.put( objectClass, new HashSet<>() );

            properties.forEach( p -> {
                for ( IdentifiableObject object : identifiableObjects )
                {
                    if ( !p.isCollection() )
                    {
                        Class<? extends IdentifiableObject> klass = (Class<? extends IdentifiableObject>) p.getKlass();

                        if ( !uidMap.containsKey( klass ) ) uidMap.put( klass, new HashSet<>() );
                        if ( !codeMap.containsKey( klass ) ) codeMap.put( klass, new HashSet<>() );

                        Object reference = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                        if ( reference != null )
                        {
                            IdentifiableObject identifiableObject = (IdentifiableObject) reference;

                            String uid = identifiableObject.getUid();
                            String code = identifiableObject.getCode();

                            if ( !StringUtils.isEmpty( uid ) ) uidMap.get( klass ).add( uid );
                            if ( !StringUtils.isEmpty( code ) ) codeMap.get( klass ).add( code );
                        }
                    }
                    else
                    {
                        Class<? extends IdentifiableObject> klass = (Class<? extends IdentifiableObject>) p.getItemKlass();

                        if ( !uidMap.containsKey( klass ) ) uidMap.put( klass, new HashSet<>() );
                        if ( !codeMap.containsKey( klass ) ) codeMap.put( klass, new HashSet<>() );

                        Collection<IdentifiableObject> reference = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                        for ( IdentifiableObject identifiableObject : reference )
                        {
                            String uid = identifiableObject.getUid();
                            String code = identifiableObject.getCode();

                            if ( !StringUtils.isEmpty( uid ) ) uidMap.get( klass ).add( uid );
                            if ( !StringUtils.isEmpty( code ) ) codeMap.get( klass ).add( code );
                        }
                    }

                    if ( !StringUtils.isEmpty( object.getUid() ) ) uidMap.get( objectClass ).add( object.getUid() );
                    if ( !StringUtils.isEmpty( object.getCode() ) ) codeMap.get( objectClass ).add( object.getCode() );
                }
            } );
        }

        return map;
    }

    @Override
    public List<PreheatValidation> checkReferences( List<IdentifiableObject> objects, Preheat preheat, PreheatIdentifier identifier )
    {
        List<PreheatValidation> preheatValidations = new ArrayList<>();
        objects.forEach( object -> preheatValidations.add( checkReferences( object, preheat, identifier ) ) );

        return preheatValidations;
    }

    @Override
    public PreheatValidation checkReferences( IdentifiableObject object, Preheat preheat, PreheatIdentifier identifier )
    {
        PreheatValidation preheatValidation = new PreheatValidation();

        if ( object == null )
        {
            return preheatValidation;
        }

        Schema schema = schemaService.getDynamicSchema( object.getClass() );
        schema.getProperties().stream()
            .filter( p -> p.isPersisted() && p.isOwner() && (PropertyType.REFERENCE == p.getPropertyType() || PropertyType.REFERENCE == p.getItemPropertyType()) )
            .forEach( p -> {
                if ( skipCheckAndConnect( p.getKlass() ) || skipCheckAndConnect( p.getItemKlass() ) )
                {
                    return;
                }

                if ( !p.isCollection() )
                {
                    IdentifiableObject refObject = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );
                    IdentifiableObject ref = preheat.get( identifier, refObject );

                    if ( ref == null && refObject != null && !Preheat.isDefault( refObject ) )
                    {
                        preheatValidation.addInvalidReference( object, identifier, refObject, p );
                    }
                }
                else
                {
                    Collection<IdentifiableObject> objects = ReflectionUtils.newCollectionInstance( p.getKlass() );
                    Collection<IdentifiableObject> refObjects = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                    for ( IdentifiableObject refObject : refObjects )
                    {
                        if ( Preheat.isDefault( refObject ) ) continue;

                        IdentifiableObject ref = preheat.get( identifier, refObject );

                        if ( ref == null && refObject != null )
                        {
                            preheatValidation.addInvalidReference( object, identifier, refObject, p );
                        }
                        else
                        {
                            objects.add( refObject );
                        }
                    }

                    ReflectionUtils.invokeMethod( object, p.getSetterMethod(), objects );
                }
            } );

        return preheatValidation;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> void connectReferences( T object, Preheat preheat, PreheatIdentifier identifier )
    {
        if ( object == null )
        {
            return;
        }

        Map<Class<? extends IdentifiableObject>, IdentifiableObject> defaults = preheat.getDefaults();

        Schema schema = schemaService.getDynamicSchema( object.getClass() );
        schema.getProperties().stream()
            .filter( p -> p.isPersisted() && p.isOwner() && (PropertyType.REFERENCE == p.getPropertyType() || PropertyType.REFERENCE == p.getItemPropertyType()) )
            .forEach( p -> {
                if ( skipCheckAndConnect( p.getKlass() ) || skipCheckAndConnect( p.getItemKlass() ) )
                {
                    return;
                }

                if ( !p.isCollection() )
                {
                    T refObject = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );
                    T ref = preheat.get( identifier, refObject );

                    if ( Preheat.isDefaultClass( refObject ) && (ref == null || "default".equals( refObject.getName() )) )
                    {
                        ref = (T) defaults.get( refObject.getClass() );
                    }

                    if ( ref != null && ref.getId() == 0 )
                    {
                        ReflectionUtils.invokeMethod( object, p.getSetterMethod(), (Object) null );
                    }
                    else
                    {
                        ReflectionUtils.invokeMethod( object, p.getSetterMethod(), ref );
                    }
                }
                else
                {
                    Collection<T> objects = ReflectionUtils.newCollectionInstance( p.getKlass() );
                    Collection<IdentifiableObject> refObjects = ReflectionUtils.invokeMethod( object, p.getGetterMethod() );

                    for ( IdentifiableObject refObject : refObjects )
                    {
                        T ref = preheat.get( identifier, (T) refObject );

                        if ( Preheat.isDefaultClass( refObject ) && (ref == null || "default".equals( refObject.getName() )) )
                        {
                            ref = (T) defaults.get( refObject.getClass() );
                        }

                        if ( ref != null && ref.getId() != 0 ) objects.add( ref );
                    }

                    ReflectionUtils.invokeMethod( object, p.getSetterMethod(), objects );
                }
            } );
    }

    @SuppressWarnings( "unchecked" )
    private Map<String, UserCredentials> getUsernames()
    {
        Map<String, UserCredentials> userCredentialsMap = new HashMap<>();
        Query query = Query.from( schemaService.getDynamicSchema( UserCredentials.class ) );
        List<UserCredentials> userCredentials = (List<UserCredentials>) queryService.query( query );

        for ( UserCredentials uc : userCredentials )
        {
            userCredentialsMap.put( uc.getUsername(), uc );
        }

        return userCredentialsMap;
    }

    private boolean skipCheckAndConnect( Class<?> klass )
    {
        return klass != null && (UserCredentials.class.isAssignableFrom( klass ) || DataElementOperand.class.isAssignableFrom( klass ));
    }
}
