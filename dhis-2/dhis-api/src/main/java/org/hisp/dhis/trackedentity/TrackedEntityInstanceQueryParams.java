package org.hisp.dhis.trackedentity;

/*
 * Copyright (c) 2004-2014, University of Oslo
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.common.QueryItem;
import org.hisp.dhis.common.SetMap;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;

/**
 * @author Lars Helge Overland
 */
public class TrackedEntityInstanceQueryParams
{
    public static final String TRACKED_ENTITY_INSTANCE_ID = "instance";
    public static final String CREATED_ID = "created";
    public static final String LAST_UPDATED_ID = "lastupdated";
    public static final String ORG_UNIT_ID = "ou";
    public static final String TRACKED_ENTITY_ID = "te";
    public static final String TRACKED_ENTITY_ATTRIBUTE_ID = "teattribute";
    public static final String TRACKED_ENTITY_ATTRIBUTE_VALUE_ID = "tevalue";
    
    /**
     * Query value, will apply to all relevant attributes.
     */
    private String query;
    
    /**
     * Attributes to be included in the response. Can be used to filter response.
     */
    private List<QueryItem> attributes = new ArrayList<QueryItem>();

    /**
     * Filters for the response.
     */
    private List<QueryItem> filters = new ArrayList<QueryItem>();
    
    /**
     * Organisation units for which instances in the response were registered at.
     * Is related to the specified OrganisationUnitMode.
     */
    private Set<OrganisationUnit> organisationUnits = new HashSet<OrganisationUnit>();
    
    /**
     * Program for which instances in the response must be enrolled in.
     */
    private Program program;
    
    /**
     * Tracked entity of the instances in the response.
     */
    private TrackedEntity trackedEntity;
    
    /**
     * Selection mode for the specified organisation units.
     */
    private OrganisationUnitSelectionMode organisationUnitMode;

    /**
     * Page number.
     */
    private Integer page;
    
    /**
     * Page size.
     */
    private Integer pageSize;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public TrackedEntityInstanceQueryParams()
    {
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------
    
    /**
     * Returns a mapping between level and organisation units.
     */
    public SetMap<Integer, OrganisationUnit> getLevelOrgUnitMap()
    {
        SetMap<Integer, OrganisationUnit> setMap = new SetMap<Integer, OrganisationUnit>();
        
        for ( OrganisationUnit ou : organisationUnits )
        {
            setMap.putValue( ou.getLevel(), ou );
        }
        
        return setMap;
    }
    
    /**
     * Indicates whether this is a logical OR query, meaning that a query string
     * is specified and instances which matches this query on one or more attributes
     * should be included in the response. The opposite is an item-specific query,
     * where the instances which matches the specific attributes should be included.
     */
    public boolean isOrQuery()
    {
        return hasQuery();
    }
    
    /**
     * Indicates whether this params specifies a query.
     */
    public boolean hasQuery()
    {
        return query != null && !query.isEmpty();
    }
    
    /**
     * Returns a list of attributes and filters combined.
     */
    public List<QueryItem> getAttributesAndFilters()
    {
        List<QueryItem> items = new ArrayList<QueryItem>();
        items.addAll( attributes );
        items.addAll( filters );
        return items;
    }

    /**
     * Indicates whether this params specifies any attributes and/or filters.
     */
    public boolean hasAttributesOrFilters()
    {
        return hasAttributes() || hasFilters();
    }

    /**
     * Indicates whether this params specifies any attributes.
     */
    public boolean hasAttributes()
    {
        return attributes != null && !attributes.isEmpty();
    }
    
    /**
     * Indicates whether this params specifies any filters.
     */
    public boolean hasFilters()
    {
        return filters != null && !filters.isEmpty();
    }

    /**
     * Indicates whether this params specifies any organisation units.
     */
    public boolean hasOrganisationUnits()
    {
        return organisationUnits != null && !organisationUnits.isEmpty();
    }
    
    /**
     * Indicates whether this params specifies a program.
     */
    public boolean hasProgram()
    {
        return program != null;
    }
    
    /**
     * Indicates whether this params specifies a tracked entity.
     */
    public boolean hasTrackedEntity()
    {
        return trackedEntity != null;
    }
    
    /**
     * Indicates whethert this params is of the given organisation unit mode.
     */
    public boolean isOrganisationUnitMode( OrganisationUnitSelectionMode mode )
    {
        return organisationUnitMode != null && organisationUnitMode.equals( mode );
    }
    
    /**
     * Indicates whether paging is enabled.
     */
    public boolean isPaging()
    {
        return page != null || pageSize != null;
    }

    /**
     * Returns the page number, falls back to default value of 1 if not specified.
     */
    public int getPageWithDefault()
    {
        return page != null && page > 0 ? page : 1;
    }
    
    /**
     * Returns the page size, falls back to default value of 50 if not specified.
     */
    public int getPageSizeWithDefault()
    {
        return pageSize != null && pageSize >= 0 ? pageSize : 50;
    }

    /**
     * Returns the offset based on the page number and page size.
     */
    public int getOffset()
    {
        return ( getPageWithDefault() - 1 ) * getPageSizeWithDefault();
    }
    
    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getQuery()
    {
        return query;
    }

    public void setQuery( String query )
    {
        this.query = query;
    }

    public List<QueryItem> getAttributes()
    {
        return attributes;
    }

    public void setAttributes( List<QueryItem> attributes )
    {
        this.attributes = attributes;
    }

    public List<QueryItem> getFilters()
    {
        return filters;
    }

    public void setFilters( List<QueryItem> filters )
    {
        this.filters = filters;
    }

    public OrganisationUnitSelectionMode getOrganisationUnitMode()
    {
        return organisationUnitMode;
    }

    public void setOrganisationUnitMode( OrganisationUnitSelectionMode organisationUnitMode )
    {
        this.organisationUnitMode = organisationUnitMode;
    }

    public Program getProgram()
    {
        return program;
    }

    public void setProgram( Program program )
    {
        this.program = program;
    }

    public TrackedEntity getTrackedEntity()
    {
        return trackedEntity;
    }

    public void setTrackedEntity( TrackedEntity trackedEntity )
    {
        this.trackedEntity = trackedEntity;
    }

    public Set<OrganisationUnit> getOrganisationUnits()
    {
        return organisationUnits;
    }

    public void setOrganisationUnits( Set<OrganisationUnit> organisationUnits )
    {
        this.organisationUnits = organisationUnits;
    }

    public Integer getPage()
    {
        return page;
    }

    public void setPage( Integer page )
    {
        this.page = page;
    }

    public Integer getPageSize()
    {
        return pageSize;
    }

    public void setPageSize( Integer pageSize )
    {
        this.pageSize = pageSize;
    }
}
