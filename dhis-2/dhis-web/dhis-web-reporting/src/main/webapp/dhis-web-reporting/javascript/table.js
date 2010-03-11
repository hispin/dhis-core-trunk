
// -----------------------------------------------------------------------------
// Create ReportTable
// -----------------------------------------------------------------------------

function createTable( tableId )
{
    var url = "createTable.action?id=" + tableId + "&mode=table";
    
    if ( document.getElementById( "reportingPeriod" ) != null )
    {
        url += "&reportingPeriod=" + getListValue( "reportingPeriod" );
    }
    
    if ( document.getElementById( "parentOrganisationUnitId" ) != null )
    {
        url += "&parentOrganisationUnitId=" + getListValue( "parentOrganisationUnitId" );
    }
    
    if ( document.getElementById( "organisationUnitId" ) != null )
    {
        url += "&organisationUnitId=" + getListValue( "organisationUnitId" );
    }
    
    var request = new Request();
    request.setCallbackSuccess( createTableReceived );    
    request.send( url );
}

function createTableReceived( messageElement )
{
    getTableStatus();
}

function getTableStatus()
{
    var url = "getStatus.action";
    
    var request = new Request();
    request.setResponseTypeXML( 'status' );
    request.setCallbackSuccess( tableStatusReceived );    
    request.send( url );
}

function tableStatusReceived( xmlObject )
{
    var statusMessage = getElementValue( xmlObject, 'statusMessage' );
    var finished = getElementValue( xmlObject, 'finished' );
    
    if ( finished == "true" )
    {
        setMessage( i18n_process_completed );
    }
    else if ( statusMessage == null )
    {
        setWaitMessage( i18n_please_wait );
    }
    else
    {
        setWaitMessage( i18n_please_wait + ". " + statusMessage + "..."  );
    }
    
    waitAndGetTableStatus( 2000 );
}

function waitAndGetTableStatus( millis )
{
    setTimeout( "getTableStatus();", millis );
}

// -----------------------------------------------------------------------------
// Save ReportTable
// -----------------------------------------------------------------------------

function saveTable()
{
    if ( validateCollections() )
    {
        var url = "validateTable.action?id=" + getFieldValue( "tableId" ) + "&name=" + getFieldValue( "tableName" );
        
        var request = new Request();
        request.setResponseTypeXML( 'message' );
        request.setCallbackSuccess( saveTableReceived );
        request.send( url );
    }
}

function saveTableReceived( messageElement )
{
	var type = messageElement.getAttribute( 'type' );
    var message = messageElement.firstChild.nodeValue;
    
    if ( type == "input" )
    {
        setMessage( message );
        
        return false;
    }
    else if ( type == "success" )
    {        
        selectTableForm();
        
        document.getElementById( "tableForm" ).submit();
    }
}

function selectTableForm()
{
    if ( isNotNull( "selectedDataElements" ) )
    {
        selectAllById( "selectedDataElements" );
    }
    
    if ( isNotNull( "selectedIndicators" ) )
    {
       selectAllById( "selectedIndicators" );
    }
        
    if ( isNotNull( "selectedDataSets" ) )
    {
        selectAllById( "selectedDataSets" );
    }
    
    selectAllById( "selectedPeriods" );
    selectAllById( "selectedOrganisationUnits" );   
}

// -----------------------------------------------------------------------------
// Remove
// -----------------------------------------------------------------------------

function removeTable( tableId, tableName )
{
	removeItem( tableId, tableName, i18n_confirm_delete, "removeTable.action" );
}

// -----------------------------------------------------------------------------
// Validation
// -----------------------------------------------------------------------------

