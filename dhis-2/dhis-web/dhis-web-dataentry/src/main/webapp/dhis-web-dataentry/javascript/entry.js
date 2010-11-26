
// -----------------------------------------------------------------------------
// Save
// -----------------------------------------------------------------------------

function saveVal( dataElementId, optionComboId )
{
	var zeroValueSaveMode = document.getElementById( 'zeroValueSaveMode' ).value;
	var dataElementName = document.getElementById( 'value[' + dataElementId + '].name' ).innerHTML;
	
	saveValue( dataElementId, optionComboId, dataElementName, zeroValueSaveMode );
}

function saveValue( dataElementId, optionComboId, dataElementName, zeroValueSaveMode )
{
    var field = document.getElementById( 'value[' + dataElementId + '].value' + ':' +  'value[' + optionComboId + '].value');
    var type = document.getElementById( 'value[' + dataElementId + '].type' ).innerHTML;   
	var organisationUnitId = getFieldValue( 'organisationUnitId' );
    
    field.style.backgroundColor = '#ffffcc';
    
    if ( field.value && field.value != '' )
    {
        if ( type == 'int' || type == 'number' || type == 'positiveNumber' || type == 'negativeNumber' )
        {
            if ( type == 'int' && !isInt( field.value ) )
            {
            	window.alert( i18n_value_must_integer + '\n\n' + dataElementName );
                return alertField( field );
            }  
            else if ( type == 'number' && !isNumber( field.value ) )
            {
                window.alert( i18n_value_must_number + '\n\n' + dataElementName );
                return alertField( field );
            } 
			else if ( type == 'positiveNumber' && !isPositiveNumber( field.value ) )
            {
                window.alert( i18n_value_must_positive_number + '\n\n' + dataElementName );
                return alertField( field );
            } 
			else if ( type == 'negativeNumber' && !isNegativeNumber( field.value ) )
            {
                window.alert( i18n_value_must_negative_number + '\n\n' + dataElementName );
                return alertField( field );
            }
            else if ( isZeroNumber( field.value ) && zeroValueSaveMode == false && significantZeros.indexOf( dataElementId ) == -1 )
            {
                // If value is 0, and zeroValueSaveMode is false, and zero is not significant for data element, then skip value
                
                field.style.backgroundColor = '#ccffcc';
                return;
            }
            else
            {
                var minString = document.getElementById( 'value[' + dataElementId + ':' + optionComboId + '].min' ).innerHTML;
                var maxString = document.getElementById( 'value[' + dataElementId + ':' + optionComboId + '].max' ).innerHTML;

                if ( minString.length != 0 && maxString.length != 0 )
                {
                    var value = new Number( field.value );
                    var min = new Number( minString );
                    var max = new Number( maxString );

                    if ( value < min )
                    {
                        var valueSaver = new ValueSaver( dataElementId, optionComboId, organisationUnitId, field.value, '#ffcccc' );
                        valueSaver.save();
                        
                        window.alert( i18n_value_of_data_element_less + '\n\n' + dataElementName );
                        
                        return;
                    }

                    if ( value > max )
                    {
                        var valueSaver = new ValueSaver( dataElementId, optionComboId, organisationUnitId, field.value, '#ffcccc' );
                        valueSaver.save();
                        
                        window.alert( i18n_value_of_data_element_greater + '\n\n' + dataElementName);
                        
                        return;
                    }
                }
            }       
        }
    }

    var valueSaver = new ValueSaver( dataElementId, optionComboId, organisationUnitId, field.value, '#ccffcc', '' );
    valueSaver.save();

    if ( type == 'int')
    {
    	calculateCDE(dataElementId);
    }
}

function saveBoolean( dataElementId, optionComboId, selectedOption  )
{	
	var select = selectedOption.options[selectedOption.selectedIndex].value 
	var organisationUnitId = getFieldValue( 'organisationUnitId' );
	
   	selectedOption.style.backgroundColor = '#ffffcc';     
    
    var valueSaver = new ValueSaver( dataElementId, optionComboId, organisationUnitId, select, '#ccffcc', selectedOption );
    valueSaver.save();
}

function saveDate( dataElementId, dataElementName )
{
	var field = document.getElementById( 'value[' + dataElementId + '].date' );
    var type = document.getElementById( 'value[' + dataElementId + '].valueType' ).innerHTML;
	var organisationUnitId = getFieldValue( 'organisationUnitId' );
    
    field.style.backgroundColor = '#ffffcc';
    
    var valueSaver = new ValueSaver( dataElementId, '', organisationUnitId, field.value, '#ccffcc', '' );
    valueSaver.save();
}

function saveComment( dataElementId, optionComboId, commentValue )
{
    var field = document.getElementById( 'value[' + dataElementId + ':' + optionComboId + '].comment' );                
    var select = document.getElementById( 'value[' + dataElementId + ':' + optionComboId + '].comments' );
	var organisationUnitId = getFieldValue( 'organisationUnitId' );
    
    field.style.backgroundColor = '#ffffcc';
    select.style.backgroundColor = '#ffffcc';
    
    var commentSaver = new CommentSaver( dataElementId, optionComboId, organisationUnitId, commentValue );
    commentSaver.save();
}

