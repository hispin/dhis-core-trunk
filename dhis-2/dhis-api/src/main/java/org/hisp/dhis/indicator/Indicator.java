package org.hisp.dhis.indicator;

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

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.common.Dimension;
import org.hisp.dhis.common.DimensionSet;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.datadictionary.ExtendedDataElement;

/**
 * @author Lars Helge Overland
 * @version $Id: Indicator.java 5540 2008-08-19 10:47:07Z larshelg $
 */
public class Indicator
    extends IdentifiableObject implements DimensionSet
{
    private Boolean annualized;

    private IndicatorType indicatorType;

    private String numerator;

    private String numeratorDescription;

    private String numeratorAggregationOperator;

    private String denominator;

    private String denominatorDescription;

    private String denominatorAggregationOperator;
    
    private ExtendedDataElement extended;
    
    private Integer sortOrder;

    private String url;
        
    private List<IndicatorGroupSet> groupSets = new ArrayList<IndicatorGroupSet>();
    
    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Indicator()
    {
    }

    public Indicator( String name, String alternativeName, String shortName, String code, String description, Boolean annualized, 
        IndicatorType indicatorType, String numerator, String numeratorDescription, String numeratorAggregationOperator, String denominator,
        String denominatorDescription, String denominatorAggregationOperator )
    {
        this.name = name;
        this.alternativeName = alternativeName;
        this.shortName = shortName;
        this.code = code;
        this.description = description;
        this.annualized = annualized;
        this.indicatorType = indicatorType;
        this.numerator = numerator;
        this.numeratorDescription = numeratorDescription;
        this.numeratorAggregationOperator = numeratorAggregationOperator;
        this.denominator = denominator;
        this.denominatorDescription = denominatorDescription;
        this.denominatorAggregationOperator = denominatorAggregationOperator;
    }

    // -------------------------------------------------------------------------
    // Dimension
    // -------------------------------------------------------------------------

    public List<? extends Dimension> getDimensions()
    {
        return groupSets;
    }
    
    // -------------------------------------------------------------------------
    // hashCode and equals
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

        if ( !(o instanceof Indicator) )
        {
            return false;
        }

        final Indicator other = (Indicator) o;

        return name.equals( other.getName() );
    }

    @Override
    public String toString()
    {
        return "[" + name + "]";
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public Boolean getAnnualized()
    {
        return annualized;
    }

    public void setAnnualized( Boolean annualized )
    {
        this.annualized = annualized;
    }

    public IndicatorType getIndicatorType()
    {
        return indicatorType;
    }

    public void setIndicatorType( IndicatorType indicatorType )
    {
        this.indicatorType = indicatorType;
    }

    public String getNumerator()
    {
        return numerator;
    }

    public void setNumerator( String numerator )
    {
        this.numerator = numerator;
    }

    public String getNumeratorDescription()
    {
        return numeratorDescription;
    }

    public void setNumeratorDescription( String numeratorDescription )
    {
        this.numeratorDescription = numeratorDescription;
    }

    public String getNumeratorAggregationOperator()
    {
        return numeratorAggregationOperator;
    }

    public void setNumeratorAggregationOperator( String numeratorAggregationOperator )
    {
        this.numeratorAggregationOperator = numeratorAggregationOperator;
    }

    public String getDenominator()
    {
        return denominator;
    }

    public void setDenominator( String denominator )
    {
        this.denominator = denominator;
    }

    public String getDenominatorDescription()
    {
        return denominatorDescription;
    }

    public void setDenominatorDescription( String denominatorDescription )
    {
        this.denominatorDescription = denominatorDescription;
    }

    public String getDenominatorAggregationOperator()
    {
        return denominatorAggregationOperator;
    }

    public void setDenominatorAggregationOperator( String denominatorAggregationOperator )
    {
        this.denominatorAggregationOperator = denominatorAggregationOperator;
    }

    public ExtendedDataElement getExtended()
    {
        return extended;
    }

    public void setExtended( ExtendedDataElement extended )
    {
        this.extended = extended;
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

    public List<IndicatorGroupSet> getGroupSets()
    {
        return groupSets;
    }

    public void setGroupSets( List<IndicatorGroupSet> groupSets )
    {
        this.groupSets = groupSets;
    }
}
