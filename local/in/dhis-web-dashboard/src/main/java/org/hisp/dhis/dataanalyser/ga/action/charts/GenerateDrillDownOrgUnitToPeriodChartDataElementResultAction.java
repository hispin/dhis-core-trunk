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
package org.hisp.dhis.dataanalyser.ga.action.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.amplecode.quick.StatementManager;
import org.apache.struts2.ServletActionContext;
import org.apache.velocity.tools.generic.ListTool;
import org.hisp.dhis.dataanalyser.util.DashBoardService;
import org.hisp.dhis.dataanalyser.util.DataElementChartResult;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

/**
 * @author Mithilesh Kumar Thakur
 *
 * @version GenerateDrillDownOrgUnitToPeriodChartDataElementResultAction.java Jan 3, 2011 11:04:22 AM
 */
public class GenerateDrillDownOrgUnitToPeriodChartDataElementResultAction implements Action
{
    private final String PERIODWISE = "period";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private StatementManager statementManager;

    public void setStatementManager( StatementManager statementManager )
    {
        this.statementManager = statementManager;
    }
   
    private DashBoardService dashBoardService;

    public void setDashBoardService( DashBoardService dashBoardService )
    {
        this.dashBoardService = dashBoardService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private DataElementCategoryService dataElementCategoryService;

    public void setDataElementCategoryService( DataElementCategoryService dataElementCategoryService )
    {
        this.dataElementCategoryService = dataElementCategoryService;
    }
    
    private OrganisationUnitService organisationUnitService;

    public OrganisationUnitService getOrganisationUnitService()
    {
        return organisationUnitService;
    }

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private OrganisationUnitGroupService organisationUnitGroupService;
    
    public void setOrganisationUnitGroupService( OrganisationUnitGroupService organisationUnitGroupService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
    }

    // -------------------------------------------------------------------------
    // Input/output
    // -------------------------------------------------------------------------
    
    private String categoryLB;

    public void setCategoryLB( String categoryLB )
    {
        this.categoryLB = categoryLB;
    }

    public String getCategoryLB()
    {
        return categoryLB;
    }

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }
    
    private String selDrillDownData;
    
    public void setSelDrillDownData( String selDrillDownData )
    {
        this.selDrillDownData = selDrillDownData;
    }

    public String[] values;
    
    ListTool listTool;

    public ListTool getListTool()
    {
        return listTool;
    }
    
    private DataElementChartResult dataElementChartResult;
    
    public DataElementChartResult getDataElementChartResult()
    {
        return dataElementChartResult;
    }
    
    private List<DataElementCategoryOptionCombo> selectedOptionComboList;
    
    private OrganisationUnit selectedOrgUnit;
    
    private OrganisationUnitGroup selectedOrgUnitGroup;

    private DataElement dataElement;

    private HttpSession session;

    public HttpSession getSession()
    {
        return session;
    }
    
    private List<Date> selStartPeriodList;

    private List<Date> selEndPeriodList;
    
    private String selectedButton;
    
    public String getSelectedButton()
    {
        return selectedButton;
    }

    public void setSelectedButton( String selectedButton )
    {
        this.selectedButton = selectedButton;
    }

    public String[] startDateArray;
    public String[] endDateArray;
    public String[] priodNameArray;
    
    private  List<String> periodNames;
    
    private List<String> selectedDrillDownData;
    
    public List<String> getSelectedDrillDownData()
    {
        return selectedDrillDownData;
    }
    
    // -------------------------------------------------------------------------
    // Action implements
    // -------------------------------------------------------------------------
    
