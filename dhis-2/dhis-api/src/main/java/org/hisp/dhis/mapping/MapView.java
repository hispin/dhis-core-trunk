package org.hisp.dhis.mapping;

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

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorGroup;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;

/**
 * @author Jan Henrik Overland
 * @version $Id$
 */
public class MapView
{
    private int id;

    private String name;

    private String mapValueType;

    private IndicatorGroup indicatorGroup;

    private Indicator indicator;

    private DataElementGroup dataElementGroup;

    private DataElement dataElement;

    private String mapDateType;

    private PeriodType periodType;

    private Period period;

    private String startDate;

    private String endDate;

    private OrganisationUnit parentOrganisationUnit;

    private OrganisationUnitLevel organisationUnitLevel;

    private String mapLegendType;

    private Integer method;

    private Integer classes;

    private String bounds;

    private String colorLow;

    private String colorHigh;

    private MapLegendSet mapLegendSet;

    private String longitude;

    private String latitude;

    private Integer zoom;

    public MapView()
    {
    }

    public MapView( String name, String mapValueType, IndicatorGroup indicatorGroup, Indicator indicator,
        DataElementGroup dataElementGroup, DataElement dataElement, String mapDateType, PeriodType periodType,
        Period period, String startDate, String endDate, OrganisationUnit parentOrganisationUnit,
        OrganisationUnitLevel organisationUnitLevel, String mapLegendType, Integer method, Integer classes, String bounds,
        String colorLow, String colorHigh, MapLegendSet mapLegendSet, String longitude, String latitude, int zoom )
    {
        this.name = name;
        this.mapValueType = mapValueType;
        this.indicatorGroup = indicatorGroup;
        this.indicator = indicator;
        this.dataElementGroup = dataElementGroup;
        this.dataElement = dataElement;
        this.mapDateType = mapDateType;
        this.periodType = periodType;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.parentOrganisationUnit = parentOrganisationUnit;
        this.organisationUnitLevel = organisationUnitLevel;
        this.mapLegendType = mapLegendType;
        this.method = method;
        this.classes = classes;
        this.bounds = bounds;
        this.colorLow = colorLow;
        this.colorHigh = colorHigh;
        this.mapLegendSet = mapLegendSet;
        this.longitude = longitude;
        this.latitude = latitude;
        this.zoom = zoom;
    }

    // -------------------------------------------------------------------------
    // hashCode, equals and toString
    // -------------------------------------------------------------------------

    @Override
    public int hashCode()
    {
        return name.hashCode();
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

        final MapView other = (MapView) object;

        return name.equals( other.name );
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public String getMapDateTypeNullSafe()
    {
        return mapDateType != null ? mapDateType : MappingService.MAP_DATE_TYPE_FIXED;
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

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getMapValueType()
    {
        return mapValueType;
    }

    public void setMapValueType( String mapValueType )
    {
        this.mapValueType = mapValueType;
    }

    public IndicatorGroup getIndicatorGroup()
    {
        return indicatorGroup;
    }

    public void setIndicatorGroup( IndicatorGroup indicatorGroup )
    {
        this.indicatorGroup = indicatorGroup;
    }

    public Indicator getIndicator()
    {
        return indicator;
    }

    public void setIndicator( Indicator indicator )
    {
        this.indicator = indicator;
    }

    public DataElementGroup getDataElementGroup()
    {
        return dataElementGroup;
    }

    public void setDataElementGroup( DataElementGroup dataElementGroup )
    {
        this.dataElementGroup = dataElementGroup;
    }

    public DataElement getDataElement()
    {
        return dataElement;
    }

    public void setDataElement( DataElement dataElement )
    {
        this.dataElement = dataElement;
    }

    public String getMapDateType()
    {
        return mapDateType;
    }

    public void setMapDateType( String mapDateType )
    {
        this.mapDateType = mapDateType;
    }

    public PeriodType getPeriodType()
    {
        return periodType;
    }

    public void setPeriodType( PeriodType periodType )
    {
        this.periodType = periodType;
    }

    public Period getPeriod()
    {
        return period;
    }

    public void setPeriod( Period period )
    {
        this.period = period;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate( String startDate )
    {
        this.startDate = startDate;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public void setEndDate( String endDate )
    {
        this.endDate = endDate;
    }

    public OrganisationUnit getParentOrganisationUnit()
    {
        return parentOrganisationUnit;
    }

    public void setParentOrganisationUnit( OrganisationUnit parentOrganisationUnit )
    {
        this.parentOrganisationUnit = parentOrganisationUnit;
    }

    public OrganisationUnitLevel getOrganisationUnitLevel()
    {
        return organisationUnitLevel;
    }

    public void setOrganisationUnitLevel( OrganisationUnitLevel organisationUnitLevel )
    {
        this.organisationUnitLevel = organisationUnitLevel;
    }

    public String getMapLegendType()
    {
        return mapLegendType;
    }

    public void setMapLegendType( String mapLegendType )
    {
        this.mapLegendType = mapLegendType;
    }

    public Integer getMethod()
    {
        return method;
    }

    public void setMethod( Integer method )
    {
        this.method = method;
    }

    public Integer getClasses()
    {
        return classes;
    }

    public void setClasses( Integer classes )
    {
        this.classes = classes;
    }

    public String getBounds()
    {
        return bounds;
    }

    public void setBounds( String bounds )
    {
        this.bounds = bounds;
    }

    public String getColorLow()
    {
        return colorLow;
    }

    public void setColorLow( String colorLow )
    {
        this.colorLow = colorLow;
    }

    public String getColorHigh()
    {
        return colorHigh;
    }

    public void setColorHigh( String colorHigh )
    {
        this.colorHigh = colorHigh;
    }

    public MapLegendSet getMapLegendSet()
    {
        return mapLegendSet;
    }

    public void setMapLegendSet( MapLegendSet mapLegendSet )
    {
        this.mapLegendSet = mapLegendSet;
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

    public Integer getZoom()
    {
        return zoom;
    }

    public void setZoom( Integer zoom )
    {
        this.zoom = zoom;
    }
}
