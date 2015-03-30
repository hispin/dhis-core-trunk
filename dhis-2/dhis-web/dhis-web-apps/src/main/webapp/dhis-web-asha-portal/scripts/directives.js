/* global angular, dhis2 */

'use strict';

/* Directives */

var trackerCaptureDirectives = angular.module('trackerCaptureDirectives', [])

.directive('d2BenenficiaryRegistration', function() {
    
    return {
        require: 'ngModel',
        restrict: 'A',
        link: function (scope, element, attrs, ctrl) {
            
            var fieldName = attrs.inputFieldId;
            var numberType = attrs.numberType;
            var isRequired = attrs.ngRequired === 'true';
            var msg = $translate(numberType)+ ' ' + $translate('required');
            
            /*function checkValidity(numberType, value){
                var isValid = false;
                switch(numberType){
                    case "number":
                        isValid = dhis2.validation.isNumber(value);
                        break;
                    case "posInt":
                        isValid = dhis2.validation.isPositiveInt(value);
                        break;
                    case "negInt":
                        isValid = dhis2.validation.isNegativeInt(value);
                        break;
                    case "zeroPositiveInt":
                        isValid = dhis2.validation.isZeroOrPositiveInt(value);
                        break;
                    case "int":
                        isValid = dhis2.validation.isInt(value);
                        break;
                    default:
                        isValid = true;
                }
                return isValid;
            }
            
            var errorMessages = ErrorMessageService.getErrorMessages();
            var fieldName = attrs.inputFieldId;
            var numberType = attrs.numberType;
            var isRequired = attrs.ngRequired === 'true';
            var msg = $translate(numberType)+ ' ' + $translate('required');
           
            ctrl.$parsers.unshift(function(value) {
            	if(value){
                    var isValid = checkValidity(numberType, value);                    
                    if(!isValid){
                        errorMessages[fieldName] = $translate('value_must_be_' + numberType);
                    }
                    else{
                        if(isRequired){
                            errorMessages[fieldName] = msg;
                        }
                        else{
                            errorMessages[fieldName] = "";
                        }
                    }
                    
                    ErrorMessageService.setErrorMessages(errorMessages);
                	ctrl.$setValidity(fieldName, isValid);
                    return value;
                }
                
                if(value === ''){
                    if(isRequired){
                        errorMessages[fieldName] = msg;
                    }
                    else{
                        ctrl.$setValidity(fieldName, true);
                        errorMessages[fieldName] = "";
                    }
                    
                    ErrorMessageService.setErrorMessages(errorMessages);
                    return undefined;
                }              
            });
           
            ctrl.$formatters.unshift(function(value) {                
                if(value){
                    var isValid = checkValidity(numberType, value);
                    ctrl.$setValidity(fieldName, isValid);
                    return value;
                }
            });*/
        }
    };
});