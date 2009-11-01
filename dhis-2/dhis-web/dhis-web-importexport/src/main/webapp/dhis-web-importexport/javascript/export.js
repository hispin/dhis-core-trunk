
// -----------------------------------------------------------------------------
// DataMartExport
// -----------------------------------------------------------------------------

function exportDataValue()
{
    if ( validateDataValueExportForm() )
    {
        var aggregatedData = getListValue( "aggregatedData" );
        
        if ( aggregatedData == "true" )
        {
            var request = new Request();
            request.setResponseTypeXML( 'message' );
            request.setCallbackSuccess( validateAggregatedExportCompleted );
            request.send( "validateAggregatedExport.action" );
        }
        else
        {
            submitDataValueExportForm();
        }
    }
}

function validateAggregatedExportCompleted( messageElement )
{
    var type = messageElement.getAttribute( 'type' );
    var message = messageElement.firstChild.nodeValue;
    
    if ( type == 'success' )
    {
        var generateDataSource = getListValue( "generateDataSource" );
        
        if ( generateDataSource && generateDataSource == "true" )
        {
            var request = new Request();
            request.sendAsPost( getDataMartExportParams() );
            request.setCallbackSuccess( exportDataMartReceived );
            request.send( "exportDataMart.action" );   
        }
        else
        {
            submitDataValueExportForm();
        }
    }
    else if ( type == 'error' )
    {
        document.getElementById( 'message' ).innerHTML = message;
        document.getElementById( 'message' ).style.display = 'block';
    }
}

function exportDataMartReceived( messageElement )
{
    getExportStatus();
}

function getExportStatus()
{
    var url = "getExportStatus.action";
    
    var request = new Request();
    request.setResponseTypeXML( "status" );
    request.setCallbackSuccess( exportStatusReceived );    
    request.send( url );
}

function exportStatusReceived( xmlObject )
{
    var statusMessage = getElementValue( xmlObject, "statusMessage" );
    var finished = getElementValue( xmlObject, "finished" );
    
    if ( finished == "true" )
    {        
        submitDataValueExportForm();
    }
    else
    {
        setMessage( statusMessage );
        
        setTimeout( "getExportStatus();", 2000 );
    }
}

// -----------------------------------------------------------------------------
// Supportive methods
// -----------------------------------------------------------------------------

function getDataMartExportParams()
{
    var params = getParamString( "selectedDataSets" );
    
    params += "startDate=" + document.getElementById( "startDate" ).value + "&";
    params += "endDate=" + document.getElementById( "endDate" ).value + "&";
    params += "dataSourceLevel=" + getListValue( "dataSourceLevel" );
    
    return params;
}

// -----------------------------------------------------------------------------
// Export
// -----------------------------------------------------------------------------

function submitDataValueExportForm()
{
    selectAll( document.getElementById( "selectedDataSets" ) );
	
	if ( validateDataValueExportForm() )
	{
	   document.getElementById( "exportForm" ).submit();
	}
}

function setDataType()
{
    var aggregatedData = getListValue( "aggregatedData" );
  
    if ( aggregatedData == "true" )
    {
        showById( "aggregatedDataDiv" );
        hideById( "regularDataDiv" );
    }
    else
    {
        hideById( "aggregatedDataDiv" );
        showById( "regularDataDiv" );
    }
}

// -----------------------------------------------------------------------------
// MetaDataExport
// -----------------------------------------------------------------------------

function submitMetaDataExportForm()
{
    if ( validateMetaDataExportForm() )
    {
       document.getElementById( "exportForm" ).submit();
    }
}

function toggle( knob )
{
    var toggle = false;
	
    if ( knob == "all" )
    {
        toggle = true;
    }
	
    document.getElementById( "dataElements" ).checked = toggle;
    document.getElementById( "dataElementGroups" ).checked = toggle;
    document.getElementById( "dataElementGroupSets" ).checked = toggle;
    document.getElementById( "dataSets" ).checked = toggle;
    document.getElementById( "indicators" ).checked = toggle;
    document.getElementById( "indicatorGroups" ).checked = toggle;
    document.getElementById( "indicatorGroupSets" ).checked = toggle;
    document.getElementById( "dataDictionaries" ).checked = toggle;
    document.getElementById( "organisationUnits" ).checked = toggle;
    document.getElementById( "organisationUnitGroups" ).checked = toggle;
    document.getElementById( "organisationUnitGroupSets" ).checked = toggle;
    document.getElementById( "organisationUnitLevels" ).checked = toggle;
    document.getElementById( "validationRules" ).checked = toggle;	
    document.getElementById( "reportTables" ).checked = toggle; 
    document.getElementById( "olapUrls" ).checked = toggle;      
}

// -----------------------------------------------------------------------------
// Validation
// -----------------------------------------------------------------------------

function validateMetaDataExportForm()
{
    if ( !isChecked( "dataElements" ) &&
         !isChecked( "dataElementGroups" ) &&
         !isChecked( "dataElementGroupSets" ) &&
         !isChecked( "dataSets" ) &&
         !isChecked( "indicators" ) &&
         !isChecked( "indicatorGroups" ) &&
         !isChecked( "indicatorGroupSets" ) &&
         !isChecked( "dataDictionaries" ) &&
         !isChecked( "organisationUnits" ) &&
         !isChecked( "organisationUnitGroups" ) &&
         !isChecked( "organisationUnitGroupSets" ) &&
         !isChecked( "organisationUnitLevels" ) &&
         !isChecked( "validationRules" ) &&
         !isChecked( "reportTables" ) &&
         !isChecked( "olapUrls" ) )
     {
         setMessage( i18n_select_one_or_more_object_types );
         return false;
     }
     
     hideMessage();
     return true;
}

function validateDataValueExportForm()
{    
    if ( selectedOrganisationUnitIds == null || selectedOrganisationUnitIds.length == 0 )
    {
        setMessage( i18n_select_organisation_unit );
        return false;
    }
    if ( !hasText( "startDate" ) )
    {
        setMessage( i18n_select_startdate );
        return false;
    }
    if ( !hasText( "endDate" ) )
    {
        setMessage( i18n_select_enddate );
        return false;
    }
    if ( !hasElements( "selectedDataSets" ) )
    {
        setMessage( i18n_select_datasets );
        return false;
    }
    
    hideMessage();
    return true;
}

var selectedOrganisationUnitIds = null;

function setSelectedOrganisationUnitIds( ids )
{
    selectedOrganisationUnitIds = ids;
}

selectionTreeSelection.setListenerFunction( setSelectedOrganisationUnitIds );

