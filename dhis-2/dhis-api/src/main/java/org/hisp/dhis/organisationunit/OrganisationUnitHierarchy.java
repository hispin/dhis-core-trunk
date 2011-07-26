package org.hisp.dhis.organisationunit;

/*
 * Copyright (c) 2004-2010, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Lars Helge Overland
 */
public class OrganisationUnitHierarchy
{
    private Map<Integer, Collection<Integer>> preparedRelationships = new HashMap<Integer, Collection<Integer>>();
    
    private Map<Integer, Set<Integer>> relationships = new HashMap<Integer, Set<Integer>>();
    
    public OrganisationUnitHierarchy( Map<Integer, Set<Integer>> relationships )
    {
        this.relationships = relationships;
    }
    
    public OrganisationUnitHierarchy( Collection<OrganisationUnitRelationship> relations )
    {
        for ( OrganisationUnitRelationship relation : relations )
        {
            Set<Integer> children = relationships.get( relation.getParentId() );
            
            if ( children == null )
            {
                children = new HashSet<Integer>();
                relationships.put( relation.getParentId(), children );
            }
            
            children.add( relation.getChildId() );
        }
    }
    
    public OrganisationUnitHierarchy prepareChildren( Collection<OrganisationUnit> parents )
    {
        for ( OrganisationUnit unit : parents )
        {
            prepareChildren( unit );
        }
        
        return this;
    }
    
    public OrganisationUnitHierarchy prepareChildren( OrganisationUnit unit )
    {
        preparedRelationships.put( unit.getId(), getChildren( unit.getId() ) );
        
        return this;
    }
    
    public Collection<Integer> getChildren( int parentId )
    {
        if ( preparedRelationships.containsKey( parentId ) )
        {
            return preparedRelationships.get( parentId );
        }
        
        List<Integer> children = new ArrayList<Integer>();
        
        children.add( 0, parentId );

        int childCounter = 1;
        
        for ( int i = 0; i < childCounter; i++ )
        {
            Set<Integer> currentChildren = relationships.get( children.get( i ) );
            
            if ( currentChildren != null )
            {
                children.addAll( currentChildren );
            
                childCounter += currentChildren.size();
            }
        }
        
        return children;
    }
    
    public Collection<Integer> getChildren( Collection<Integer> parentIds )
    {
        Set<Integer> children = new HashSet<Integer>();
        
        for ( Integer id : parentIds )
        {
            children.addAll( getChildren( id ) );
        }
        
        return children;
    }
}

