package org.hisp.dhis.schema;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.NameableObject;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@JacksonXmlRootElement( localName = "schema", namespace = DxfNamespaces.DXF_2_0 )
public class Schema implements Ordered
{
    /**
     * Class that is described in this schema.
     */
    private Class<?> klass;

    /**
     * Is this class a sub-class of IdentifiableObject
     *
     * @see org.hisp.dhis.common.IdentifiableObject
     */
    private boolean identifiableObject;

    /**
     * Is this class a sub-class of NameableObject
     *
     * @see org.hisp.dhis.common.NameableObject
     */
    private boolean nameableObject;

    /**
     * Singular name.
     */
    private String singular;

    /**
     * Plural name.
     */
    private String plural;

    /**
     * Is sharing supported for instances of this class.
     */
    private boolean shareable;

    /**
     * Points to Web-API endpoint (if exposed).
     */
    private String apiEndpoint;

    /**
     * Is this class considered metadata, this is mainly used for our metadata importer/exporter.
     */
    private boolean metadata;

    /**
     * List of authorities required for doing operations on this class.
     */
    private List<Authority> authorities = Lists.newArrayList();

    /**
     * List of all exposed properties on this class.
     */
    private List<Property> properties = Lists.newArrayList();

    /**
     * Used for sorting of schema list when doing metadata import/export.
     */
    private int order = Ordered.LOWEST_PRECEDENCE;

    public Schema( Class<?> klass, String singular, String plural )
    {
        this.klass = klass;
        this.identifiableObject = IdentifiableObject.class.isAssignableFrom( klass );
        this.nameableObject = NameableObject.class.isAssignableFrom( klass );
        this.singular = singular;
        this.plural = plural;
        this.metadata = true;
    }

    @JsonProperty
    @JacksonXmlProperty( isAttribute = true )
    public Class<?> getKlass()
    {
        return klass;
    }

    public void setKlass( Class<?> klass )
    {
        this.klass = klass;
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isIdentifiableObject()
    {
        return identifiableObject;
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isNameableObject()
    {
        return nameableObject;
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getSingular()
    {
        return singular;
    }

    public void setSingular( String singular )
    {
        this.singular = singular;
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getPlural()
    {
        return plural;
    }

    public void setPlural( String plural )
    {
        this.plural = plural;
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isShareable()
    {
        return shareable;
    }

    public void setShareable( boolean shareable )
    {
        this.shareable = shareable;
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getApiEndpoint()
    {
        return apiEndpoint;
    }

    public void setApiEndpoint( String apiEndpoint )
    {
        this.apiEndpoint = apiEndpoint;
    }

    public boolean haveEndpoint()
    {
        return getApiEndpoint() != null;
    }

    @JsonProperty
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isMetadata()
    {
        return metadata;
    }

    public void setMetadata( boolean metadata )
    {
        this.metadata = metadata;
    }

    @JsonProperty
    @JacksonXmlElementWrapper( localName = "authorities", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "authority", namespace = DxfNamespaces.DXF_2_0 )
    public List<Authority> getAuthorities()
    {
        return authorities;
    }

    public void setAuthorities( List<Authority> authorities )
    {
        this.authorities = authorities;
    }

    @JsonProperty
    @JacksonXmlElementWrapper( localName = "properties", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "property", namespace = DxfNamespaces.DXF_2_0 )
    public List<Property> getProperties()
    {
        return properties;
    }

    public void setProperties( List<Property> properties )
    {
        this.properties = properties;
    }

    private Map<AuthorityType, List<String>> authorityMap = Maps.newHashMap();

    public List<String> getAuthorityByType( AuthorityType type )
    {
        if ( !authorityMap.containsKey( type ) )
        {
            List<String> authorityList = Lists.newArrayList();

            for ( Authority authority : authorities )
            {
                if ( type.equals( authority.getType() ) )
                {
                    authorityList.addAll( authority.getAuthorities() );
                }
            }

            authorityMap.put( type, authorityList );
        }

        return authorityMap.get( type );
    }

    // TODO not exposed right now, should we?
    @Override
    public int getOrder()
    {
        return order;
    }

    public void setOrder( int order )
    {
        this.order = order;
    }

    @Override
    public String toString()
    {
        return "Schema{" +
            "klass=" + klass +
            ", identifiableObject=" + identifiableObject +
            ", nameableObject=" + nameableObject +
            ", singular='" + singular + '\'' +
            ", plural='" + plural + '\'' +
            ", shareable=" + shareable +
            ", authorities=" + authorities +
            ", properties=" + properties +
            '}';
    }
}
