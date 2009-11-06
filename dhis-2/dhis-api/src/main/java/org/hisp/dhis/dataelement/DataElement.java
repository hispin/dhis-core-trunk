package org.hisp.dhis.dataelement;

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

// import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.datadictionary.ExtendedDataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dimension.Dimension;
import org.hisp.dhis.dimension.DimensionOption;
import org.hisp.dhis.dimension.DimensionOptionElement;
import org.hisp.dhis.dimension.DimensionSet;
import org.hisp.dhis.period.PeriodType;

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
    implements DimensionSet, DimensionOption, DimensionOptionElement
{
    public static final String VALUE_TYPE_STRING = "string";

    public static final String VALUE_TYPE_INT = "int";

    public static final String VALUE_TYPE_BOOL = "bool";

    public static final String VALUE_TYPE_DATE = "date";

    public static final String TYPE_AGGREGATE = "aggregate";

    public static final String TYPE_PATIENT = "patient";

    public static final String AGGREGATION_OPERATOR_SUM = "sum";

    public static final String AGGREGATION_OPERATOR_AVERAGE = "average";

    public static final String AGGREGATION_OPERATOR_COUNT = "count";

    /**
     * If this DataElement is active or not (enabled or disabled).
     */
    private boolean active;

    /**
     * The type of this DataElement; e.g. DataElement.TYPE_AGGREGATE or
     * DataElement.TYPE_PATIENT.
     */
    private String type;

    /**
     * The value type of this DataElement; e.g. DataElement.VALUE_TYPE_INT or
     * DataElement.VALUE_TYPE_BOOL.
     */
    private String valueType;

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
     * The lower organisation unit levels for aggregation.
     */
    private List<Integer> aggregationLevels = new ArrayList<Integer>();

    /**
     * A Set of DataElementGroupSets.
     */
    private List<DataElementGroupSet> groupSets = new ArrayList<DataElementGroupSet>();

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

    public static Dimension DIMENSION = new DataElementDimension();

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

    public List<? extends Dimension> getDimensions()
    {
        return groupSets;
    }

    public List<? extends DimensionOptionElement> getDimensionOptionElements()
    {
        List<DimensionOptionElement> dimensionOptionElements = new ArrayList<DimensionOptionElement>();
        
        for ( Dimension dimension : getDimensions() )
        {
            for ( DimensionOption dimensionOption : dimension.getDimensionOptions() )
            {
                dimensionOptionElements.addAll( dimensionOption.getDimensionOptionElements() );
            }
        }
        
        return dimensionOptionElements;
    }
    
    public List<? extends DimensionOption> getDimensionOptions()
    {
        return new ArrayList<DimensionOption>( groups );
    }

    public Dimension getDimension()
    {
        return DIMENSION;
    }
    
    public boolean isDimensionSet()
    {
        return groupSets != null && groupSets.size() > 0;
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
     * Returns the PeriodType of the DataElement, based on the PeriodType of the
     * DataSet which the DataElement is registered for.
     */
    public PeriodType getPeriodType()
    {
        return dataSets != null && dataSets.size() > 0 ? dataSets.iterator().next().getPeriodType() : null;
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

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getValueType()
    {
        return valueType;
    }

    public void setValueType( String valueType )
    {
        this.valueType = valueType;
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

    public List<Integer> getAggregationLevels()
    {
        return aggregationLevels;
    }

    public void setAggregationLevels( List<Integer> aggregationLevels )
    {
        this.aggregationLevels = aggregationLevels;
    }

    public List<DataElementGroupSet> getGroupSets()
    {
        return groupSets;
    }

    public void setGroupSets( List<DataElementGroupSet> groupSets )
    {
        this.groupSets = groupSets;
    }
}
