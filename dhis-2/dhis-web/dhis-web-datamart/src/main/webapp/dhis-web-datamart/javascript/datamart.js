
// -----------------------------------------------------------------------------
// Data retrieval methods
// -----------------------------------------------------------------------------

function getDataElements()
{
	var dataElementGroupList = document.getElementById( "dataElementGroupId" );
	var dataElementGroupId = dataElementGroupList.options[ dataElementGroupList.selectedIndex ].value;
	
	if ( dataElementGroupId != null )
	{
		var url = "../dhis-web-commons-ajax/getDataElements.action?id=" + dataElementGroupId + "&aggregate=true";
				
		var request = new Request();
	    request.setResponseTypeXML( 'dataElement' );
	    request.setCallbackSuccess( getDataElementsReceived );
	    request.send( url );
	}
}

function getIndicators()
{
	var indicatorGroupList = document.getElementById( "indicatorGroupId" );
	var indicatorGroupId = indicatorGroupList.options[ indicatorGroupList.selectedIndex ].value;
	
	if ( indicatorGroupId != null )
	{
		var url = "../dhis-web-commons-ajax/getIndicators.action?id=" + indicatorGroupId;
		
		var request = new Request();
	    request.setResponseTypeXML( 'indicator' );
	    request.setCallbackSuccess( getIndicatorsReceived );
	    request.send( url );	    
	}
}

function getOrganisationUnits()
{
	var organisationUnitLevelList = document.getElementById( "organisationUnitLevel" );
	var organisationUnitLevel = organisationUnitLevelList.options[ organisationUnitLevelList.selectedIndex ].value;
	
	if ( organisationUnitLevel != null )
	{
		var url = "../dhis-web-commons-ajax/getOrganisationUnits.action?level=" + organisationUnitLevel;
		
		var request = new Request();
	    request.setResponseTypeXML( 'organisationUnit' );
	    request.setCallbackSuccess( getOrganisationUnitsReceived );
	    request.send( url );	    
	}
}

function getOrganisationUnitChildren()
{
	var organisationUnitList = document.getElementById( "availableOrganisationUnits" );
	var organisationUnitId = organisationUnitList.options[ organisationUnitList.selectedIndex ].value;
	
	if ( organisationUnitId != null )
	{
		var url = "../dhis-web-commons-ajax/getOrganisationUnitChildren.action?id=" + organisationUnitId;
		
		var request = new Request();
	    request.setResponseTypeXML( 'organisationUnit' );
	    request.setCallbackSuccess( getOrganisationUnitChildrenReceived );
	    request.send( url );	
	}
}

function getDataElementsReceived( xmlObject )
{	
	var availableDataElements = document.getElementById( "availableDataElements" );
	var selectedDataElements = document.getElementById( "selectedDataElements" );
	
	clearList( availableDataElements );
	
	var dataElements = xmlObject.getElementsByTagName( "dataElement" );
	
	for ( var i = 0; i < dataElements.length; i++ )
	{
		var id = dataElements[ i ].getElementsByTagName( "id" )[0].firstChild.nodeValue;
		var dataElementName = dataElements[ i ].getElementsByTagName( "name" )[0].firstChild.nodeValue;
		
		if ( listContains( selectedDataElements, id ) == false )
		{				
			var option = document.createElement( "option" );
			option.value = id;
			option.text = dataElementName;
			availableDataElements.add( option, null );
		}
	}
}

function getIndicatorsReceived( xmlObject )
{	
	var availableIndicators = document.getElementById( "availableIndicators" );
	var selectedIndicators = document.getElementById( "selectedIndicators" );
	
	clearList( availableIndicators );
	
	var indicators = xmlObject.getElementsByTagName( "indicator" );
	
	for ( var i = 0; i < indicators.length; i++ )
	{
		var id = indicators[ i ].getElementsByTagName( "id" )[0].firstChild.nodeValue;
		var indicatorName = indicators[ i ].getElementsByTagName( "name" )[0].firstChild.nodeValue;
		
		if ( listContains( selectedIndicators, id ) == false )
		{				
			var option = document.createElement( "option" );
			option.value = id;
			option.text = indicatorName;
			availableIndicators.add( option, null );
		}
	}
}

function getOrganisationUnitsReceived( xmlObject )
{	
	var availableOrganisationUnits = document.getElementById( "availableOrganisationUnits" );
	var selectedOrganisationUnits = document.getElementById( "selectedOrganisationUnits" );
	
	clearList( availableOrganisationUnits );
	
	var organisationUnits = xmlObject.getElementsByTagName( "organisationUnit" );
	
	for ( var i = 0; i < organisationUnits.length; i++ )
	{
		var id = organisationUnits[ i ].getElementsByTagName( "id" )[0].firstChild.nodeValue;
		var organisationUnitName = organisationUnits[ i ].getElementsByTagName( "name" )[0].firstChild.nodeValue;
		
		if ( listContains( selectedOrganisationUnits, id ) == false )
		{						
			var option = document.createElement( "option" );
			option.value = id;
			option.text = organisationUnitName;
			availableOrganisationUnits.add( option, null );
		}
	}
}

