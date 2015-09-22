package org.hisp.dhis.webapi.controller;

/*
 * Copyright (c) 2004-2015, University of Oslo
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

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.dxf2.webmessage.WebMessage;
import org.hisp.dhis.dxf2.webmessage.WebMessageException;
import org.hisp.dhis.dxf2.webmessage.WebMessageStatus;
import org.hisp.dhis.dxf2.webmessage.responses.FileResourceWebMessageResponse;
import org.hisp.dhis.fileresource.FileResource;
import org.hisp.dhis.fileresource.FileResourceDomain;
import org.hisp.dhis.fileresource.FileResourceService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.system.util.ValidationUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.webapi.utils.InputUtils;
import org.hisp.dhis.webapi.utils.WebMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hisp.dhis.setting.SystemSettingManager.KEY_DATA_IMPORT_REQUIRE_CATEGORY_OPTION_COMBO;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_DATA_IMPORT_STRICT_CATEGORY_OPTION_COMBOS;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_DATA_IMPORT_STRICT_ORGANISATION_UNITS;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_DATA_IMPORT_STRICT_PERIODS;

/**
 * @author Lars Helge Overland
 */
@Controller
@RequestMapping( value = DataValueController.RESOURCE_PATH )
public class DataValueController
{
    public static final String RESOURCE_PATH = "/dataValues";

    // ---------------------------------------------------------------------
    // Dependencies
    // ---------------------------------------------------------------------

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private DataElementCategoryService categoryService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private DataValueService dataValueService;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private IdentifiableObjectManager idObjectManager;
    
    @Autowired
    private SystemSettingManager systemSettingManager;

    @Autowired
    private InputUtils inputUtils;

    @Autowired
    private FileResourceService fileResourceService;

    // ---------------------------------------------------------------------
    // POST
    // ---------------------------------------------------------------------

    @PreAuthorize( "hasRole('ALL') or hasRole('F_DATAVALUE_ADD')" )
    @RequestMapping( method = RequestMethod.POST, produces = "text/plain" )
    public void saveDataValue(
        @RequestParam String de,
        @RequestParam( required = false ) String co,
        @RequestParam( required = false ) String cc,
        @RequestParam( required = false ) String cp,
        @RequestParam String pe,
        @RequestParam String ou,
        @RequestParam( required = false ) String value,
        @RequestParam( required = false ) String comment,
        @RequestParam( required = false ) boolean followUp, HttpServletResponse response )
        throws WebMessageException
    {
        boolean strictPeriods = (Boolean) systemSettingManager.getSystemSetting( KEY_DATA_IMPORT_STRICT_PERIODS, false );
        boolean strictCategoryOptionCombos = (Boolean) systemSettingManager.getSystemSetting( KEY_DATA_IMPORT_STRICT_CATEGORY_OPTION_COMBOS, false );
        boolean strictOrgUnits = (Boolean) systemSettingManager.getSystemSetting( KEY_DATA_IMPORT_STRICT_ORGANISATION_UNITS, false );
        boolean requireCategoryOptionCombo = (Boolean) systemSettingManager.getSystemSetting( KEY_DATA_IMPORT_REQUIRE_CATEGORY_OPTION_COMBO, false );
        
        // ---------------------------------------------------------------------
        // Input validation
        // ---------------------------------------------------------------------

        DataElement dataElement = getAndValidateDataElement( de );

        DataElementCategoryOptionCombo categoryOptionCombo = getAndValidateCategoryOptionCombo( co, requireCategoryOptionCombo );

        DataElementCategoryOptionCombo attributeOptionCombo = getAndValidateAttributeOptionCombo( cc, cp );

        Period period = getAndValidatePeriod( pe );

        OrganisationUnit organisationUnit = getAndValidateOrganisationUnit( ou );
        
        validateInvalidFuturePeriod( period, dataElement );

        String valueValid = ValidationUtils.dataValueIsValid( value, dataElement );

        if ( valueValid != null )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Invalid value: " + value + ", must match data element type: " + dataElement.getValueType() ) );
        }

        String commentValid = ValidationUtils.commentIsValid( comment );

