package org.hisp.dhis.importexport.action.util;

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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.gml.GmlImportService;
import org.hisp.dhis.dxf2.gml.GmlPreProcessingResult;
import org.hisp.dhis.scheduling.TaskId;
import org.hisp.dhis.system.notification.NotificationLevel;
import org.hisp.dhis.system.notification.Notifier;
import org.springframework.web.util.HtmlUtils;
import org.xml.sax.SAXParseException;

import java.io.InputStream;

/**
 * @author Halvdan Hoem Grelland
 */
public class ImportMetaDataGmlTask
    implements Runnable
{
    private static final Log log = LogFactory.getLog( ImportMetaDataGmlTask.class );

    private TaskId taskId;

    private String userUid;


    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private GmlImportService gmlImportService;

    private Notifier notifier;

    private ImportOptions importOptions;

    private InputStream inputStream;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public ImportMetaDataGmlTask( String userUid, GmlImportService gmlImportService, Notifier notifier,
        ImportOptions importOptions, InputStream inputStream, TaskId taskId )
    {
        this.userUid = userUid;
        this.gmlImportService = gmlImportService;
        this.notifier = notifier;
        this.importOptions = importOptions;
        this.inputStream = inputStream;
        this.taskId = taskId;
    }

    // -------------------------------------------------------------------------
    // Runnable implementation
    // -------------------------------------------------------------------------

    @Override
    public void run()
    {
        importOptions.setImportStrategy( "update" ); // Force update only for GML import

        GmlPreProcessingResult gmlPreProcessingResult = gmlImportService.preProcessGml( inputStream );

        if ( !gmlPreProcessingResult.isSuccess() )
        {
            Throwable throwable = gmlPreProcessingResult.getThrowable();
            String message = createErrorMessage( throwable );

            notifier.notify( taskId, NotificationLevel.ERROR, message, false );
            log.error( "GML import failed: " + message, throwable );

            return;
        }

        gmlImportService.importGml( gmlPreProcessingResult.getResultMetaData(), userUid, importOptions, taskId );
    }

    private String createErrorMessage( Throwable throwable )
    {
        String message = "";
        Throwable rootThrowable = ExceptionUtils.getRootCause( throwable );

        if ( rootThrowable instanceof SAXParseException )
        {
            SAXParseException e = (SAXParseException) rootThrowable;
            message += "Syntax error on line " + e.getLineNumber() + ". " + e.getMessage();
        }
        else
        {
            message += rootThrowable.getMessage();
        }

        return HtmlUtils.htmlEscape( message );
    }
}
