
// -----------------------------------------------------------------------------
// View chart
// -----------------------------------------------------------------------------

function viewChart( url )
{
    window.open( url, "_blank", "directories=no, height=560, width=760, location=no, menubar=no, status=no, toolbar=no, resizable=yes, scrollbars=yes" );
}

// -----------------------------------------------------------------------------
// Remove chart
// -----------------------------------------------------------------------------

function removeChart( chartId, chartTitle )
{
	removeItem( chartId, chartTitle, i18n_confirm_delete, "removeChart.action" );
}

// -----------------------------------------------------------------------------
// Show chart details
// -----------------------------------------------------------------------------

function showChartDetails( chartId )
{
    var request = new Request();
    request.setResponseTypeXML( 'chart' );
    request.setCallbackSuccess( chartReceived );
    request.send( 'getChart.action?id=' + chartId );
}

function chartReceived( xmlObject )
{
    setInnerHTML( 'titleField', getElementValue( xmlObject, 'title' ) );
    setInnerHTML( 'dimensionField', getElementValue( xmlObject, 'dimension' ) );
    setInnerHTML( 'indicatorsField', getElementValue( xmlObject, 'indicators' ) );
    setInnerHTML( 'periodsField', getElementValue( xmlObject, 'periods' ) );
    setInnerHTML( 'organisationUnitsField', getElementValue( xmlObject, 'organisationUnits' ) );
    
    showDetails();
}

// -----------------------------------------------------------------------------
// Validate and save
// -----------------------------------------------------------------------------

function saveChart()
{
    if ( validateCollections() )
    {
        var url = "validateChart.action?id=" + getFieldValue( "id" ) + "&title=" + getFieldValue( "title" );

        var request = new Request();
        request.setResponseTypeXML( 'message' );
        request.setCallbackSuccess( saveChartReceived );
        request.send( url );
    }
}

function saveChartReceived( messageElement )
{
    var type = messageElement.getAttribute( 'type' );
    var message = messageElement.firstChild.nodeValue;
    var dimension = document.getElementById( "dimension" ).value;

    if ( type == "input" )
    {
        setMessage( message );
        
        return false;
    }
    else if ( type == "success" )
    {
        selectAllById( "selectedIndicators" );
        
        if ( dimension == "period" || dimension == "indicator")
        {
            selectAllById( "selectedPeriods" );
        }
        else if ( dimension == "organisationUnit" )
        {        
            selectAllById( "selectedOrganisationUnits" );
        }
        
        document.getElementById( "chartForm" ).submit();
    }
}

function validateCollections()
{
    if ( !hasElements( "selectedIndicators" ) )
    {
        setMessage( i18n_must_select_at_least_one_indicator );
        
        return false;
    }
    
    if ( !hasElements( "selectedOrganisationUnits" ) )
    {
        setMessage( i18n_must_select_at_least_one_unit );
        
        return false;
    }
    
    if ( !hasElements( "selectedPeriods" ) && !relativePeriodsChecked() )
    {
        setMessage( i18n_must_select_at_least_one_period );
        
        return false;
    }
    
    return true;
}
