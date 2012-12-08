package org.hisp.dhis.web.webapi.v1.utils;

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

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.web.webapi.v1.domain.Facility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Component
public class ToOrganisationUnitConverter implements Converter<Facility, OrganisationUnit>
{
    @Autowired
    @Qualifier( "org.hisp.dhis.organisationunit.OrganisationUnitService" )
    private OrganisationUnitService organisationUnitService;

    @Autowired
    @Qualifier( "org.hisp.dhis.dataset.DataSetService" )
    private DataSetService dataSetService;

    @Override
    public OrganisationUnit convert( Facility facility )
    {
        OrganisationUnit organisationUnit = new OrganisationUnit();

        organisationUnit.setUid( facility.getId() );
        organisationUnit.setName( facility.getName() );

        if ( facility.getName().length() > 49 )
        {
            organisationUnit.setShortName( facility.getName().substring( 0, 49 ) );
        }
        else
        {
            organisationUnit.setShortName( facility.getName() );
        }

        organisationUnit.setActive( facility.getActive() );
        organisationUnit.setParent( organisationUnitService.getOrganisationUnit( (String) facility.getProperties().get( "parent" ) ) );

        Collection<String> dataSets = (Collection<String>) facility.getProperties().get( "dataSets" );

        if ( dataSets != null )
        {
            for ( String uid : dataSets )
            {
                DataSet dataSet = dataSetService.getDataSet( uid );
                organisationUnit.getDataSets().add( dataSet );
            }
        }

        organisationUnit.setFeatureType( OrganisationUnit.FEATURETYPE_POINT );

        GeoUtils.Coordinates coordinates = GeoUtils.parseCoordinates(
            facility.getCoordinates().toString(), GeoUtils.CoordinateOrder.COORDINATE_LATLNG, GeoUtils.CoordinateOrder.COORDINATE_LNGLAT );

        organisationUnit.setCoordinates( String.format( "[%f, %f]", coordinates.lng, coordinates.lat ) );

        return organisationUnit;
    }
}