/**
 * Supportive method.
 */
function alertField( field )
{
	field.style.backgroundColor = '#ffcc00';
    field.select();
    field.focus();
    return false;
}

// -----------------------------------------------------------------------------
// Saver objects
// -----------------------------------------------------------------------------

function ValueSaver( dataElementId_, optionComboId_, organisationUnitId_, value_, resultColor_, selectedOption_ )
{
    var SUCCESS = '#ccffcc';
    var ERROR = '#ccccff';

    var dataElementId = dataElementId_;
    var optionComboId = optionComboId_;
    var value = value_;
    var resultColor = resultColor_;
    var selectedOption = selectedOption_;
    var organisationUnitId = organisationUnitId_;
    
    this.save = function()
    {
        var request = new Request();
        request.setCallbackSuccess( handleResponse );
        request.setCallbackError( handleHttpError );
        request.setResponseTypeXML( 'status' );        
        request.send( 'saveMultiDimensionalValue.action?organisationUnitId=' + organisationUnitId + '&dataElementId=' +
                dataElementId + '&optionComboId=' + optionComboId + '&value=' + value );
    };
    
    function handleResponse( rootElement )
    {
        var codeElement = rootElement.getElementsByTagName( 'code' )[0];
        var code = parseInt( codeElement.firstChild.nodeValue );
        
        if ( code == 0 )
        {
            markValue( resultColor );
        }
        else
        {
            markValue( ERROR );
            window.alert( i18n_saving_value_failed_status_code + '\n\n' + code );
        }
    }
    
    function handleHttpError( errorCode )
    {
        markValue( ERROR );
        window.alert( i18n_saving_value_failed_error_code + '\n\n' + errorCode );
    }   
    
    function markValue( color )
    {
        var type = document.getElementById( 'value[' + dataElementId + '].type' ).innerText;
        var element;
        
        if ( type == 'bool' )
        {
            element = document.getElementById( 'value[' + dataElementId + '].boolean' );
        }
        else if ( type == 'date' )
        {
        	element = document.getElementById( 'value[' + dataElementId + '].date' );
        }
        else if ( selectedOption )
        {
        	element = selectedOption;    
        }
        else
        {            
            element = document.getElementById( 'value[' + dataElementId + '].value' + ':' +  'value[' + optionComboId + '].value');            
        }

        element.style.backgroundColor = color;
    }
}

// -----------------------------------------------------------------------------
// Section
// -----------------------------------------------------------------------------

function openCloseSection( sectionId )
{
	var divSection = document.getElementById( sectionId );
	var sectionLabel = document.getElementById( sectionId + ":name" );	
	
	if( divSection.style.display == 'none' )
	{			
		divSection.style.display = ('block');
		sectionLabel.style.textAlign = 'center';
	}
	else
	{			
		divSection.style.display = ('none');
		sectionLabel.style.textAlign = 'left';
	}
}

// -----------------------------------------------------------------------------
// CalculatedDataElements
// -----------------------------------------------------------------------------

/**
 * Calculate and display the value of any CDE the given data element is a part of.
 * @param dataElementId  id of the data element to calculate a CDE for
 */
function calculateCDE( dataElementId )
{
    var cdeId = getCalculatedDataElement(dataElementId);
  
    if ( ! cdeId )
    {
  	    return;
    }
    
    var factorMap = calculatedDataElementMap[cdeId];
    var value = 0;
    var dataElementValue;
    
    for ( dataElementId in factorMap )
    {
    	dataElementValue = document.getElementById( 'value[' + dataElementId + '].value' ).value;
    	value += ( dataElementValue * factorMap[dataElementId] );
    }
    
    document.getElementById( 'value[' + cdeId + '].value' ).value = value;
}

/**
 * Returns the id of the CalculatedDataElement this DataElement id is a part of.
 * @param dataElementId id of the DataElement
 * @return id of the CalculatedDataElement this DataElement id is a part of,
 *     or null if the DataElement id is not part of any CalculatedDataElement
 */
function getCalculatedDataElement( dataElementId )
{
    for ( cdeId in calculatedDataElementMap )
    {
  	    var factorMap = calculatedDataElementMap[cdeId];

  	    if ( deId in factorMap )
  	    {
  	    	return cdeId;
  	    }
    }

    return null;
}

function calculateAndSaveCDEs()
{
	lockScreen();

    var request = new Request();
    request.setCallbackSuccess( dataValuesReceived );
    request.setResponseTypeXML( 'dataValues' );
    request.send( 'calculateCDEs.action' );
}

function dataValuesReceived( node )
{
	var values = node.getElementsByTagName('dataValue');
    var dataElementId;
    var value;

	for ( var i = 0, value; value = values[i]; i++ )
	{
		dataElementId = value.getAttribute('dataElementId');
		value = value.firstChild.nodeValue;		
		document.getElementById( 'value[' + dataElementId + '].value' ).value = value;
	}
	
	unLockScreen();
	
	setHeaderDelayMessage(i18n_save_calculated_data_element_success);
}