    public String execute()throws Exception
    {
        statementManager.initialise();
        
        selectedDrillDownData = new ArrayList<String>();
        
        selectedOptionComboList = new ArrayList<DataElementCategoryOptionCombo>();
        
        listTool = new ListTool();
        
        values = selDrillDownData.split( ":" );
        
        int orgunit =Integer.parseInt( values[0] );
        int orgUnitGroup = Integer.parseInt( values[1]);

        if ( orgUnitGroup != 0 )
        {
            selectedOrgUnitGroup = organisationUnitGroupService.getOrganisationUnitGroup( orgUnitGroup );
        }
        
        selectedOrgUnit = organisationUnitService.getOrganisationUnit( orgunit );
        
        int dataElementid = Integer.parseInt( values[2] );
        int optionComboid = Integer.parseInt( values[3] );
        
        dataElement = dataElementService.getDataElement( dataElementid );
        DataElementCategoryOptionCombo categoryCombo = dataElementCategoryService.getDataElementCategoryOptionCombo( optionComboid );
        
        List<DataElement> dataElementList = new ArrayList<DataElement>();
        dataElementList.add( dataElement ); 
        
        selectedOptionComboList.add( categoryCombo );
        
        String periodTypeLB = values[4];
        
        selStartPeriodList = new ArrayList<Date>();
        selEndPeriodList = new ArrayList<Date>();
        periodNames = new ArrayList<String>();
        
        startDateArray = values[5].split( ";" );
        for ( int i = 0 ; i < startDateArray.length ; i++ )
        {
            String startD = startDateArray[i];
            selStartPeriodList.add( format.parseDate( startD ) );
        }
        
        endDateArray = values[6].split( ";" );
        for ( int i = 0 ; i < endDateArray.length ; i++ )
        {
            String startD = endDateArray[i];
            selEndPeriodList.add( format.parseDate( startD ) );
        }
        
        priodNameArray = values[7].split( ";" );
        for ( int i = 0 ; i < priodNameArray.length ; i++ )
        {
            String startD = priodNameArray[i];
            periodNames.add( startD );
        }
        
        String deSelection = values[8];
        String aggDataCB = values[9];
        
        System.out.println( selStartPeriodList + ":" + selEndPeriodList + ":" + periodTypeLB + ":" +  dataElementList+ ":" + deSelection + ":" + selectedOptionComboList +  ":" + selectedOrgUnit + ":" + aggDataCB );
        System.out.println( "Chart Generation Start Time is for drillDown: \t" + new Date() );
        
        if( orgUnitGroup == 0 && ( categoryLB.equalsIgnoreCase( PERIODWISE )) ) 
        {
            int periodCount = 0;
            for( Date startDate : selStartPeriodList )
            {
                String drillDownPeriodName = periodNames.get( periodCount );
                Date endDate = selEndPeriodList.get( periodCount );
                String tempStartDate = format.formatDate( startDate );
                String tempEndDate   = format.formatDate( endDate );
                String drillDownData = selectedOrgUnit.getId() + ":" + "0" + ":" + dataElement.getId() + ":"+ categoryCombo.getId() + ":"  + periodTypeLB + ":" + tempStartDate + ":" + tempEndDate + ":" + drillDownPeriodName + ":" + deSelection + ":" + aggDataCB;
                selectedDrillDownData.add( drillDownData );
                periodCount++;
            }
        }
          
        if( orgUnitGroup != 0 && ( categoryLB.equalsIgnoreCase( PERIODWISE )) ) 
        {
            int periodCount = 0;
            for( Date startdate : selStartPeriodList )
            {
                String drillDownPeriodName = periodNames.get( periodCount );
                Date endDate = selEndPeriodList.get( periodCount );
                String tempStartDate = format.formatDate( startdate );
                String tempEndDate   = format.formatDate( endDate );
                String drillDownData = selectedOrgUnit.getId() + ":" + "0" + ":" + dataElement.getId() + ":"+ categoryCombo.getId() + ":"  + periodTypeLB + ":" + tempStartDate + ":" + tempEndDate + ":" + drillDownPeriodName + ":" + deSelection + ":" + aggDataCB;
                selectedDrillDownData.add( drillDownData );
                periodCount++;
            }
        }
        
        
        if( orgUnitGroup != 0 )
        {
            dataElementChartResult = dashBoardService.generateDataElementChartDataWithGroupToPeriodWise( selStartPeriodList, selEndPeriodList, periodNames ,periodTypeLB, dataElementList, deSelection, selectedOptionComboList, selectedOrgUnit, selectedOrgUnitGroup ,aggDataCB );
        }
        else
        {
            dataElementChartResult = dashBoardService.generateDataElementChartDataWithPeriodWise( selStartPeriodList, selEndPeriodList,periodNames, periodTypeLB, dataElementList, deSelection, selectedOptionComboList, selectedOrgUnit, aggDataCB );
        }
        
        ActionContext ctx = ActionContext.getContext();
        HttpServletRequest req = (HttpServletRequest) ctx.get( ServletActionContext.HTTP_REQUEST );

        session = req.getSession();
        
        session.setAttribute( "data1", dataElementChartResult.getData() );
        session.setAttribute( "series1", dataElementChartResult.getSeries() );
        session.setAttribute( "categories1", dataElementChartResult.getCategories() );
        session.setAttribute( "chartTitle", dataElementChartResult.getChartTitle() );
        session.setAttribute( "xAxisTitle", dataElementChartResult.getXAxis_Title() );
        session.setAttribute( "yAxisTitle", dataElementChartResult.getYAxis_Title() );
        
        System.out.println( "Chart Generation End Time is drillDown : \t" + new Date() );
        
        statementManager.destroy();
        return SUCCESS;
    }

    
}


