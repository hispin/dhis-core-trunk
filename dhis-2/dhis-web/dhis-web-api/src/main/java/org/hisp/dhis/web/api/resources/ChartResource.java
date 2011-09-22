package org.hisp.dhis.web.api.resources;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.hisp.dhis.chart.Chart;
import org.hisp.dhis.chart.ChartService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nManager;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.system.util.CodecUtils;
import org.hisp.dhis.util.ContextUtils;
import org.hisp.dhis.web.api.ResponseUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

@Path( "/chart" )
public class ChartResource
{
    private ChartService chartService;

    public void setChartService( ChartService chartService )
    {
        this.chartService = chartService;
    }
    
    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }
    
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private I18nManager i18nManager;

    public void setI18nManager( I18nManager manager )
    {
        i18nManager = manager;
    }

    @GET
    @Path( "/{id}" )
    @Produces( ContextUtils.CONTENT_TYPE_PNG )
    public Response getChart( @PathParam("id") Integer id )
        throws Exception
    {
        final JFreeChart jFreeChart = chartService.getJFreeChart( id, i18nManager.getI18nFormat() );
        
        final Chart chart = chartService.getChart( id );
        
        final String filename = CodecUtils.filenameEncode( chart.getTitle() + ".png" );
        
        return ResponseUtils.response( true, filename, false ).entity( new StreamingOutput()
        {
            @Override
            public void write( OutputStream out )
                throws IOException, WebApplicationException
            {
                ChartUtilities.writeChartAsPNG( out, jFreeChart, 600, 400 );
            }
        } ).build();
    }
    
    @GET
    @Path( "/indicator/{indicator}/{orgUnit}" )
    @Produces( ContextUtils.CONTENT_TYPE_PNG )
    public Response getIndicatorChart( @PathParam("indicator") String indicatorUuid, @PathParam("orgUnit") String orgUnitUuid )
        throws Exception
    {
        final Indicator indicator = indicatorService.getIndicator( indicatorUuid );
        
        final OrganisationUnit unit = organisationUnitService.getOrganisationUnit( orgUnitUuid );
        
        if ( indicator == null || unit == null )
        {
            return null;
        }
        
        final I18nFormat format = i18nManager.getI18nFormat();
        
        final String filename = CodecUtils.filenameEncode( indicator.getName() + ".png" );
        
        final JFreeChart jFreeChart = chartService.getJFreeChart( indicator, unit, format );
        
        return ResponseUtils.response( true, filename, false ).entity( new StreamingOutput()
        {
            @Override
            public void write( OutputStream out )
                throws IOException, WebApplicationException
            {
                ChartUtilities.writeChartAsPNG( out, jFreeChart, 600, 400 );
            }
        } ).build();
    }
}