function getOrganisationUnitChildrenReceived( xmlObject )
{
	var selectedOrganisationUnits = document.getElementById( "selectedOrganisationUnits" );
	
	var organisationUnits = xmlObject.getElementsByTagName( "organisationUnit" );
	
	for ( var i = 0; i < organisationUnits.length; i++ )
	{
		var id = organisationUnits[ i ].getElementsByTagName( "id" )[0].firstChild.nodeValue;
		
		var organisationUnitName = organisationUnits[ i ].getElementsByTagName( "name" )[0].firstChild.nodeValue;
		
		if ( listContains( selectedOrganisationUnits, id ) == false )
		{
			var option = document.createElement( "option" );
			option.value = id;
			option.text = organisationUnitName;
			selectedOrganisationUnits.add( option, null );
		}
	}
}

// -----------------------------------------------------------------------------
// DataMartExport details
// -----------------------------------------------------------------------------

function showDataMartExportDetails( id )
{
    var request = new Request();
    request.setResponseTypeXML( 'dataMartExport' );
    request.setCallbackSuccess( dataMartExportReceived );
    request.send( 'getDataMartExport.action?id=' + id );
}

function dataMartExportReceived( xmlObject )
{
    setInnerHTML( "nameField", getElementValue( xmlObject, "name" ) );
    setInnerHTML( "dataElementField", getElementValue( xmlObject, "dataElements" ) );
    setInnerHTML( "indicatorField", getElementValue( xmlObject, "indicators" ) );
    setInnerHTML( "organisationUnitField", getElementValue( xmlObject, "organisationUnits" ) );
    setInnerHTML( "periodField", getElementValue( xmlObject, "periods" ) );
    
    showDetails();
}

// -----------------------------------------------------------------------------
// DatmartExport
// -----------------------------------------------------------------------------

function saveExport()
{
    if ( validateCollections() )
    {
        var url = "validateDataMartExport.action?id=" + 
            getFieldValue( "id" ) + "&name=" + getFieldValue( "name" );
        
        var request = new Request();
        request.setResponseTypeXML( 'message' );
        request.setCallbackSuccess( saveExportReceived );
        request.send( url );
    }
}

function saveExportReceived( messageElement )
{
    var type = messageElement.getAttribute( 'type' );
    var message = messageElement.firstChild.nodeValue;
    
    if ( type == "input" )
    {
        setMessage( message );
    }
    else if ( type == "success" )
    {
    	selectAllById( "selectedDataElements" );
    	selectAllById( "selectedIndicators" );
    	selectAllById( "selectedOrganisationUnits" );
    	selectAllById( "selectedPeriods" );
    	
    	document.getElementById( "exportForm" ).submit();
    }
}

// -----------------------------------------------------------------------------
// Export
// -----------------------------------------------------------------------------

function getExportStatus()
{
    var url = "getExportStatus.action";
	
	var request = new Request();
    request.setResponseTypeXML( 'status' );
    request.setCallbackSuccess( exportStatusReceived );
    request.send( url );
}

function exportStatusReceived( xmlObject )
{
    var message = getElementValue( xmlObject, "message" );
    var running = getElementValue( xmlObject, "running" );
        
    if ( running == "true" )
    {
        setWaitMessage( message );
	    waitAndGetExportStatus( 2000 );
    }
    else
    {
    	setMessage( message );
    }
}

function waitAndGetExportStatus( millis )
{
	setTimeout( "getExportStatus();", millis );
}

function cancelExport()
{
	var url = "cancelExport.action";
		
	var request = new Request();  
    request.send( url );
}

function removeDatamartExport( exportId, exportName )
{
    var result = window.confirm( i18n_confirm_delete + '\n\n' + exportName );
    
    if ( result )
    {
        window.location.href = 'removeDataMartExport.action?id=' + exportId;
    }
}

function getQueryStringFromList( listId, paramName )
{
	var list = document.getElementById( listId );
	
	var params = "";
	
	for ( var i = 0; i < list.options.length; i++ )
	{
		params += paramName + "=" + list.options[i].value + "&";
	}
	
	return params;
}

// -----------------------------------------------------------------------------
// Validation
// -----------------------------------------------------------------------------

function validateCollections()
{
    if ( !hasElements( "selectedDataElements" ) && !hasElements( "selectedIndicators" ) )
    {
        setMessage( i18n_must_select_at_least_one_dataelement_or_indicator );
        
        return false;
    }
    
    if ( !hasElements( "selectedOrganisationUnits" ) )
    {
        setMessage( i18n_must_select_at_least_one_organisation_unit );
        
        return false;
    }
    
    if ( !hasElements( "selectedPeriods" ) && relativePeriodsChecked() == false )
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
         isChecked( "last12Months" ) == true ||
         isChecked( "last3To6Months" ) == true ||
         isChecked( "last6To9Months" ) == true ||
         isChecked( "last9To12Months" ) == true ||
         isChecked( "last12IndividualMonths" ) == true ||
         isChecked( "soFarThisYear" ) == true ||
         isChecked( "individualMonthsThisYear" ) == true ||
         isChecked( "individualQuartersThisYear" ) == true )
    {
        return true;
    }
    
    return false;
}

// -----------------------------------------------------------------------------
// Dashboard
// -----------------------------------------------------------------------------

function addToDashboard( id )
{
    var dialog = window.confirm( i18n_confirm_add_to_dashboard );
    
    if ( dialog )
    {
        var request = new Request(); 
        request.send( "addDataMartExportToDashboard.action?id=" + id );
    }
}
