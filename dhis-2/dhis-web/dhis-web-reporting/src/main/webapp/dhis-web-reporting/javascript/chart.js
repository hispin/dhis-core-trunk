
// -----------------------------------------------------------------------------
// View chart
// -----------------------------------------------------------------------------

var tempChartId;

function runAndViewChart( chartId )
{
	setHeaderWaitMessage( "Please wait" );
	
    tempChartId = chartId;
    
    var request = new Request();
    request.setCallbackSuccess( runAndViewChartReceived );    
    request.send( "createChart.action?id=" + chartId );
}

function runAndViewChartReceived( messageElement )
{
    getChartStatus();
}

function getChartStatus()
{
    var url = "getStatus.action";
    
    var request = new Request();
    request.setResponseTypeXML( "status" );
    request.setCallbackSuccess( chartStatusReceived );    
    request.send( url );
}

function chartStatusReceived( xmlObject )
{
    var statusMessage = getElementValue( xmlObject, "statusMessage" );
    var finished = getElementValue( xmlObject, "finished" );
    
    updateHeaderWaitMessage( statusMessage );
    
    if ( finished == "true" )
    {
    	hideHeaderMessage();
    	
        var url = "viewChart.action?id=" + tempChartId;
        
        viewChart( url );
    }
    else
    {
        setTimeout( "getChartStatus();", 2000 );
    }
}

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
    setFieldValue( 'titleField', getElementValue( xmlObject, 'title' ) );
    setFieldValue( 'dimensionField', getElementValue( xmlObject, 'dimension' ) );
    setFieldValue( 'indicatorsField', getElementValue( xmlObject, 'indicators' ) );
    setFieldValue( 'periodsField', getElementValue( xmlObject, 'periods' ) );
    setFieldValue( 'organisationUnitsField', getElementValue( xmlObject, 'organisationUnits' ) );
    
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
        
        if ( dimension == "period" )
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
    
    if ( !hasElements( "selectedPeriods" ) ) //&& !relativePeriodsChecked() )
    {
        setMessage( i18n_must_select_at_least_one_period );
        
        return false;
    }
    
    return true;
}

function relativePeriodsChecked()
{
    if ( isChecked( "reportingMonth" ) == true ||
         isChecked( "last3Months" ) == true ||
         isChecked( "last6Months" ) == true ||
         isChecked( "last9Months" ) == true ||
         isChecked( "last12Months" ) == true ||
         isChecked( "last3To6Months" ) == true ||
         isChecked( "last6To9Months" ) == true ||
         isChecked( "last9To12Months" ) == true ||
         isChecked( "last12IndividualMonths" ) == true ||
         isChecked( "soFarThisYear" ) == true ||
         isChecked( "soFarThisFinancialYear" ) == true ||
         isChecked( "individualMonthsThisYear" ) == true ||
         isChecked( "individualQuartersThisYear" ) == true )
    {
        return true;
    }
    
    return false;
}