function validateCollections()
{
    if ( isChecked( "regression" ) && document.getElementById( "selectedOrganisationUnits" ).options.length > 1 )
    {
        setMessage( i18n_cannot_include_more_organisation_unit_regression );
        
        return false;
    }
    
    if ( isChecked( "doIndicators" ) && isChecked( "doPeriods" ) && isChecked( "doOrganisationUnits" ) )
    {
        setMessage( i18n_cannot_crosstab_all_dimensions );
        
        return false;
    }
    
    if ( !isTrue( "dimension" ) && !isChecked( "doIndicators" ) && !isChecked( "doPeriods" ) && !isChecked( "doOrganisationUnits" ) )
    {
        setMessage( i18n_cannot_crosstab_no_dimensions );
        
        return false;
    }
    
    if ( isNotNull( "selectedDataElements" ) && !hasElements( "selectedDataElements" ) )
    {
        setMessage( i18n_must_select_at_least_one_dataelement );
        
        return false;
    }
    
    if ( isNotNull( "selectedIndicators" ) && !hasElements( "selectedIndicators" ) )
    {
        setMessage( i18n_must_select_at_least_one_indicator );
        
        return false;
    }
    
    if ( !hasElements( "selectedOrganisationUnits" ) && !organisationUnitReportParamsChecked() )
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

function isTrue( elementId )
{
    var value = document.getElementById( elementId ).value;
    
    return value && value == "true" ? true : false;
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

function organisationUnitReportParamsChecked()
{
    if ( isChecked( "paramParentOrganisationUnit" ) == true ||
         isChecked( "paramOrganisationUnit" ) == true )
    {
        return true;
    }
    
    return false;
}

// -----------------------------------------------------------------------------
// Details
// -----------------------------------------------------------------------------

function showTableDetails( tableId )
{
	var request = new Request();
    request.setResponseTypeXML( 'reportTable' );
    request.setCallbackSuccess( tableReceived );
    request.send( 'getTable.action?id=' + tableId );	
}

function tableReceived( xmlObject )
{
	setFieldValue( 'nameField', getElementValue( xmlObject, 'name' ) );
	setFieldValue( 'tableNameField', getElementValue( xmlObject, 'tableName' ) );
	setFieldValue( 'indicatorsField', getElementValue( xmlObject, 'indicators' ) );
	setFieldValue( 'periodsField', getElementValue( xmlObject, 'periods' ) );
	setFieldValue( 'unitsField', getElementValue( xmlObject, 'units' ) );
	setFieldValue( 'doIndicatorsField', parseBool( getElementValue( xmlObject, 'doIndicators' ) ) );
	setFieldValue( 'doPeriodsField', parseBool( getElementValue( xmlObject, 'doPeriods' ) ) );
	setFieldValue( 'doUnitsField', parseBool( getElementValue( xmlObject, 'doUnits' ) ) );
	
	showDetails();
}

function parseBool( bool )
{
	return ( bool == "true" ) ? i18n_yes : i18n_no;
}

// -----------------------------------------------------------------------------
// Regression
// -----------------------------------------------------------------------------

function toggleRegression()
{
    if ( document.getElementById( "regression" ).checked )
    {
        check( "doIndicators" );
        uncheck( "doOrganisationUnits" );
        uncheck( "doPeriods" );
        
        disable( "doOrganisationUnits" );
        disable( "doPeriods" );
    }
    else
    {
        enable( "doOrganisationUnits" );
        enable( "doPeriods" );
    }
}

// -----------------------------------------------------------------------------
// Dashboard
// -----------------------------------------------------------------------------

function addReportTableToDashboard( id )
{
    var dialog = window.confirm( i18n_confirm_add_report_table_to_dashboard );
    
    if ( dialog )
    {
        var request = new Request(); 
        request.send( "addReportTableToDashboard.action?id=" + id );
    }
}

// -----------------------------------------------------------------------------
// Display
// -----------------------------------------------------------------------------

function saveDisplayTable()
{
    var params = "id=" + document.getElementById( "reportTableId" ).value + "&";
    
    var table = document.getElementById( "columnTable" );
    
    var inputs = table.getElementsByTagName( "input" );
    
    for ( var i = 0; i < inputs.length; i+=2 )
    {
        var column = inputs[i].id;
        var header = inputs[i].value;
        var hidden = !( inputs[i+1].checked );
        
        params += "column=" + column + "-" + header + "-" + hidden + "&";
    }
    
    var request = new Request();
    request.setResponseTypeXML( 'message' );
    request.setCallbackSuccess( saveDisplayTableReceived );
    request.sendAsPost( params );
    request.send( "saveDisplayTable.action" );
}

function saveDisplayTableReceived( messageElement )
{
    window.location.href = 'displayManageTableForm.action';
}

// -----------------------------------------------------------------------------
// Export
// -----------------------------------------------------------------------------

function exportToExcel( id )
{	
	verifyAndExport( id, "generateTableDataWorkbook.action?id=" + id );
}

function exportToHtml( id )
{
	verifyAndExport( id, "getTableData.action?id=" + id );
}

function exportToCsv( id )
{
	verifyAndExport( id, "getTableDataExport.action?exportFormat=CSV&id=" + id );
}

function verifyAndExport( id, url )
{   
    $.getJSON( 
       "verifyReportTable.action",
       { "id": id },
       function( json )
       {
           if ( json.response == "error" )
           {
               setHeaderDelayMessage( i18n_generate_before_export );
           }
           else
           {
               window.location.href = "generateTableDataWorkbook.action?id=" + id;
           }
       } );
}
