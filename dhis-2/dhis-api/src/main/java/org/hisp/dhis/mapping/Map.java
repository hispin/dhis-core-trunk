package org.hisp.dhis.mapping;

/*
 * Copyright (c) 2004-2007, University of Oslo
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

import java.util.Set;

import org.hisp.dhis.chart.Chart;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;

/**
 * @author Jan Henrik Overland
 * @version $Id$
 */
public class Map
{
    private int id;

    private String mapLayerPath;
    
    private OrganisationUnit organisationUnit;

    private OrganisationUnitLevel organisationUnitLevel;

    private String uniqueColumn;

    private String nameColumn;
    
    private String longitude;
    
    private String latitude;
    
    private int zoom;
    
    private Set<String> staticMapLayerPaths;
    
    public Map()
    {
    }

    public Map( String mapLayerPath, OrganisationUnit organisationUnit, OrganisationUnitLevel organisationUnitLevel, 
        String uniqueColumn, String nameColumn, String longitude, String latitude, int zoom, Set<String> staticMapLayerPaths )
    {
        this.mapLayerPath = mapLayerPath;
        this.organisationUnit = organisationUnit;
        this.organisationUnitLevel = organisationUnitLevel;
        this.uniqueColumn = uniqueColumn;
        this.nameColumn = nameColumn;
        this.longitude = longitude;
        this.latitude = latitude;
        this.zoom = zoom;
        this.staticMapLayerPaths = staticMapLayerPaths;
    }
    
    // -------------------------------------------------------------------------
    // hashCode, equals and toString
    // -------------------------------------------------------------------------
    
    @Override
    public int hashCode()
    {
        return mapLayerPath.hashCode();
    }

    @Override
    public boolean equals( Object object )
    {
        if ( this == object )
        {
            return true;
        }
        
        if ( object == null )
        {
            return false;
        }
        
        if ( getClass() != object.getClass() )
        {
            return false;
        }
        
        Chart other = (Chart) object;
        
        return mapLayerPath.equals( other.getTitle() );
    }
    
    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }
    
    public String getMapLayerPath()
    {
        return mapLayerPath;
    }

    public void setMapLayerPath( String mapLayerPath )
    {
        this.mapLayerPath = mapLayerPath;
    }
    
    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    public void setOrganisationUnit( OrganisationUnit organisationUnit )
    {
        this.organisationUnit = organisationUnit;
    }

    public OrganisationUnitLevel getOrganisationUnitLevel()
    {
        return organisationUnitLevel;
    }

    public void setOrganisationUnitLevel( OrganisationUnitLevel organisationUnitLevel )
    {
        this.organisationUnitLevel = organisationUnitLevel;
    }

    public String getUniqueColumn()
    {
        return uniqueColumn;
    }

    public void setUniqueColumn( String uniqueColumn )
    {
        this.uniqueColumn = uniqueColumn;
    }

    public String getNameColumn()
    {
        return nameColumn;
    }

    public void setNameColumn( String nameColumn )
    {
        this.nameColumn = nameColumn;
    }
    
    public String getLongitude()
    {
        return longitude;
    }

    public void setLongitude( String longitude )
    {
        this.longitude = longitude;
    }

    public String getLatitude()
    {
        return latitude;
    }

    public void setLatitude( String latitude )
    {
        this.latitude = latitude;
    }

    public int getZoom()
    {
        return zoom;
    }

    public void setZoom( int zoom )
    {
        this.zoom = zoom;
    }
    
    public Set<String> getStaticMapLayerPaths()
    {
        return staticMapLayerPaths;
    }

    public void setStaticMapLayerPaths( Set<String> staticMapLayerPaths )
    {
        this.staticMapLayerPaths = staticMapLayerPaths;
    }
}
