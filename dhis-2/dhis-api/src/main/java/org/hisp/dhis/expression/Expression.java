package org.hisp.dhis.expression;

/*
 * Copyright (c) 2004-2012, University of Oslo
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.Dxf2Namespace;
import org.hisp.dhis.common.annotation.Scanned;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.ExportView;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * An Expression is the expression of e.g. a validation rule. It consist of a
 * String representation of the rule as well as references to the data elements
 * and category option combos included in the expression.
 * <p/>
 * The expression can contain numbers and mathematical operators and contain references
 * to data elements and category option combos on the form:
 * <p/>
 * i) [1.2] where 1 refers to the data element identifier and 2 refers to the
 * category option combo identifier.
 * <p/>
 * ii) [1] where 1 refers to the data element identifier, in this case the formula
 * represents the total value for all category option combos for that data element.
 *
 * @author Margrethe Store
 * @version $Id: Expression.java 5011 2008-04-24 20:41:28Z larshelg $
 */
@JacksonXmlRootElement( localName = "expression", namespace = Dxf2Namespace.NAMESPACE )
public class Expression
    implements Serializable
{
    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = -4868682510629094282L;

    public static final String SEPARATOR = ".";
    public static final String EXP_OPEN = "[";
    public static final String EXP_CLOSE = "]";
    public static final String PAR_OPEN = "(";
    public static final String PAR_CLOSE = ")";

    /**
     * The unique identifier for this Expression.
     */
    private int id;

    /**
     * The Expression.
     */
    private String expression;

    /**
     * A description of the Expression.
     */
    private String description;

    /**
     * A reference to the DataElements in the Expression.
     */
    @Scanned
    private Set<DataElement> dataElementsInExpression = new HashSet<DataElement>();

    /**
     * A reference to the optionCombos in the Expression.
     */
    @Scanned
    private Set<DataElementCategoryOptionCombo> optionCombosInExpression = new HashSet<DataElementCategoryOptionCombo>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default empty Expression
     */
    public Expression()
    {
    }

    /**
     * Constructor with all the parameters.
     *
     * @param expression               The expression as a String
     * @param description              A description of the Expression.
     * @param dataElementsInExpression A reference to the DataElements in the Expression.
     */
    public Expression( String expression, String description, Set<DataElement> dataElementsInExpression,
        Set<DataElementCategoryOptionCombo> optionCombosInExpression )
    {
        this.expression = expression;
        this.description = description;
        this.dataElementsInExpression = dataElementsInExpression;
        this.optionCombosInExpression = optionCombosInExpression;
    }

    // -------------------------------------------------------------------------
    // Equals and hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Expression that = (Expression) o;

        if ( id != that.id ) return false;
        if ( dataElementsInExpression != null ? !dataElementsInExpression.equals( that.dataElementsInExpression ) : that.dataElementsInExpression != null )
            return false;
        if ( description != null ? !description.equals( that.description ) : that.description != null ) return false;
        if ( expression != null ? !expression.equals( that.expression ) : that.expression != null ) return false;
        if ( optionCombosInExpression != null ? !optionCombosInExpression.equals( that.optionCombosInExpression ) : that.optionCombosInExpression != null )
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id;
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (dataElementsInExpression != null ? dataElementsInExpression.hashCode() : 0);
        result = 31 * result + (optionCombosInExpression != null ? optionCombosInExpression.hashCode() : 0);

        return result;
    }

    @Override
    public String toString()
    {
        return "Expression{" +
            "id=" + id +
            ", expression='" + expression + '\'' +
            ", description='" + description + '\'' +
            ", dataElementsInExpression=" + dataElementsInExpression.size() +
            ", optionCombosInExpression=" + optionCombosInExpression.size() +
            '}';
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

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty
    public String getExpression()
    {
        return expression;
    }

    public void setExpression( String expression )
    {
        this.expression = expression;
    }

    @JsonProperty( value = "dataElements" )
    @JsonSerialize( contentAs = BaseIdentifiableObject.class )
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "dataElements" )
    @JacksonXmlProperty( localName = "dataElement" )
    public Set<DataElement> getDataElementsInExpression()
    {
        return dataElementsInExpression;
    }

    public void setDataElementsInExpression( Set<DataElement> dataElementsInExpression )
    {
        this.dataElementsInExpression = dataElementsInExpression;
    }

    @JsonProperty( value = "categoryOptionCombos" )
    @JsonSerialize( contentAs = BaseIdentifiableObject.class )
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "categoryOptionCombos" )
    @JacksonXmlProperty( localName = "categoryOptionCombo" )
    public Set<DataElementCategoryOptionCombo> getOptionCombosInExpression()
    {
        return optionCombosInExpression;
    }

    public void setOptionCombosInExpression( Set<DataElementCategoryOptionCombo> optionCombosInExpression )
    {
        this.optionCombosInExpression = optionCombosInExpression;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty
    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }
}
