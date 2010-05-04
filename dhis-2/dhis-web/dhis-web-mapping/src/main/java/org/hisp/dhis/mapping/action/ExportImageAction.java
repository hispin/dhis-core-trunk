package org.hisp.dhis.mapping.action;

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
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.mapping.export.SVGDocument;
import org.hisp.dhis.mapping.export.SVGUtils;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.util.StreamActionSupport;

/**
 * @author Tran Thanh Tri
 * @version $Id$
 */
@SuppressWarnings( "serial" )
public class ExportImageAction
    extends StreamActionSupport
{
    private static final Log log = LogFactory.getLog( ExportImageAction.class );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    protected I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    // -------------------------------------------------------------------------
    // Output & input
    // -------------------------------------------------------------------------

    private String svg;

    public void setSvg( String svg )
    {
        this.svg = svg;
    }

    private String title;

    public void setTitle( String title )
    {
        this.title = title;
    }

    private Integer indicator;

    public void setIndicator( Integer indicator )
    {
        this.indicator = indicator;
    }

    private Integer period;

    public void setPeriod( Integer period )
    {
        this.period = period;
    }

    private String legends;

    public void setLegends( String legends )
    {
        this.legends = legends;
    }

    private boolean includeLegends;

    public void setIncludeLegends( boolean includeLegends )
    {
        this.includeLegends = includeLegends;
    }

    private Integer width;

    public void setWidth( Integer width )
    {
        this.width = width;
    }

    private Integer height;

    public void setHeight( Integer height )
    {
        this.height = height;
    }

    private String imageFormat;

    public void setImageFormat( String imageFormat )
    {
        this.imageFormat = imageFormat;
    }

    @Override
    protected String execute( HttpServletResponse response, OutputStream out )
        throws Exception
    {
        log.info( "Exporting image, width: " + width + ", height: " + height + ", format: " + this.imageFormat );

        if ( imageFormat.equalsIgnoreCase( "image/png" ) )
        {
            SVGUtils.convertToPNG( getSvg(), out, width, height );
        }
        else
        {
            SVGUtils.convertToJPEG( getSvg(), out, width, height );
        }

        return SUCCESS;
    }

    @Override
    protected String getContentType()
    {
        return this.imageFormat;
    }

    @Override
    protected String getFilename()
    {
        if ( imageFormat.equalsIgnoreCase( "image/png" ) )
            return "dhis2-gis-image.png";
        return "dhis2-gis-image.jpg";
    }

    private StringBuffer getSvg()
    {
        Period p = periodService.getPeriod( period );

        p.setName( format.formatPeriod( p ) );

        Indicator i = indicatorService.getIndicator( indicator );

        SVGDocument svgDocument = new SVGDocument();

        svgDocument.setTitle( this.title );
        svgDocument.setSvg( this.svg );
        svgDocument.setIndicator( i );
        svgDocument.setPeriod( p );
        svgDocument.setLegends( legends );
        svgDocument.setIncludeLegends( includeLegends );

        return svgDocument.getSVGForImage();
    }
}
