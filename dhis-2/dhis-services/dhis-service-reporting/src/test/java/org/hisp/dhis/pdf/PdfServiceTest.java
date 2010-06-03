package org.hisp.dhis.pdf;

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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.indicator.IndicatorType;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.system.util.StreamUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class PdfServiceTest
    extends DhisSpringTest
{
    private PdfService pdfService;

    private DataElementService dataElementService;

    private IndicatorService indicatorService;

    private OrganisationUnitService organisationUnitService;

    // -------------------------------------------------------------------------
    // I18n
    // -------------------------------------------------------------------------

    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    // -------------------------------------------------------------------------
    // Fixture
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest()
    {
        pdfService = (PdfService) getBean( PdfService.ID );

        dataElementService = (DataElementService) getBean( DataElementService.ID );

        indicatorService = (IndicatorService) getBean( IndicatorService.ID );

        organisationUnitService = (OrganisationUnitService) getBean( OrganisationUnitService.ID );

        dataElementService.addDataElement( createDataElement( 'A' ) );
        dataElementService.addDataElement( createDataElement( 'B' ) );
        dataElementService.addDataElement( createDataElement( 'C' ) );

        IndicatorType indicatorType = createIndicatorType( 'A' );
        indicatorService.addIndicatorType( indicatorType );

        indicatorService.addIndicator( createIndicator( 'A', indicatorType ) );
        indicatorService.addIndicator( createIndicator( 'B', indicatorType ) );
        indicatorService.addIndicator( createIndicator( 'C', indicatorType ) );

        organisationUnitService.addOrganisationUnit( createOrganisationUnit( 'A' ) );
        organisationUnitService.addOrganisationUnit( createOrganisationUnit( 'B' ) );
        organisationUnitService.addOrganisationUnit( createOrganisationUnit( 'C' ) );
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @Ignore
    public void testWriteAllDataElements()
        throws Exception
    {
        OutputStream outputStream = new BufferedOutputStream( new FileOutputStream( "dataElementsTest.pdf" ) );

        pdfService.writeAllDataElements( outputStream, i18n );

        StreamUtils.closeOutputStream( outputStream );
    }

    @Test
    @Ignore
    public void testWriteAllIndicators()
        throws Exception
    {
        OutputStream outputStreamB = new BufferedOutputStream( new FileOutputStream( "indicatorsTest.pdf" ) );

        pdfService.writeAllIndicators( outputStreamB, i18n );

        StreamUtils.closeOutputStream( outputStreamB );
    }

    @Test
    @Ignore
    public void testWriteAllOrganisationUnits()
        throws Exception
    {
        OutputStream outputStream = new BufferedOutputStream( new FileOutputStream( "organisationUnitsTest.pdf" ) );

        pdfService.writeAllOrganisationUnits( outputStream, i18n );

        StreamUtils.closeOutputStream( outputStream );
    }
}
