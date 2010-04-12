package org.hisp.dhis.dataadmin.action.maintenance;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.common.DeleteNotAllowedException;
import org.hisp.dhis.completeness.DataSetCompletenessStore;
import org.hisp.dhis.datamart.DataMartStore;
import org.hisp.dhis.maintenance.MaintenanceService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;

import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class PerformMaintenanceAction
    extends ActionSupport
{
    private static final Log log = LogFactory.getLog( PerformMaintenanceAction.class );
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private MaintenanceService maintenanceService;

    public void setMaintenanceService( MaintenanceService maintenanceService )
    {
        this.maintenanceService = maintenanceService;
    }
    
    private DataSetCompletenessStore completenessStore;

    public void setCompletenessStore( DataSetCompletenessStore completenessStore )
    {
        this.completenessStore = completenessStore;
    }
    
    private DataMartStore dataMartStore;

    public void setDataMartStore( DataMartStore dataMartStore )
    {
        this.dataMartStore = dataMartStore;
    }
    
    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }
    
    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------
    
    private boolean hierarchyHistory;

    public void setHierarchyHistory( boolean hierarchyHistory )
    {
        this.hierarchyHistory = hierarchyHistory;
    }

    private boolean aggregatedDataValues;

    public void setAggregatedDataValues( boolean aggregatedDataValues )
    {
        this.aggregatedDataValues = aggregatedDataValues;
    }

    private boolean aggregatedIndicatorValues;

    public void setAggregatedIndicatorValues( boolean aggregatedIndicatorValues )
    {
        this.aggregatedIndicatorValues = aggregatedIndicatorValues;
    }

    private boolean zeroValues;

    public void setZeroValues( boolean zeroValues )
    {
        this.zeroValues = zeroValues;
    }
    
    private boolean dataSetCompleteness;

    public void setDataSetCompleteness( boolean dataSetCompleteness )
    {
        this.dataSetCompleteness = dataSetCompleteness;
    }
    
    private boolean prunePeriods;

    public void setPrunePeriods( boolean prunePeriods )
    {
        this.prunePeriods = prunePeriods;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    
    public String execute() 
        throws Exception
    {
        if ( hierarchyHistory )
        {
            organisationUnitService.clearOrganisationUnitHierarchyHistory();
        }
        
        if ( aggregatedDataValues )
        {
            dataMartStore.deleteAggregatedDataValues();
        }
        
        if ( aggregatedIndicatorValues )
        {
            dataMartStore.deleteAggregatedIndicatorValues();
        }
        
        if ( zeroValues )
        {
            maintenanceService.deleteZeroDataValues();
        }
        
        if ( dataSetCompleteness )
        {
            completenessStore.deleteDataSetCompleteness();
        }
        
        if ( prunePeriods )
        {
            prunePeriods();
        }
        
        return SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------
    
    private void prunePeriods()
    {
        for ( Period period : periodService.getAllPeriods() )
        {
            int periodId = period.getId();
            
            try
            {
                periodService.deletePeriod( period );
                
                log.info( "Deleted period with id: " + periodId );
            }
            catch ( DeleteNotAllowedException ex )
            {
                log.debug( "Period has associated objects and could not be deleted: " + periodId );
            }
        }
        
        log.info( "Period pruning done" );
    }
}
