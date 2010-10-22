package org.hisp.dhis.dataset;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.dataentryform.DataEntryForm;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.source.Source;

/**
 * This class is used for defining the standardized DataSets. A DataSet consists
 * of a collection of DataElements.
 * 
 * @author Kristian Nordal
 * @version $Id: DataSet.java 6255 2008-11-10 16:01:24Z larshelg $
 */
public class DataSet
    extends IdentifiableObject
{
    /**
     * The PeriodType indicating the frequency that this DataSet should be used
     */
    private PeriodType periodType;

    /**
     * All DataElements associated with this DataSet.
     */
    private Collection<DataElement> dataElements = new HashSet<DataElement>();
    
    /**
     * The DataElementOperands for which data must be entered in order for the DataSet to
     * be considered as complete.
     */
    private Set<DataElementOperand> compulsoryDataElementOperands = new HashSet<DataElementOperand>();
    
    /**
     * All Sources that register data with this DataSet.
     */
    private Set<Source> sources = new HashSet<Source>();
    
    /**
    * Property indicating whether the DataSet is locked for data entry.
    */
    private Boolean locked = false;
        
    /**
     * The Sections associated with the DataSet.
     */
    private Set<Section> sections = new HashSet<Section>();

    /**
     * Indicating position in the custom sort order.
     */
    private Integer sortOrder;
    
    /**
     * Property indicating if the dataset could be collected using mobile data entry.
     */
    private Boolean mobile = false;

    /**
     * Indicating custom data entry form.
     */
    private DataEntryForm dataEntryForm;

    
    // -------------------------------------------------------------------------
    // Contructors
    // -------------------------------------------------------------------------

    public DataSet()
    {
    }
    
    public DataSet( String name )
    {
        this.name = name;
    }
    
    public DataSet( String name, PeriodType periodType )
    {
        this.name = name;
        this.periodType = periodType;
    }
    
    public DataSet( String name, String shortName, PeriodType periodType )
    {
        this.name = name;
        this.shortName = shortName;
        this.periodType = periodType;
    }
    
    public DataSet( String name, String shortName, String code, PeriodType periodType )
    {
        this.name = name;
        this.shortName = shortName;
        this.code = code;
        this.periodType = periodType;
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

        if ( !(o instanceof DataSet) )
        {
            return false;
        }

        final DataSet other = (DataSet) o;

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
    
    public PeriodType getPeriodType()
    {
        return periodType;
    }

    public DataEntryForm getDataEntryForm()
    {
        return dataEntryForm;
    }

    public void setDataEntryForm( DataEntryForm dataEntryForm )
    {
        this.dataEntryForm = dataEntryForm;
    }

    public void setPeriodType( PeriodType periodType )
    {
        this.periodType = periodType;
    }

    public Collection<DataElement> getDataElements()
    {
        return dataElements;
    }

    public void setDataElements( Collection<DataElement> dataElements )
    {
        this.dataElements = dataElements;
    }

    public Set<DataElementOperand> getCompulsoryDataElementOperands()
    {
        return compulsoryDataElementOperands;
    }

    public void setCompulsoryDataElementOperands( Set<DataElementOperand> compulsoryDataElementOperands )
    {
        this.compulsoryDataElementOperands = compulsoryDataElementOperands;
    }

    public Set<Source> getSources()
    {
        return sources;
    }

    public void setSources( Set<Source> sources )
    {
        this.sources = sources;
    }

    public Boolean getLocked()
    {
        return locked;
    }

    public void setLocked( Boolean locked )
    {
        this.locked = locked;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder( Integer sortOrder )
    {
        this.sortOrder = sortOrder;
    }

    /**
     * @param sections the sections to set
     */
    public void setSections( Set<Section> sections )
    {
        this.sections = sections;
    }

    /**
     * @return the sections
     */
    public Set<Section> getSections()
    {
        return sections;
    }

	public void setMobile(Boolean mobile) 
	{
		this.mobile = mobile;
	}
	
	public Boolean getMobile() 
	{
		return mobile;
	}
}
