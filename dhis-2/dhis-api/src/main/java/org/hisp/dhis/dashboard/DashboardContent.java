package org.hisp.dhis.dashboard;

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

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.datamart.DataMartExport;
import org.hisp.dhis.document.Document;
import org.hisp.dhis.mapping.MapView;
import org.hisp.dhis.olap.OlapURL;
import org.hisp.dhis.report.Report;
import org.hisp.dhis.reporttable.ReportTable;
import org.hisp.dhis.user.User;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class DashboardContent
{
    private final static int MAX_DASHBOARD_ELEMENTS = 6;
    
    private int id;
    
    private User user;

    private List<Report> reports = new ArrayList<Report>();
    
    private List<DataMartExport> dataMartExports = new ArrayList<DataMartExport>();
    
    private List<OlapURL> olapUrls = new ArrayList<OlapURL>();
    
    private List<Document> documents = new ArrayList<Document>();
    
    private List<ReportTable> reportTables = new ArrayList<ReportTable>();

    private List<MapView> mapViews = new ArrayList<MapView>();

    public DashboardContent()
    {   
    }
    
    public DashboardContent( User user )
    {
        this.user = user;
    }
    
    public int hashCode()
    {
        return user.hashCode();
    }
    
    public boolean equals( Object object )
    {        
        if ( this == object )
        {
            return true;
        }
        
        if ( object == null )
        {
            return false;
        }
        
        if ( object.getClass() != getClass() )
        {
            return false;
        }
        
        final DashboardContent other = (DashboardContent) object;
        
        return user.equals( other.user );
    }
    
    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public void addReport( Report report )
    {
        if ( !reports.contains( report ) )
        {
            reports.add( 0, report );
            
            while ( reports.size() > MAX_DASHBOARD_ELEMENTS )
            {
                reports.remove( MAX_DASHBOARD_ELEMENTS );
            }
        }
    }
    
    public void addDataMartExport( DataMartExport export )
    {
        if ( !dataMartExports.contains( export ) )
        {
            dataMartExports.add( 0, export );
            
            while ( dataMartExports.size() > MAX_DASHBOARD_ELEMENTS )
            {
                dataMartExports.remove( MAX_DASHBOARD_ELEMENTS );
            }
        }
    }
    
    public void addOlapUrl( OlapURL url )
    {
        if ( !olapUrls.contains( url ) )
        {
            olapUrls.add( 0, url );
            
            while ( olapUrls.size() > MAX_DASHBOARD_ELEMENTS )
            {
                olapUrls.remove( MAX_DASHBOARD_ELEMENTS );
            }
        }
    }
    
    public void addDocument( Document document )
    {
        if ( !documents.contains( document ) )
        {
            documents.add( 0, document );
            
            while ( documents.size() > MAX_DASHBOARD_ELEMENTS )
            {
                documents.remove( MAX_DASHBOARD_ELEMENTS );
            }
        }
    }
    
    public void addReportTable( ReportTable reportTable )
    {
        if ( !reportTables.contains( reportTable ) )
        {
            reportTables.add( 0, reportTable );
            
            while ( reportTables.size() > MAX_DASHBOARD_ELEMENTS )
            {
                reportTables.remove( MAX_DASHBOARD_ELEMENTS );
            }
        }
    }
    
    public void addMapView( MapView mapView )
    {
        if ( !mapViews.contains( mapView ) )
        {
            mapViews.add( 0, mapView );
            
            while ( mapViews.size() > MAX_DASHBOARD_ELEMENTS )
            {
                mapViews.remove( MAX_DASHBOARD_ELEMENTS );
            }
        }
    }
    
    // -------------------------------------------------------------------------
    // Getters & setters
    // -------------------------------------------------------------------------

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser( User user )
    {
        this.user = user;
    }

    public List<Report> getReports()
    {
        return reports;
    }

    public void setReports( List<Report> reports )
    {
        this.reports = reports;
    }

    public List<DataMartExport> getDataMartExports()
    {
        return dataMartExports;
    }

    public void setDataMartExports( List<DataMartExport> dataMartExports )
    {
        this.dataMartExports = dataMartExports;
    }

    public List<OlapURL> getOlapUrls()
    {
        return olapUrls;
    }

    public void setOlapUrls( List<OlapURL> olapUrls )
    {
        this.olapUrls = olapUrls;
    }

    public List<Document> getDocuments()
    {
        return documents;
    }

    public void setDocuments( List<Document> documents )
    {
        this.documents = documents;
    }

    public List<ReportTable> getReportTables()
    {
        return reportTables;
    }

    public void setReportTables( List<ReportTable> reportTables )
    {
        this.reportTables = reportTables;
    }

    public List<MapView> getMapViews()
    {
        return mapViews;
    }

    public void setMapViews( List<MapView> mapViews )
    {
        this.mapViews = mapViews;
    }
}
