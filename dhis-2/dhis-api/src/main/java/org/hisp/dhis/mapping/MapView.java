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

import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorGroup;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;

/**
 * @author Jan Henrik Overland
 * @version $Id$
 */
public class MapView
{
    public static final String MAP_SOURCE_TYPE_DATABASE = "database";
    public static final String MAP_SOURCE_TYPE_GEOJSON = "geojson";
    public static final String MAP_SOURCE_TYPE_SHAPEFILE = "shapefile";

    private int id;

    private String name;
    
    private IndicatorGroup indicatorGroup;
    
    private Indicator indicator;
    
    private PeriodType periodType;
    
    private Period period;
    
    private String mapSourceType;
    
    private String mapSource;
    
    private int method;
    
    private int classes;
    
    private String colorLow;
    
    private String colorHigh;
    
    public MapView()
    {
    }

    public MapView( String name, IndicatorGroup indicatorGroup, Indicator indicator, PeriodType periodType, Period period, String mapSourceType, String mapSource,
        int method, int classes, String colorLow, String colorHigh )
    {
        this.name = name;
        this.indicatorGroup = indicatorGroup;
        this.indicator = indicator;
        this.periodType = periodType;
        this.period = period;
        this.mapSourceType = mapSourceType;
        this.mapSource = mapSource;
        this.method = method;
        this.classes = classes;
        this.colorLow = colorLow;
        this.colorHigh = colorHigh;
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

    public String getMapSourceType()
    {
        return mapSourceType;
    }

    public void setMapSourceType( String mapSourceType )
    {
        this.mapSourceType = mapSourceType;
    }
    
    public String getMapSource()
    {
        return mapSource;
    }

    public void setMapSource( String mapSource )
    {
        this.mapSource = mapSource;
    }

    public int getMethod()
    {
        return method;
    }

    public void setMethod( int method )
    {
        this.method = method;
    }

    public int getClasses()
    {
        return classes;
    }

    public void setClasses( int classes )
    {
        this.classes = classes;
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
}