package org.hisp.dhis.eventchart;

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
import java.util.Date;
import java.util.List;

import org.hisp.dhis.chart.BaseChart;
import org.hisp.dhis.common.AnalyticsType;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.DimensionalObject;
import org.hisp.dhis.common.DimensionalObjectUtils;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.NameableObject;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.DimensionalView;
import org.hisp.dhis.common.view.ExportView;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.user.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author Jan Henrik Overland
 */
public class EventChart
    extends BaseChart
{
    public static final String COUNT_TYPE_EVENTS = "events";
    public static final String COUNT_TYPE_TRACKED_ENTITY_INSTANCES = "tracked_entity_instances";

    private Program program;

    private ProgramStage programStage;

    private Date startDate;

    private Date endDate;

    private List<String> columnDimensions = new ArrayList<String>();

    private List<String> rowDimensions = new ArrayList<String>();

    private String countType;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public EventChart()
    {
    }

    public EventChart( String name )
    {
        this.name = name;
    }

    // -------------------------------------------------------------------------
    // AnalyticalObject
    // -------------------------------------------------------------------------

    @Override
    public void init( User user, Date date, OrganisationUnit organisationUnit,
        List<OrganisationUnit> organisationUnitsAtLevel, List<OrganisationUnit> organisationUnitsInGroups,
        I18nFormat format )
    {
        this.user = user;
        this.format = format;
    }

    @Override
    public void populateAnalyticalProperties()
    {
        for ( String column : columnDimensions )
        {
            columns.addAll( getDimensionalObjectList( column ) );
        }

        for ( String row : rowDimensions )
        {
            rows.addAll( getDimensionalObjectList( row ) );
        }

        for ( String filter : filterDimensions )
        {
            filters.addAll( getDimensionalObjectList( filter ) );
        }
    }

    public List<NameableObject> series()
    {
        String series = columnDimensions.get( 0 );
        
        DimensionalObject object = getDimensionalObject( series, relativePeriodDate, user, true,
            organisationUnitsAtLevel, organisationUnitsInGroups, format );

        DimensionalObjectUtils.setDimensionItemsForFilters( object, dataItemGrid );
        
        return object != null ? object.getItems() : null;
    }

    public List<NameableObject> category()
    {
        String category = rowDimensions.get( 0 );
        
        DimensionalObject object = getDimensionalObject( category, relativePeriodDate, user, true,
            organisationUnitsAtLevel, organisationUnitsInGroups, format );

        DimensionalObjectUtils.setDimensionItemsForFilters( object, dataItemGrid );
        
        return object != null ? object.getItems() : null;
    }
    
    public AnalyticsType getAnalyticsType()
    {
        return AnalyticsType.EVENT;
    }

    // -------------------------------------------------------------------------
    // Getters and setters properties
    // -------------------------------------------------------------------------

    @JsonProperty
    @JsonSerialize( as = BaseIdentifiableObject.class )
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Program getProgram()
    {
        return program;
    }

    public void setProgram( Program program )
    {
        this.program = program;
    }

    @JsonProperty
    @JsonSerialize( as = BaseIdentifiableObject.class )
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public ProgramStage getProgramStage()
    {
        return programStage;
    }

    public void setProgramStage( ProgramStage programStage )
    {
        this.programStage = programStage;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate( Date startDate )
    {
        this.startDate = startDate;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate( Date endDate )
    {
        this.endDate = endDate;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "columnDimensions", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "column", namespace = DxfNamespaces.DXF_2_0 )
    public List<String> getColumnDimensions()
    {
        return columnDimensions;
    }

    public void setColumnDimensions( List<String> columnDimensions )
    {
        this.columnDimensions = columnDimensions;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "rowDimensions", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "row", namespace = DxfNamespaces.DXF_2_0 )
    public List<String> getRowDimensions()
    {
        return rowDimensions;
    }

    public void setRowDimensions( List<String> rowDimensions )
    {
        this.rowDimensions = rowDimensions;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getCountType()
    {
        return countType;
    }

    public void setCountType( String countType )
    {
        this.countType = countType;
    }

    // -------------------------------------------------------------------------
    // Merge with
    // -------------------------------------------------------------------------

    @Override
    public void mergeWith( IdentifiableObject other )
    {
        super.mergeWith( other );

        if ( other.getClass().isInstance( this ) )
        {
            EventChart eventChart = (EventChart) other;

            program = eventChart.getProgram();
            programStage = eventChart.getProgramStage();
            startDate = eventChart.getStartDate();
            endDate = eventChart.getEndDate();

            columnDimensions.clear();
            columnDimensions.addAll( eventChart.getColumnDimensions() );

            rowDimensions.clear();
            rowDimensions.addAll( eventChart.getRowDimensions() );

            countType = eventChart.getCountType();
        }
    }
}
