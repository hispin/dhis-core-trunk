package org.hisp.dhis.api.controller;

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

import org.hisp.dhis.api.utils.ContextUtils;
import org.hisp.dhis.api.utils.ContextUtils.CacheStrategy;
import org.hisp.dhis.common.view.ExportView;
import org.hisp.dhis.dxf2.metadata.*;
import org.hisp.dhis.dxf2.utils.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;
import java.util.zip.*;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Controller
@RequestMapping( method = RequestMethod.GET )
public class MetaDataController
{
    public static final String RESOURCE_PATH = "/metaData";

    @Autowired
    private ExportService exportService;

    @Autowired
    private ImportService importService;

    @Autowired
    private ContextUtils contextUtils;

    //--------------------------------------------------------------------------
    // Export
    //--------------------------------------------------------------------------

    @RequestMapping( value = MetaDataController.RESOURCE_PATH )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_EXPORT')" )
    public String export( @RequestParam Map<String, String> parameters, Model model )
    {
        WebOptions options = new WebOptions( parameters );
        MetaData metaData = exportService.getMetaData( options );

        model.addAttribute( "model", metaData );
        model.addAttribute( "viewClass", "export" );

        return "export";
    }

    @RequestMapping( value = MetaDataController.RESOURCE_PATH + ".xml", produces = "*/*" )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_EXPORT')" )
    public void exportXml( @RequestParam Map<String, String> parameters, HttpServletResponse response ) throws IOException
    {
        WebOptions options = new WebOptions( parameters );
        MetaData metaData = exportService.getMetaData( options );

        contextUtils.configureResponse( response, ContextUtils.CONTENT_TYPE_XML, CacheStrategy.NO_CACHE, "metaData.xml", true );

        JacksonUtils.toXmlWithView( response.getOutputStream(), metaData, ExportView.class );
    }

    @RequestMapping( value = MetaDataController.RESOURCE_PATH + ".json", produces = "*/*" )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_EXPORT')" )
    public void exportJson( @RequestParam Map<String, String> parameters, HttpServletResponse response ) throws IOException
    {
        WebOptions options = new WebOptions( parameters );
        MetaData metaData = exportService.getMetaData( options );

        contextUtils.configureResponse( response, ContextUtils.CONTENT_TYPE_JSON, CacheStrategy.NO_CACHE, "metaData.json", true );

        JacksonUtils.toXmlWithView( response.getOutputStream(), metaData, ExportView.class );
    }

    @RequestMapping( value = { MetaDataController.RESOURCE_PATH + ".zip" }, produces = "*/*" )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_EXPORT')" )
    public void exportZipped( @RequestParam Map<String, String> parameters, HttpServletResponse response, HttpServletRequest request ) throws IOException
    {
        if ( request.getHeader( "Accept" ).equalsIgnoreCase( "application/json" ) )
        {
            exportZippedJSON( parameters, response );
        }

        exportZippedXML( parameters, response );
    }

    @RequestMapping( value = { MetaDataController.RESOURCE_PATH + ".xml.zip" }, produces = "*/*" )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_EXPORT')" )
    public void exportZippedXML( @RequestParam Map<String, String> parameters, HttpServletResponse response ) throws IOException
    {
        WebOptions options = new WebOptions( parameters );
        MetaData metaData = exportService.getMetaData( options );

        contextUtils.configureResponse( response, ContextUtils.CONTENT_TYPE_ZIP, CacheStrategy.NO_CACHE, "metaData.xml.zip", true );
        response.addHeader( ContextUtils.HEADER_CONTENT_TRANSFER_ENCODING, "binary" );

        ZipOutputStream zip = new ZipOutputStream( response.getOutputStream() );
        zip.putNextEntry( new ZipEntry( "metaData.xml" ) );

        JacksonUtils.toXmlWithView( zip, metaData, ExportView.class );
    }

    @RequestMapping( value = { MetaDataController.RESOURCE_PATH + ".json.zip" }, produces = "*/*" )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_EXPORT')" )
    public void exportZippedJSON( @RequestParam Map<String, String> parameters, HttpServletResponse response ) throws IOException
    {
        WebOptions options = new WebOptions( parameters );
        MetaData metaData = exportService.getMetaData( options );

        contextUtils.configureResponse( response, ContextUtils.CONTENT_TYPE_ZIP, CacheStrategy.NO_CACHE, "metaData.json.zip", true );
        response.addHeader( ContextUtils.HEADER_CONTENT_TRANSFER_ENCODING, "binary" );

        ZipOutputStream zip = new ZipOutputStream( response.getOutputStream() );
        zip.putNextEntry( new ZipEntry( "metaData.json" ) );

        JacksonUtils.toJsonWithView( zip, metaData, ExportView.class );
    }

    @RequestMapping( value = { MetaDataController.RESOURCE_PATH + ".gz" }, produces = "*/*" )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_EXPORT')" )
    public void exportGZipped( @RequestParam Map<String, String> parameters, HttpServletResponse response, HttpServletRequest request ) throws IOException
    {
        if ( request.getHeader( "Accept" ).equalsIgnoreCase( "application/json" ) )
        {
            exportGZippedJSON( parameters, response );
        }

        exportGZippedXML( parameters, response );
    }

    @RequestMapping( value = { MetaDataController.RESOURCE_PATH + ".xml.gz" }, produces = "*/*" )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_EXPORT')" )
    public void exportGZippedXML( @RequestParam Map<String, String> parameters, HttpServletResponse response ) throws IOException
    {
        WebOptions options = new WebOptions( parameters );
        MetaData metaData = exportService.getMetaData( options );

        contextUtils.configureResponse( response, ContextUtils.CONTENT_TYPE_GZIP, CacheStrategy.NO_CACHE, "metaData.xml.gz", true );
        response.addHeader( ContextUtils.HEADER_CONTENT_TRANSFER_ENCODING, "binary" );

        GZIPOutputStream gzip = new GZIPOutputStream( response.getOutputStream() );
        JacksonUtils.toXmlWithView( gzip, metaData, ExportView.class );
    }

