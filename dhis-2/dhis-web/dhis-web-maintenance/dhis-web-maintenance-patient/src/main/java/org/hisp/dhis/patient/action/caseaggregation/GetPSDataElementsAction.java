package org.hisp.dhis.patient.action.caseaggregation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.program.ProgramStageDataElementService;
import org.hisp.dhis.program.ProgramStageService;

import com.opensymphony.xwork2.Action;

public class GetPSDataElementsAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ProgramStageService programStageService;

    public void setProgramStageService( ProgramStageService programStageService )
    {
        this.programStageService = programStageService;
    }

    private ProgramStageDataElementService programStageDataElementService;

    public void setProgramStageDataElementService( ProgramStageDataElementService programStageDataElementService )
    {
        this.programStageDataElementService = programStageDataElementService;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private List<String> optionComboNames;

    public List<String> getOptionComboNames()
    {
        return optionComboNames;
    }

    private List<String> optionComboIds;

    public List<String> getOptionComboIds()
    {
        return optionComboIds;
    }

    private List<String> optionComboType;

    public List<String> getOptionComboType()
    {
        return optionComboType;
    }

    private Integer psId;

    public void setPsId( Integer psId )
    {
        this.psId = psId;
    }

    private List<DataElement> dataElementList;

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
    {
        optionComboNames = new ArrayList<String>();

        optionComboIds = new ArrayList<String>();

        optionComboType = new ArrayList<String>();

        dataElementList = new ArrayList<DataElement>( programStageDataElementService
            .getListDataElement( programStageService.getProgramStage( psId ) ) );

        if ( dataElementList != null && !dataElementList.isEmpty() )
        {
            Iterator<DataElement> deIterator = dataElementList.iterator();

            while ( deIterator.hasNext() )
            {
                DataElement de = deIterator.next();

                if ( de.getType().equals( DataElement.VALUE_TYPE_STRING ) && de.isMultiDimensional() )
                {
                    optionComboIds.add( "[DE:" + psId + "." + de.getId() + ".0]" );

                    optionComboNames.add( de.getName() );

                    optionComboType.add( "1:" + de.getType() );

                    continue;
                }

                DataElementCategoryCombo dataElementCategoryCombo = de.getCategoryCombo();

                List<DataElementCategoryOptionCombo> optionCombos = new ArrayList<DataElementCategoryOptionCombo>(
                    dataElementCategoryCombo.getOptionCombos() );

                Iterator<DataElementCategoryOptionCombo> optionComboIterator = optionCombos.iterator();

                while ( optionComboIterator.hasNext() )
                {
                    DataElementCategoryOptionCombo decoc = optionComboIterator.next();

                    optionComboIds.add( "[" + CaseAggregationCondition.OBJECT_PROGRAM_STAGE_DATAELEMENT + ":" + psId
                        + "." + de.getId() + "." + decoc.getId() + "]" );

                    optionComboNames.add( de.getName() + " " + decoc.getName() );

                    if ( de.isMultiDimensional() )
                        optionComboType.add( "1:" + de.getType() );
                    else
                        optionComboType.add( "0:" + de.getType() );
                }
            }
        }

        return SUCCESS;
    }
}
