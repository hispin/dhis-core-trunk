package org.hisp.dhis.dataelement;

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

// import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.datadictionary.ExtendedDataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dimension.Dimension;
import org.hisp.dhis.dimension.DimensionOption;
import org.hisp.dhis.dimension.DimensionOptionElement;
import org.hisp.dhis.dimension.DimensionType;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.TwoYearlyPeriodType;

/**
 * A DataElement is a definition (meta-information about) of the entities that
 * are captured in the system. An example from public health care is a
 * DataElement representing the number BCG doses; A DataElement with "BCG dose"
 * as name, with type DataElement.TYPE_INT. DataElements can be structured
 * hierarchically, one DataElement can have a parent and a collection of
 * children. The sum of the children represent the same entity as the parent.
 * Hiearchies of DataElements are used to give more fine- or course-grained
 * representations of the entities.
 * 
 * DataElement acts as a DimensionSet in the dynamic dimensional model, and as a
 * DimensionOption in the static DataElement dimension.
 * 
 * @author Kristian Nordal
 * @version $Id: DataElement.java 5540 2008-08-19 10:47:07Z larshelg $
 */
public class DataElement
    extends IdentifiableObject
    implements DimensionOption, DimensionOptionElement
{
    public static final String VALUE_TYPE_STRING = "string";

    public static final String VALUE_TYPE_INT = "int";

    public static final String VALUE_TYPE_BOOL = "bool";

    public static final String VALUE_TYPE_DATE = "date";

    public static final String DOMAIN_TYPE_AGGREGATE = "aggregate";

    public static final String DOMAIN_TYPE_PATIENT = "patient";

    public static final String AGGREGATION_OPERATOR_SUM = "sum";

    public static final String AGGREGATION_OPERATOR_AVERAGE = "average";

    public static final String AGGREGATION_OPERATOR_COUNT = "count";

    /**
     * If this DataElement is active or not (enabled or disabled).
     */
    private boolean active;

    /**
     * The domain of this DataElement; e.g. DataElement.DOMAIN_TYPE_AGGREGATE or
     * DataElement.DOMAIN_TYPE_PATIENT.
     */
    private String domainType;

    /**
     * The value type of this DataElement; e.g. DataElement.VALUE_TYPE_INT or
     * DataElement.VALUE_TYPE_BOOL.
     */
    private String type;

    /**
     * The aggregation operator of this DataElement; e.g. DataElement.SUM og
     * DataElement.AVERAGE.
     */
    private String aggregationOperator;

    /**
     * A Collection of children DataElements.
     */
    private Set<DataElement> children = new HashSet<DataElement>();

    /**
     * The parent DataElement for this DataElement.
     */
    private DataElement parent;

    /**
     * Extended information about the DataElement.
     */
    private ExtendedDataElement extended;

    /**
     * A combination of categories to capture data.
     */
    private DataElementCategoryCombo categoryCombo;

    /**
     * Defines a custom sort order.
     */
    private Integer sortOrder;

    /**
     * URL for lookup of additional information on the web.
     */
    private String url;

    /**
     * The date this data element was last updated.
     */
    private Date lastUpdated;

    /**
     * The data element groups which this
     */
    private Set<DataElementGroup> groups = new HashSet<DataElementGroup>();

    /**
     * The data sets which this data element is a member of.
     */
    private Set<DataSet> dataSets = new HashSet<DataSet>();

    /**
     * A Set of DataElementGroupSets.
     */
    private List<DataElementGroupSet> groupSets = new ArrayList<DataElementGroupSet>();

    /**
     * The lower organisation unit levels for aggregation.
     */
    private List<Integer> aggregationLevels = new ArrayList<Integer>();

    /**
     * There is no point of saving 0's for this data element default is false
     * ,we don't want to store 0's if not set to true
     */
    private Boolean zeroIsSignificant;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public DataElement()
    {
    }

    public DataElement( String name )
    {
        this.name = name;
    }

    // -------------------------------------------------------------------------
    // Dimension
    // -------------------------------------------------------------------------

    public static final Dimension DIMENSION = new DataElementDimension();

    public static class DataElementDimension
        extends Dimension
    {
        private static final String NAME = "DataElement";

        public String getName()
        {
            return NAME;
        }

        public List<? extends DimensionOption> getDimensionOptions()
        {
            return null;
        }

        @Override
        public boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }

            if ( o == null )
            {
                return false;
            }

            if ( !(o instanceof DataElementDimension) )
            {
                return false;
            }

            final DataElementDimension other = (DataElementDimension) o;

            return NAME.equals( other.getName() );
        }

        @Override
        public int hashCode()
        {
            return NAME.hashCode();
        }

        @Override
        public String toString()
        {
            return "[" + NAME + "]";
        }
    }

    public DimensionType getDimensionType()
    {
        return null; // DataElement is DimensionOption for the static DataElement dimension
    }

    public Set<? extends DimensionOptionElement> getDimensionOptionElements()
    {
        return null; // DataElement is DimensionOption for the static DataElement dimension
    }

    public List<? extends DimensionOption> getDimensionOptions()
    {
        return new ArrayList<DimensionOption>( groups );
    }

    public Dimension getDimension()
    {
        return DIMENSION;
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
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null )
        {
            return false;
        }

        if ( !(o instanceof DataElement) )
        {
            return false;
        }

        final DataElement other = (DataElement) o;

        return name.equals( other.getName() );
    }

    @Override
    public String toString()
    {
        return "[" + name + "]";
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    /**
     * Null-safe check.
     */
    public boolean isZeroIsSignificant()
    {
        return zeroIsSignificant != null && zeroIsSignificant;
    }
    
    /**
     * Returns the PeriodType of the DataElement, based on the PeriodType of the
     * DataSet which the DataElement is registered for.
     */
    public PeriodType getPeriodType()
    {
        return dataSets != null && dataSets.size() > 0 ? dataSets.iterator().next().getPeriodType() : null;
    }
    
    /**
     * Returns the frequency order for the PeriodType of this DataElement. If no
     * PeriodType exists, 0 is returned.
     */
    public int getFrequencyOrder()
    {
        PeriodType periodType = getPeriodType();
        
        return periodType != null ? periodType.getFrequencyOrder() : TwoYearlyPeriodType.FREQUENCY_ORDER;
    }

    /**
     * Tests whether a PeriodType can be defined for the DataElement, which
     * requires that the DataElement is registered for DataSets with the same
     * PeriodType.
     */
    public boolean periodTypeIsValid()
    {
        PeriodType periodType = null;

        for ( DataSet dataSet : dataSets )
        {
            if ( periodType != null && !periodType.equals( dataSet.getPeriodType() ) )
            {
                return false;
            }

            periodType = dataSet.getPeriodType();
        }

        return true;
    }

    /**
     * Tests whether more than one aggregation level exists for the DataElement.
     */
    public boolean hasAggregationLevels()
    {
        return aggregationLevels != null && aggregationLevels.size() > 0;
    }

    /**
     * Tests whether the DataElement is associated with a
     * DataELementCategoryCombo with more than one DataElementCategory, or any
     * DataElementCategory with more than one DataElementCategoryOption.
     */
    public boolean isMultiDimensional()
    {
        if ( categoryCombo != null )
        {
            if ( categoryCombo.getCategories().size() > 1 )
            {
                return true;
            }

            for ( DataElementCategory category : categoryCombo.getCategories() )
            {
                if ( category.getCategoryOptions().size() > 1 )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public String getDomainTypeNullSafe()
    {
        return domainType != null ? domainType : DOMAIN_TYPE_AGGREGATE;
    }

    public Set<DataElement> getDataElements()
    {
        Set<DataElement> dataElements = new HashSet<DataElement>();

        for ( DataElementGroupSet groupSet : groupSets )
        {
            for ( DataElementGroup group : groupSet.getMembers() )
            {
                dataElements.addAll( group.getMembers() );
            }
        }

        return dataElements;
    }
    
    public String toJSON()
    {   
        StringBuffer result = new StringBuffer();        
        
        result.append( "{" );
        result.append( "\"id\":\"" + this.id + "\"" );
        result.append( ",\"name\":\"" + StringEscapeUtils.escapeJavaScript( this.name ) + "\"" );
        result.append( ",\"shortName\":\"" + StringEscapeUtils.escapeJavaScript( this.shortName ) + "\"" );
        result.append( ",\"type\":\"" + StringEscapeUtils.escapeJavaScript( this.type ) + "\"" );
        result.append( "}" );        
        return result.toString();
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public boolean isActive()
    {
        return active;
    }

    public void setActive( boolean active )
    {
        this.active = active;
    }

    public String getDomainType()
    {
        return domainType;
    }

    public void setDomainType( String domainType )
    {
        this.domainType = domainType;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getAggregationOperator()
    {
        return aggregationOperator;
    }

    public void setAggregationOperator( String aggregationOperator )
    {
        this.aggregationOperator = aggregationOperator;
    }

    public Set<DataElement> getChildren()
    {
        return children;
    }

    public void setChildren( Set<DataElement> children )
    {
        this.children = children;
    }

    public DataElement getParent()
    {
        return parent;
    }

    public void setParent( DataElement parent )
    {
        this.parent = parent;
    }

    public ExtendedDataElement getExtended()
    {
        return extended;
    }

    public void setExtended( ExtendedDataElement extended )
    {
        this.extended = extended;
    }

    public DataElementCategoryCombo getCategoryCombo()
    {
        return categoryCombo;
    }

    public void setCategoryCombo( DataElementCategoryCombo categoryCombo )
    {
        this.categoryCombo = categoryCombo;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder( Integer sortOrder )
    {
        this.sortOrder = sortOrder;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public Date getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated( Date lastUpdated )
    {
        this.lastUpdated = lastUpdated;
    }

    public Set<DataElementGroup> getGroups()
    {
        return groups;
    }

    public void setGroups( Set<DataElementGroup> groups )
    {
        this.groups = groups;
    }

    public Set<DataSet> getDataSets()
    {
        return dataSets;
    }

    public void setDataSets( Set<DataSet> dataSets )
    {
        this.dataSets = dataSets;
    }

    public List<DataElementGroupSet> getGroupSets()
    {
        return groupSets;
    }

    public void setGroupSets( List<DataElementGroupSet> groupSets )
    {
        this.groupSets = groupSets;
    }

    public List<Integer> getAggregationLevels()
    {
        return aggregationLevels;
    }

    public void setAggregationLevels( List<Integer> aggregationLevels )
    {
        this.aggregationLevels = aggregationLevels;
    }

    public Boolean getZeroIsSignificant()
    {
        return zeroIsSignificant;
    }

    public void setZeroIsSignificant( Boolean zeroIsSignificant )
    {
        this.zeroIsSignificant = zeroIsSignificant;
    }
}