        if ( commentValid != null )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Invalid comment: " + comment ) );
        }

        // ---------------------------------------------------------------------
        // Optional constraints
        // ---------------------------------------------------------------------

        if ( strictPeriods && !dataElement.getPeriodTypes().contains( period.getPeriodType() ) )
        {
            throw new WebMessageException( WebMessageUtils.conflict( 
                "Period type of period: " + period.getIsoDate() + " not valid for data element: " + dataElement.getUid() ) );
        }
        
        if ( strictCategoryOptionCombos && !dataElement.getCategoryCombo().getOptionCombos().contains( categoryOptionCombo ) )
        {
            throw new WebMessageException( WebMessageUtils.conflict( 
                "Category option combo: " + categoryOptionCombo.getUid() + " must be part of category combo of data element: " + dataElement.getUid() ) );
        }
        
        if ( strictOrgUnits && !dataElement.hasDataSetOrganisationUnit( organisationUnit ) )
        {
            throw new WebMessageException( WebMessageUtils.conflict( 
                "Data element: " + dataElement.getUid() + " must be assigned through data sets to organisation unit: " + organisationUnit.getUid() ) );
        }
        
        // ---------------------------------------------------------------------
        // Locking validation
        // ---------------------------------------------------------------------

        validateDataSetNotLocked( dataElement, period, organisationUnit );

        // ---------------------------------------------------------------------
        // Deal with file resource
        // ---------------------------------------------------------------------

        FileResource fileResource = null;

        if ( dataElement.getValueType() == ValueType.FILE_RESOURCE )
        {
            fileResource = fileResourceService.getFileResource( value );

            if ( fileResource == null || fileResource.getDomain() != FileResourceDomain.DATA_VALUE )
            {
                throw new WebMessageException( WebMessageUtils.notFound( FileResource.class, value ) );
            }

            if ( fileResource.isAssigned() )
            {
                throw new WebMessageException( WebMessageUtils.conflict( "File resource is already assigned or is linked to another data value" ) );
            }

            fileResource.setAssigned( true );
        }

        // ---------------------------------------------------------------------
        // Assemble and save data value
        // ---------------------------------------------------------------------

        String storedBy = currentUserService.getCurrentUsername();

        Date now = new Date();

        DataValue dataValue = dataValueService.getDataValue( dataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo );

        if ( dataValue == null )
        {
            dataValue = new DataValue( dataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo,
                StringUtils.trimToNull( value ), storedBy, now, StringUtils.trimToNull( comment ) );

            dataValueService.addDataValue( dataValue );
        }
        else
        {
            if ( value == null && ValueType.TRUE_ONLY.equals( dataElement.getValueType() ) )
            {
                if ( comment == null )
                {
                    dataValueService.deleteDataValue( dataValue );
                    return;
                }
                else
                {
                    value = "false";
                }
            }

            if ( value != null )
            {
                if ( dataElement.isFileType() )
                {
                    fileResourceService.deleteFileResource( dataValue.getValue() );
                }

                dataValue.setValue( StringUtils.trimToNull( value ) );
            }

            if ( comment != null )
            {
                dataValue.setComment( StringUtils.trimToNull( comment ) );
            }

            if ( followUp )
            {
                dataValue.toggleFollowUp();
            }

            dataValue.setLastUpdated( now );
            dataValue.setStoredBy( storedBy );

            dataValueService.updateDataValue( dataValue );
        }

        if ( fileResource != null )
        {
            fileResourceService.updateFileResource( fileResource );
        }
    }

    // ---------------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------------

    @PreAuthorize( "hasRole('ALL') or hasRole('F_DATAVALUE_DELETE')" )
    @RequestMapping( method = RequestMethod.DELETE, produces = "text/plain" )
    public void deleteDataValue(
        @RequestParam String de,
        @RequestParam( required = false ) String co,
        @RequestParam( required = false ) String cc,
        @RequestParam( required = false ) String cp,
        @RequestParam String pe,
        @RequestParam String ou, HttpServletResponse response )
        throws WebMessageException
    {
        // ---------------------------------------------------------------------
        // Input validation
        // ---------------------------------------------------------------------

        DataElement dataElement = getAndValidateDataElement( de );

        DataElementCategoryOptionCombo categoryOptionCombo = getAndValidateCategoryOptionCombo( co, false );

        DataElementCategoryOptionCombo attributeOptionCombo = getAndValidateAttributeOptionCombo( cc, cp );

        Period period = getAndValidatePeriod( pe );

        OrganisationUnit organisationUnit = getAndValidateOrganisationUnit( ou );

        // ---------------------------------------------------------------------
        // Locking validation
        // ---------------------------------------------------------------------

        validateDataSetNotLocked( dataElement, period, organisationUnit );

        // ---------------------------------------------------------------------
        // Delete data value
        // ---------------------------------------------------------------------

        DataValue dataValue = dataValueService.getDataValue( dataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo );

        if ( dataValue == null )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Data value cannot be deleted because it does not exist" ) );
        }

        dataValueService.deleteDataValue( dataValue );
    }

    // ---------------------------------------------------------------------
    // GET
    // ---------------------------------------------------------------------

    @RequestMapping( method = RequestMethod.GET )
    public String getDataValue(
        @RequestParam String de,
        @RequestParam( required = false ) String co,
        @RequestParam( required = false ) String cc,
        @RequestParam( required = false ) String cp,
        @RequestParam String pe,
        @RequestParam String ou,
        Model model, HttpServletResponse response )
        throws WebMessageException
    {
        // ---------------------------------------------------------------------
        // Input validation
        // ---------------------------------------------------------------------

        DataElement dataElement = getAndValidateDataElement( de );

        DataElementCategoryOptionCombo categoryOptionCombo = getAndValidateCategoryOptionCombo( co, false );

        DataElementCategoryOptionCombo attributeOptionCombo = getAndValidateAttributeOptionCombo( cc, cp );

        Period period = getAndValidatePeriod( pe );

        OrganisationUnit organisationUnit = getAndValidateOrganisationUnit( ou );

        // ---------------------------------------------------------------------
        // Locking validation
        // ---------------------------------------------------------------------

        validateDataSetNotLocked( dataElement, period, organisationUnit );

        // ---------------------------------------------------------------------
        // Get data value
        // ---------------------------------------------------------------------

        DataValue dataValue = dataValueService.getDataValue( dataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo );

        if ( dataValue == null )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Data value does not exist" ) );
        }

        List<String> value = new ArrayList<>();
        value.add( dataValue.getValue() );

        model.addAttribute( "model", value );

        return "value";
    }

    // ---------------------------------------------------------------------
    // POST file
    // ---------------------------------------------------------------------

    @PreAuthorize( "hasRole('ALL') or hasRole('F_DATAVALUE_ADD')" )
    @RequestMapping( value = "/files", method = RequestMethod.POST )
    public @ResponseBody WebMessage saveDataValueFileResource(
        @RequestParam String de,
        @RequestParam( required = false ) String co,
        @RequestParam( required = false ) String cc,
        @RequestParam( required = false ) String cp,
        @RequestParam String pe,
        @RequestParam String ou,
        @RequestParam( value = "file", required = true ) MultipartFile multipartFile,
        HttpServletResponse response )
        throws WebMessageException, IOException
    {
        boolean strictPeriods = (Boolean) systemSettingManager.getSystemSetting( KEY_DATA_IMPORT_STRICT_PERIODS, false );
        boolean strictCategoryOptionCombos = (Boolean) systemSettingManager.getSystemSetting( KEY_DATA_IMPORT_STRICT_CATEGORY_OPTION_COMBOS, false );
        boolean strictOrgUnits = (Boolean) systemSettingManager.getSystemSetting( KEY_DATA_IMPORT_STRICT_ORGANISATION_UNITS, false );
        boolean requireCategoryOptionCombo = (Boolean) systemSettingManager.getSystemSetting( KEY_DATA_IMPORT_REQUIRE_CATEGORY_OPTION_COMBO, false );

        // ---------------------------------------------------------------------
        // Input validation
        // ---------------------------------------------------------------------

        DataElement dataElement = getAndValidateDataElement( de );

        DataElementCategoryOptionCombo categoryOptionCombo = getAndValidateCategoryOptionCombo( co, requireCategoryOptionCombo );

        getAndValidateAttributeOptionCombo( cc, cp );

        Period period = getAndValidatePeriod( pe );

        OrganisationUnit organisationUnit = getAndValidateOrganisationUnit( ou );

        validateInvalidFuturePeriod( period, dataElement );

        if ( multipartFile == null || multipartFile.isEmpty() )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "File is missing",
                "The multipart request didn't contain a file or the file was empty." ) );
        }

        // ---------------------------------------------------------------------
        // Optional constraints
        // ---------------------------------------------------------------------

        if ( strictPeriods && !dataElement.getPeriodTypes().contains( period.getPeriodType() ) )
        {
            throw new WebMessageException( WebMessageUtils.conflict(
                "Period type of period: " + period.getIsoDate() + " not valid for data element: " + dataElement.getUid() ) );
        }

        if ( strictCategoryOptionCombos && !dataElement.getCategoryCombo().getOptionCombos().contains( categoryOptionCombo ) )
        {
            throw new WebMessageException( WebMessageUtils.conflict(
                "Category option combo: " + categoryOptionCombo.getUid() + " must be part of category combo of data element: " + dataElement.getUid() ) );
        }

        if ( strictOrgUnits && !dataElement.hasDataSetOrganisationUnit( organisationUnit ) )
        {
            throw new WebMessageException( WebMessageUtils.conflict(
                "Data element: " + dataElement.getUid() + " must be assigned through data sets to organisation unit: " + organisationUnit.getUid() ) );
        }

        // ---------------------------------------------------------------------
        // Locking validation
        // ---------------------------------------------------------------------

        validateDataSetNotLocked( dataElement, period, organisationUnit );

        // ---------------------------------------------------------------------
        // Validate and assemble FileResource
        // ---------------------------------------------------------------------

        String filename = StringUtils.defaultIfBlank( FilenameUtils.getName( multipartFile.getOriginalFilename() ), "untitled" );

        String contentType = multipartFile.getContentType();
        contentType = isValidContentType( contentType ) ? contentType : ContentType.APPLICATION_OCTET_STREAM.toString();

        long contentLength = multipartFile.getSize();

        if ( contentLength <= 0 )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Could not read file or file is empty" ) );
        }

        ByteSource content = new ByteSource()
        {
            @Override
            public InputStream openStream()
            {
                try
                {
                    return multipartFile.getInputStream();
                }
                catch ( IOException e )
                {
                    return new NullInputStream( 0 );
                }
            }
        };

        String contentMD5 = content.hash( Hashing.md5() ).toString(); // TODO Consider letting filestore create the hash

        FileResource fileResource = new FileResource( filename, contentType, contentLength, contentMD5, FileResourceDomain.DATA_VALUE );
        fileResource.setAssigned( false );
        fileResource.setCreated( new Date() );
        fileResource.setUser( currentUserService.getCurrentUser() );

        // ---------------------------------------------------------------------
        // Save file resource
        // ---------------------------------------------------------------------

        String uid = fileResourceService.saveFileResource( fileResource, content );

        if ( uid == null )
        {
            throw new WebMessageException( WebMessageUtils.error( "Saving the file failed" ) );
        }

        WebMessage webMessage = new WebMessage( WebMessageStatus.OK, HttpStatus.CREATED );
        webMessage.setResponse( new FileResourceWebMessageResponse( fileResource ) );

        return webMessage;
    }

    // ---------------------------------------------------------------------
    // GET file
    // ---------------------------------------------------------------------

    @RequestMapping( value = "/files", method = RequestMethod.GET )
    public void getDataValueFile(
        @RequestParam String de,
        @RequestParam( required = false ) String co,
        @RequestParam( required = false ) String cc,
        @RequestParam( required = false ) String cp,
        @RequestParam String pe,
        @RequestParam String ou, HttpServletResponse response )
        throws WebMessageException
    {
        // ---------------------------------------------------------------------
        // Input validation
        // ---------------------------------------------------------------------

        DataElement dataElement = getAndValidateDataElement( de );

        if ( !dataElement.isFileType() )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "DataElement must be of type file" ) );
        }

        DataElementCategoryOptionCombo categoryOptionCombo = getAndValidateCategoryOptionCombo( co, false );

        DataElementCategoryOptionCombo attributeOptionCombo = getAndValidateAttributeOptionCombo( cc, cp );

        Period period = getAndValidatePeriod( pe );

        OrganisationUnit organisationUnit = getAndValidateOrganisationUnit( ou );

        // ---------------------------------------------------------------------
        // Locking validation
        // ---------------------------------------------------------------------

        validateDataSetNotLocked( dataElement, period, organisationUnit );

        // ---------------------------------------------------------------------
        // Get data value
        // ---------------------------------------------------------------------

        DataValue dataValue = dataValueService.getDataValue( dataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo );

        if ( dataValue == null )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Data value does not exist" ) );
        }

        // ---------------------------------------------------------------------
        // Get file resource
        // ---------------------------------------------------------------------

        String uid = dataValue.getValue();

        FileResource fileResource = fileResourceService.getFileResource( uid );

        if ( fileResource == null )
        {
            throw new WebMessageException( WebMessageUtils.notFound( "The file resource reference id " + uid + " was not found." ) );
        }

        if ( fileResource.getDomain() != FileResourceDomain.DATA_VALUE )
        {
            throw  new WebMessageException( WebMessageUtils.conflict( "File resource domain must be DATA_VALUE" ) );
        }

        ByteSource content = fileResourceService.getFileResourceContent( fileResource );

        if ( content == null )
        {
            throw new WebMessageException( WebMessageUtils.notFound( "The referenced file could not be found" ) );
        }

        // ---------------------------------------------------------------------
        // Build response and return
        // ---------------------------------------------------------------------

        response.setContentType( fileResource.getContentType() );
        response.setContentLength( Math.round( fileResource.getContentLength() ) );
        response.setHeader( HttpHeaders.CONTENT_DISPOSITION, "filename=" + fileResource.getName() );

        InputStream inputStream = null;

        try
        {
            inputStream = content.openStream();
            IOUtils.copy( inputStream, response.getOutputStream() );
        }
        catch ( IOException e )
        {
            throw new WebMessageException( WebMessageUtils.error( "Failed fetching the file from storage",
                "There was an exception when trying to fetch the file from the storage backend. " +
                    "Depending on the provider the root cause could be network or file system related." ) );
        }
        finally
        {
            IOUtils.closeQuietly( inputStream );
        }
    }

    // ---------------------------------------------------------------------
    // Supportive methods
    // ---------------------------------------------------------------------

    private DataElement getAndValidateDataElement( String de )
        throws WebMessageException
    {
        DataElement dataElement = idObjectManager.get( DataElement.class, de );

        if ( dataElement == null )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Illegal data element identifier: " + de ) );
        }

        return dataElement;
    }

    private DataElementCategoryOptionCombo getAndValidateCategoryOptionCombo( String co, boolean requireCategoryOptionCombo )
        throws WebMessageException
    {
        DataElementCategoryOptionCombo categoryOptionCombo = categoryService.getDataElementCategoryOptionCombo( co );

        if ( categoryOptionCombo == null )
        {
            if ( requireCategoryOptionCombo )
            {
                throw new WebMessageException( WebMessageUtils.conflict( "Category option combo is required but is not specified" ) );
            }
            else if ( co != null )
            {
                throw new WebMessageException( WebMessageUtils.conflict( "Illegal category option combo identifier: " + co ) );
            }
            else
            {
                categoryOptionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
            }
        }

        return categoryOptionCombo;
    }

    private DataElementCategoryOptionCombo getAndValidateAttributeOptionCombo( String cc, String cp )
        throws WebMessageException
    {
        DataElementCategoryOptionCombo attributeOptionCombo = inputUtils.getAttributeOptionCombo( cc, cp );

        if ( attributeOptionCombo == null )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Illegal attribute option combo identifier: " + cc + " " + cp ) );
        }

        return attributeOptionCombo;
    }

    private Period getAndValidatePeriod( String pe )
        throws WebMessageException
    {
        Period period = PeriodType.getPeriodFromIsoString( pe );

        if ( period == null )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Illegal period identifier: " + pe ) );
        }

        return period;
    }

    private OrganisationUnit getAndValidateOrganisationUnit( String ou )
        throws WebMessageException
    {
        OrganisationUnit organisationUnit = idObjectManager.get( OrganisationUnit.class, ou );

        if ( organisationUnit == null )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Illegal organisation unit identifier: " + ou ) );
        }

        boolean isInHierarchy = organisationUnitService.isInUserHierarchy( organisationUnit );

        if ( !isInHierarchy )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Organisation unit is not in the hierarchy of the current user: " + ou ) );
        }

        return organisationUnit;
    }

    private void validateInvalidFuturePeriod( Period period, DataElement dataElement )
        throws WebMessageException
    {
        boolean invalidFuturePeriod = period.isFuture() && dataElement.getOpenFuturePeriods() <= 0;

        if ( invalidFuturePeriod )
        {
            throw new WebMessageException(
                WebMessageUtils.conflict( "One or more data sets for data element does not allow future periods: " + dataElement.getUid() ) );
        }
    }

    private void validateDataSetNotLocked( DataElement dataElement, Period period, OrganisationUnit organisationUnit )
        throws WebMessageException
    {
        if ( dataSetService.isLocked( dataElement, period, organisationUnit, null ) )
        {
            throw new WebMessageException( WebMessageUtils.conflict( "Data set is locked" ) );
        }
    }

    private boolean isValidContentType( String contentType )
    {
        try
        {
            MimeType.valueOf( contentType );
        }
        catch ( InvalidMimeTypeException e )
        {
            return false;
        }

        return true;
    }
}