    @RequestMapping( value = { MetaDataController.RESOURCE_PATH + ".json.gz" }, method = RequestMethod.GET, produces = "*/*" )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_EXPORT')" )
    public void exportGZippedJSON( @RequestParam Map<String, String> parameters, HttpServletResponse response ) throws IOException
    {
        WebOptions options = new WebOptions( parameters );
        MetaData metaData = exportService.getMetaData( options );

        contextUtils.configureResponse( response, ContextUtils.CONTENT_TYPE_GZIP, CacheStrategy.NO_CACHE, "metaData.json.gz", true );
        response.addHeader( ContextUtils.HEADER_CONTENT_TRANSFER_ENCODING, "binary" );

        GZIPOutputStream gzip = new GZIPOutputStream( response.getOutputStream() );
        JacksonUtils.toJsonWithView( gzip, metaData, ExportView.class );
    }

    //--------------------------------------------------------------------------
    // Import
    //--------------------------------------------------------------------------

    @RequestMapping( value = MetaDataController.RESOURCE_PATH, method = RequestMethod.POST, consumes = { "application/xml", "text/*" } )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_IMPORT')" )
    public void importXml( ImportOptions importOptions, HttpServletResponse response, HttpServletRequest request ) throws JAXBException, IOException
    {
        MetaData metaData = JacksonUtils.fromXml( request.getInputStream(), MetaData.class );

        ImportSummary summary = importService.importMetaData( metaData, importOptions );

        response.setContentType( MediaType.APPLICATION_XML.toString() );
        JacksonUtils.toXml( response.getOutputStream(), summary );
    }

    @RequestMapping( value = MetaDataController.RESOURCE_PATH, method = RequestMethod.POST, consumes = "application/json" )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_IMPORT')" )
    public void importJson( ImportOptions importOptions, HttpServletResponse response, HttpServletRequest request ) throws IOException
    {
        MetaData metaData = JacksonUtils.fromJson( request.getInputStream(), MetaData.class );

        ImportSummary summary = importService.importMetaData( metaData, importOptions );

        response.setContentType( MediaType.APPLICATION_JSON.toString() );
        JacksonUtils.toJson( response.getOutputStream(), summary );
    }

    @RequestMapping( value = { MetaDataController.RESOURCE_PATH + ".zip", MetaDataController.RESOURCE_PATH + ".xml.zip" }, method = RequestMethod.POST, consumes = { "application/xml", "text/*" } )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_IMPORT')" )
    public void importZippedXml( ImportOptions importOptions, HttpServletResponse response, HttpServletRequest request ) throws JAXBException, IOException
    {
        ZipInputStream zip = new ZipInputStream( request.getInputStream() );
        zip.getNextEntry();

        MetaData metaData = JacksonUtils.fromXml( zip, MetaData.class );

        ImportSummary summary = importService.importMetaData( metaData, importOptions );

        response.setContentType( MediaType.APPLICATION_XML.toString() );
        JacksonUtils.toXml( response.getOutputStream(), summary );
    }

    @RequestMapping( value = { MetaDataController.RESOURCE_PATH + ".zip", MetaDataController.RESOURCE_PATH + ".json.zip" }, method = RequestMethod.POST, consumes = "application/json" )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_IMPORT')" )
    public void importZippedJson( ImportOptions importOptions, HttpServletResponse response, HttpServletRequest request ) throws IOException
    {
        ZipInputStream zip = new ZipInputStream( request.getInputStream() );
        zip.getNextEntry();

        MetaData metaData = JacksonUtils.fromJson( zip, MetaData.class );

        ImportSummary summary = importService.importMetaData( metaData, importOptions );

        response.setContentType( MediaType.APPLICATION_JSON.toString() );
        JacksonUtils.toJson( response.getOutputStream(), summary );
    }


    @RequestMapping( value = { MetaDataController.RESOURCE_PATH + ".gz", MetaDataController.RESOURCE_PATH + ".xml.gz" }, method = RequestMethod.POST, consumes = { "application/xml", "text/*" } )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_IMPORT')" )
    public void importGZippedXml( ImportOptions importOptions, HttpServletResponse response, HttpServletRequest request ) throws JAXBException, IOException
    {
        GZIPInputStream gzip = new GZIPInputStream( request.getInputStream() );

        MetaData metaData = JacksonUtils.fromXml( gzip, MetaData.class );

        ImportSummary summary = importService.importMetaData( metaData, importOptions );

        response.setContentType( MediaType.APPLICATION_XML.toString() );
        JacksonUtils.toXml( response.getOutputStream(), summary );
    }

    @RequestMapping( value = { MetaDataController.RESOURCE_PATH + ".gz", MetaDataController.RESOURCE_PATH + ".json.gz" }, method = RequestMethod.POST, consumes = "application/json" )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_METADATA_IMPORT')" )
    public void importGZippedJson( ImportOptions importOptions, HttpServletResponse response, HttpServletRequest request ) throws IOException
    {
        GZIPInputStream gzip = new GZIPInputStream( request.getInputStream() );

        MetaData metaData = JacksonUtils.fromJson( gzip, MetaData.class );

        ImportSummary summary = importService.importMetaData( metaData, importOptions );

        response.setContentType( MediaType.APPLICATION_JSON.toString() );
        JacksonUtils.toJson( response.getOutputStream(), summary );
    }
}
