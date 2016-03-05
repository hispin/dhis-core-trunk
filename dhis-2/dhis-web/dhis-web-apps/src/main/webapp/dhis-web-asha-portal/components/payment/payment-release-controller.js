/* global trackerCapture, angular */

trackerCapture.controller('PaymentReleaseController',
        function($scope,
                $filter,
                $translate,
                ModalService,
                EventUtils,
                DateUtils,
                AshaPortalUtils,
                PeriodService,
                TEIGridService,
                DHIS2EventFactory,
                AttributesFactory,
                ProgramFactory,
                ProgramStageFactory,
                CurrentSelection,
                OptionSetService,
                EventReportService,
                DialogService) {
                    
    $scope.today = DateUtils.getToday();
    $scope.periodOffset = 0;
    
    $scope.approvalAuthorityLevel = AshaPortalUtils.getApprovalAuthorityLevel();
    
    $scope.ouModes = [{name: 'SELECTED'}, {name: 'CHILDREN'}, {name: 'DESCENDANTS'}, {name: 'ACCESSIBLE'}];         
    $scope.selectedOuMode = $scope.ouModes[0];
    $scope.report = {};
    $scope.periodType = null;
    
    $scope.optionSets = CurrentSelection.getOptionSets();
    if(!$scope.optionSets){
        $scope.optionSets = [];
        OptionSetService.getAll().then(function(optionSets){
            angular.forEach(optionSets, function(optionSet){                        
                $scope.optionSets[optionSet.id] = optionSet;
            });

            CurrentSelection.setOptionSets($scope.optionSets);
        });
    }    
    
    ProgramStageFactory.getAll().then(function(stages){
        $scope.programStages = stages;
        $scope.programStagesById = [];
        $scope.paymentStages = [];
        $scope.paymentStagesById = [];
        $scope.dataElementForServiceOwner = null;
        $scope.dataElementForPaymentSanctioned = null;
        $scope.dataElementForCurrentApprovalLevel = null;
        $scope.dataElementForCurrentApprovalStatus = null;        
        $scope.periodType = null;
        $scope.periodOffset = 0;
        
        angular.forEach(stages, function(stage){
            
            $scope.programStagesById[stage.id] = stage;
            
            if(stage.BeneficiaryRegistration || stage.ActivityRegistration){
                $scope.paymentStagesById[stage.id] = stage;
                $scope.paymentStages.push(stage);
                $scope.periodType = stage.periodType;
            }

            for( var i=0; 
                 i<stage.programStageDataElements.length && 
                 !$scope.dataElementForServiceOwner || 
                 !$scope.dataElementForPaymentSanctioned ||
                 !$scope.dataElementForCurrentApprovalLevel ||
                 !$scope.dataElementForCurrentApprovalStatus; 
                 i++){
                if( stage.programStageDataElements[i] && stage.programStageDataElements[i].dataElement ) {

                    if( stage.programStageDataElements[i].dataElement.PaymentSanctioned ){
                        $scope.dataElementForPaymentSanctioned = stage.programStageDataElements[i].dataElement;
                    }                                    
                    if( stage.programStageDataElements[i].dataElement.ServiceOwner ){
                        $scope.dataElementForServiceOwner = stage.programStageDataElements[i].dataElement;
                    }                                    
                    if( stage.programStageDataElements[i].dataElement.ApprovalLevel ){
                        $scope.dataElementForCurrentApprovalLevel = stage.programStageDataElements[i].dataElement;
                    }                                    
                    if( stage.programStageDataElements[i].dataElement.ApprovalStatus ){
                        $scope.dataElementForCurrentApprovalStatus = stage.programStageDataElements[i].dataElement;
                    }
                }
            }
        });        

        if(!$scope.dataElementForServiceOwner || 
                !$scope.dataElementForPaymentSanctioned ||
                !$scope.dataElementForCurrentApprovalLevel ||
                !$scope.dataElementForCurrentApprovalStatus){
            //invalid db configuration
            var dialogOptions = {
                headerText: 'invalid_db_configuration',
                bodyText: $translate('stage_missing_service_owner_config')
            };
            DialogService.showDialog({}, dialogOptions);
            $scope.enrollmentSuccess = false;
            return;    
        }
        
        if( !$scope.periodType ){
            //invalid db configuration
            var dialogOptions = {
                headerText: 'invalid_db_configuration',
                bodyText: $translate('stage_missing_period_type')
            };
            DialogService.showDialog({}, dialogOptions);
            $scope.enrollmentSuccess = false;
            return;
        }
        $scope.periods = PeriodService.getPeriodsByType($scope.periodType, $scope.periodOffset);
    });        
    
    $scope.getPeriods = function(mode){
        
        if( mode === 'NXT'){
            $scope.periodOffset = $scope.periodOffset + 1;
            $scope.selectedPeriod = null;
            $scope.periods = PeriodService.getPeriodsByType($scope.periodType, $scope.periodOffset);
        }
        else{
            $scope.periodOffset = $scope.periodOffset - 1;
            $scope.selectedPeriod = null;
            $scope.periods = PeriodService.getPeriodsByType($scope.periodType, $scope.periodOffset);
        }
    };        
                        
    //watch for selection of org unit from tree
    $scope.$watch('selectedOrgUnit', function() {      
        $scope.selectedProgram = null;
        $scope.reportStarted = false;
        $scope.dataReady = false;  
        if( angular.isObject($scope.selectedOrgUnit)){            
            $scope.loadPrograms($scope.selectedOrgUnit);
        }
    });
    
    //load programs associated with the selected org unit.
    $scope.loadPrograms = function(orgUnit) {        
        $scope.selectedOrgUnit = orgUnit;        
        if (angular.isObject($scope.selectedOrgUnit)){
            $scope.programs = [];
            $scope.beneficiaryPrograms = [];
            $scope.programsById = [];
            ProgramFactory.getProgramsByOu($scope.selectedOrgUnit, $scope.selectedProgram, null).then(function(response){                
                angular.forEach(response.programs, function(pr){
                    $scope.programsById[pr.id] = {name: pr.name, id: pr.id, type: pr.type};
                });
                
                $scope.programs = $filter('filter')(response.programs, {ProgramOwner: 'ASHA', type: 1});
                $scope.activityPrograms = $filter('filter')(response.programs, {ProgramOwner: 'ASHA', type: 3});
                $scope.beneficiaryPrograms = $filter('filter')(response.programs, {ProgramOwner: 'Beneficiary'});
                
                if($scope.programs.length === 1){
                    $scope.selectedProgram = $scope.programs[0];
                }
                else{
                    $scope.selectedProgram = null;
                }             
            });
        }
    };
    
    $scope.generateReport = function(program, period, ouMode){
        
        $scope.selectedProgram = program;
        $scope.selectedPeriod = period;
        $scope.selectedOuMode = ouMode;
        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid || !$scope.selectedProgram){
            return false;
        }
        
        $scope.reportStarted = true;
        $scope.reportFinished = false;
            
        AttributesFactory.getByProgram($scope.selectedProgram).then(function(atts){            
            var grid = TEIGridService.generateGridColumns(atts, $scope.selectedOuMode.name);   
            $scope.gridColumns = grid.columns;
        });
                        
        AshaPortalUtils.getPaymentRate($scope.selectedPeriod.iso, $scope.selectedOrgUnit.id).then(function(paymentRate){
                
            $scope.paymentRate = paymentRate;
        
            //fetch TEIs for the selected program and orgunit/mode
            /* params
             * orgUnit, ouMode, program, startDate, endDate, programStatus, 
             * eventStatus, dataElement, dataValue, paging, pager*/
            EventReportService.getPaymentDetails($scope.selectedOrgUnit.id, 
                                                $scope.selectedOuMode.name, 
                                                $scope.selectedProgram.id, 
                                                DateUtils.formatFromUserToApi($scope.selectedPeriod.startDate), 
                                                DateUtils.formatFromUserToApi($scope.selectedPeriod.endDate), 
                                                'ACTIVE',
                                                null,
                                                $scope.dataElementForServiceOwner.id,
                                                false,
                                                null).then(function(data){                     

                $scope.paymentList = [];
                $scope.teiList = [];
                $scope.teisById = [];
                $scope.paymentsByOwner = [];
                $scope.serviceGridColumns = [{id: 'ownerProgramStage', name: 'ownerProgramStage', statusCol: false},
                                            {id: 'programId', name: 'programId', statusCol: false},
                                            {id: 'programStageId', name: 'programStageId', statusCol: false}];
                
                $scope.serviceGridColumns.push({name: 'service_date', id: 'eventDate', statusCol: true});
                $scope.serviceGridColumns.push({name: 'current_approval_status', id: $scope.dataElementForCurrentApprovalStatus.id,  statusCol: true});
                $scope.serviceGridColumns.push({name: 'current_approval_level', id: $scope.dataElementForCurrentApprovalLevel.id,  statusCol: true});

                angular.forEach(data.eventEventRows, function(row){

                    if(row.eventProvider && row.eventProvider.trackedEntityInstance && row.eventProvider.programStage && $scope.paymentStagesById[row.eventProvider.programStage] && 
                                ($scope.paymentStagesById[row.eventProvider.programStage].ActivityRegistration || $scope.paymentStagesById[row.eventProvider.programStage].BeneficiaryRegistration)){                        

                        if(!$scope.teisById[row.eventProvider.trackedEntityInstance]){
                            var tei = {};
                            tei.trackedEntityInstance = row.eventProvider.trackedEntityInstance;
                            tei.paymentRowCount = 0;
                            tei.paymentRows = [];
                            angular.forEach(row.eventProvider.attributes, function(att){
                                var val = AttributesFactory.formatAttributeValue(att, $scope.attributesById, $scope.optionSets, 'USER');                        
                                tei[att.attribute] = val;                        
                            });
                            $scope.teisById[row.eventProvider.trackedEntityInstance] = tei;
                            $scope.teiList.push(row.eventProvider.trackedEntityInstance);
                        }

                        if(row.providedEvents){
                            angular.forEach(row.providedEvents, function(ev){
                                
                                if( !$scope.printableExists ){
                                    $scope.printableExists = true;
                                }
                                var service = {};
                                
                                service.programName = $scope.programsById[ev.program].name;
                                service.program = ev.program;
                                service.programStage = ev.programStage;
                                service.orgUnit = ev.orgUnit;
                                service.event = ev.event;
                                service.status = ev.status;
                                service.enrollment = ev.enrollment;
                                service.type = $scope.programsById[ev.program].type;
                                
                                if(ev.trackedEntityInstance){
                                    service.trackedEntityInstance = ev.trackedEntityInstance;
                                }
                                service.eventDate = DateUtils.formatFromApiToUser(ev.eventDate);
                                if(ev.dueDate){
                                    service.dueDate =  DateUtils.formatFromApiToUser(ev.dueDate);
                                }
                                
                                if($scope.programsById[ev.program].type === 1){
                                    service.serviceName = $scope.programStagesById[ev.programStage].name;
                                }
                                else{
                                    service.serviceName = service.programName;
                                }                                
                                
                                service.ownerTrackedEntityInstance = row.eventProvider.trackedEntityInstance;
                                service.ownerOrgUnitName = row.eventProvider.orgUnitName;
                                service.ownerProgramStage = row.eventProvider.programStage;
                                service.ownerProgramStageName = $scope.programStagesById[row.eventProvider.programStage].name;
                                
                                $scope.teisById[row.eventProvider.trackedEntityInstance].paymentRowCount++;                            

                                if(ev.dataValues){
                                    angular.forEach(ev.dataValues, function(dv){
                                        var val = dv.value;
                                        if(dv.dataElement && val && dv.type){
                                            if(dv.type === 'date'){
                                                val = DateUtils.formatFromApiToUser(val);
                                            }
                                            if(dv.type === 'trueOnly'){
                                                if(val === 'true'){
                                                    val = true;
                                                }
                                                else{
                                                    val = '';
                                                }
                                            }
                                            
                                            if(dv.dataElement === $scope.dataElementForCurrentApprovalLevel.id){
                                                service[dv.dataElement] = new Number(val);
                                            }
                                            else{
                                                if(dv.dataElement === $scope.dataElementForCurrentApprovalStatus.id){
                                                    service.currentApprovalStatus = val;
                                                }
                                                service[dv.dataElement] = val;
                                            }
                                        }
                                    });                                
                                }                                
                                $scope.teisById[row.eventProvider.trackedEntityInstance].paymentRows.push(service);                                                            
                            });
                        }
                    }
                });
                $scope.reportFinished = true;
                $scope.reportStarted = false;
            });
        });
    };
    
    $scope.savePayment = function(payment, mode){
        
        if( $scope.programStagesById[payment.programStage] && $scope.programStagesById[payment.programStage].id ){
            
            var headerText = payment.latestApprovalStatus;
            if(mode === 'RELEASE'){
                headerText = payment[$scope.dataElementForPaymentSanctioned.id] ? $translate('release_payment') : $translate('hold_payment');
            }
            
            var modalOptions = {
                closeButtonText: 'cancel',
                actionButtonText: 'yes',
                headerText: headerText,
                bodyText: $translate('proceed_?')
            };

            //(event, programStage, optionSets)
            ModalService.showModal({}, modalOptions).then(function(result){                
                var obj = mode === 'APPROVAL' ? AshaPortalUtils.saveApproval( payment, 
                                          $scope.programStagesById[payment.programStage], 
                                          $scope.optionSets, 
                                          $scope.dataElementForCurrentApprovalLevel.id, 
                                          $scope.dataElementForCurrentApprovalStatus.id) : EventUtils.reconstruct(payment, $scope.programStagesById[payment.programStage], $scope.optionSets);

                DHIS2EventFactory.update( mode === 'APPROVAL' ? obj.model : obj ).then(function(){                    
                    if(mode === 'APPROVAL'){
                        payment.currentApprovalLevel =  payment[$scope.dataElementForCurrentApprovalLevel.id] = obj.display[$scope.dataElementForCurrentApprovalLevel.id];
                        payment[$scope.dataElementForCurrentApprovalStatus.id] = payment.latestApprovalStatus;
                        payment.currentApprovalStatus = payment.latestApprovalStatus;
                    }                    
                });                           
            }, function(){
                if(mode === 'APPROVAL'){
                    payment.latestApprovalStatus = null;
                }
                if(mode === 'RELEASE'){
                    payment[$scope.dataElementForPaymentSanctioned.id] = !payment[$scope.dataElementForPaymentSanctioned.id];
                }
            });
        }        
    };
});