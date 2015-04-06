/* global trackerCapture, angular */

trackerCapture.controller('ActivityController',
        function($scope,
                $translate,
                $modal,     
                orderByFilter,
                ProgramFactory,
                ProgramStageFactory,
                AttributesFactory,
                DHIS2EventFactory,
                OptionSetService,
                SessionStorageService,
                EventReportService,
                DateUtils,
                DialogService,
                ModalService,
                Paginator,
                AshaPortalUtils,
                CurrentSelection) {
                    
    $scope.approvalAuthorityLevel = AshaPortalUtils.getApprovalAuthorityLevel();
    
    $scope.ouModes = [{name: 'SELECTED'}, {name: 'CHILDREN'}, {name: 'DESCENDANTS'}, {name: 'ACCESSIBLE'}];
    $scope.somethingToApprove = false;
    $scope.somethingToReject = false;
    
    $scope.selectedOrgUnit = SessionStorageService.get('SELECTED_OU');
    
    $scope.attributesById = CurrentSelection.getAttributesById();
    if(!$scope.attributesById){
        $scope.attributesById = [];
        AttributesFactory.getAll().then(function(atts){
            angular.forEach(atts, function(att){
                att.allowDataEntry = true;
                $scope.attributesById[att.id] = att;
            });
            
            CurrentSelection.setAttributesById($scope.attributesById);
        });
    }    
    
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
    
    function getOwnerDetails(){
        $scope.selectedTei = {};
        $scope.tei = {};
        var benOwners = CurrentSelection.getBenOrActOwners();    
        
        $scope.orgUnitName = benOwners.orgUnitName;
        $scope.ashaDetails = benOwners.asha;
        $scope.ashaPeriod = benOwners.period;
        $scope.ashaEvent = benOwners.period.event;
        $scope.paymentRate = benOwners.paymentRate;
        
        ProgramFactory.getActivityPrograms().then(function(programs){
            $scope.activityPrograms = programs;            
            $scope.activityProgramsById = [];
            $scope.programStageIds = [];
            $scope.stagesById = [];
            
            angular.forEach($scope.activityPrograms, function(pr){
                $scope.activityProgramsById[pr.id] = pr;
                angular.forEach(pr.programStages, function(st){
                    $scope.programStageIds.push(st.id);
                });            
            });
            
            ProgramStageFactory.getAll().then(function(stages){
                $scope.stages = [];
                $scope.dataElementForServiceOwner = null;
                $scope.dataElementForPaymentSanctioned = null;
                $scope.dataElementForCurrentApprovalLevel = null;
                $scope.dataElementForCurrentApprovalStatus = null;
                $scope.dataElementsByStage = [];
                angular.forEach(stages, function(stage){
                    if($scope.programStageIds.indexOf( stage.id ) !== -1){                       
                        for( var i=0; i<stage.programStageDataElements.length; i++){
                            if( stage.programStageDataElements[i] && 
                                    stage.programStageDataElements[i].dataElement &&
                                    stage.programStageDataElements[i].dataElement.id ) {
                                
                                stage.programStageDataElements[i].displayForDataEntry = false;
                                if( stage.programStageDataElements[i].dataElement.PaymentSanctioned ){
                                    $scope.dataElementForPaymentSanctioned = stage.programStageDataElements[i].dataElement;
                                }                                    
                                else if( stage.programStageDataElements[i].dataElement.ServiceOwner ){
                                    $scope.dataElementForServiceOwner = stage.programStageDataElements[i].dataElement;
                                }                                    
                                else if( stage.programStageDataElements[i].dataElement.ApprovalLevel ){
                                    $scope.dataElementForCurrentApprovalLevel = stage.programStageDataElements[i].dataElement;
                                }                                    
                                else if( stage.programStageDataElements[i].dataElement.ApprovalStatus ){
                                    $scope.dataElementForCurrentApprovalStatus = stage.programStageDataElements[i].dataElement;
                                }
                                else{
                                    stage.programStageDataElements[i].displayForDataEntry = true;
                                }
                                
                                if( stage.programStageDataElements[i].dataElement.PaymentSanctioned ||
                                    stage.programStageDataElements[i].dataElement.ServiceOwner ||
                                    stage.programStageDataElements[i].dataElement.ApprovalLevel ||
                                    stage.programStageDataElements[i].dataElement.ApprovalStatus){
                                    
                                    stage.programStageDataElements[i].displayForDataEntry = false;
                                }
                                else{
                                    stage.programStageDataElements[i].displayForDataEntry = true;
                                }
                            }
                        } 
                    
                        $scope.stages.push(stage);
                        $scope.stagesById[stage.id] = stage;
                    }
                });
                
                $scope.getActivitiesConducted();
            });            
        });
    };
    
    //listen to current ASHA and reporting period
    $scope.$on('activityRegistration', function(event, args){
        $scope.optionSets = args.optionSets;
        getOwnerDetails();
    });
    
    //getOwnerDetails();
    
    //watch for changes in activity program
    $scope.$watch('selectedActivityProgram', function() {   
        $scope.selectedProgramStage = null;
        $scope.newActivity = {};
        if( angular.isObject($scope.selectedActivityProgram)){
            if($scope.selectedActivityProgram.programStages && 
                    $scope.selectedActivityProgram.programStages[0] && 
                    $scope.selectedActivityProgram.programStages[0].id &&
                    $scope.stagesById[$scope.selectedActivityProgram.programStages[0].id]){
                
                $scope.selectedProgramStage = $scope.stagesById[$scope.selectedActivityProgram.programStages[0].id];
            }
        }
    });   
    
    //sortGrid
    $scope.sortGrid = function(gridHeader){
        if ($scope.sortColumn && $scope.sortColumn.id === gridHeader.id){
            $scope.reverse = !$scope.reverse;
            return;
        }        
        $scope.sortColumn = gridHeader;
        if($scope.sortColumn.valueType === 'date'){
            $scope.reverse = true;
        }
        else{
            $scope.reverse = false;    
        }
    };
    
    $scope.d2Sort = function(tei){        
        if($scope.sortColumn && $scope.sortColumn.valueType === 'date'){            
            var d = tei[$scope.sortColumn.id];         
            return DateUtils.getDate(d);
        }
        return tei[$scope.sortColumn.id];
    };
    
    $scope.getActivitiesConducted = function(){
        $scope.approvedActivityExists = false;
        $scope.activitiesFetched = false;
        $scope.activitiesConducted = [];
        
        if($scope.dataElementForServiceOwner && $scope.dataElementForServiceOwner.id){
            EventReportService.getEventReport($scope.selectedOrgUnit.id, 
                                          $scope.ouModes[1].name, 
                                          null, 
                                          null, 
                                          null, 
                                          'ACTIVE',
                                          'VISITED', 
                                          $scope.dataElementForServiceOwner.id,
                                          $scope.ashaEvent,
                                          $scope.pager).then(function(data){
                                              
                if( data.pager ){
                    $scope.pager = data.pager;
                    $scope.pager.toolBarDisplay = 5;

                    Paginator.setPage($scope.pager.page);
                    Paginator.setPageCount($scope.pager.pageCount);
                    Paginator.setPageSize($scope.pager.pageSize);
                    Paginator.setItemCount($scope.pager.total);                    
                }

                angular.forEach(data.eventRows, function(row){
                    var activityConducted = {};
                    activityConducted.eventDate = DateUtils.formatFromApiToUser(row.dueDate);
                    activityConducted.event = row.event;
                    activityConducted.status = 'VISITED';
                    activityConducted.orgUnit = row.eventOrgUnit;
                    activityConducted.program = row.program;
                    activityConducted.programStage = row.programStage;
                    angular.forEach(row.dataValues, function(dv){
                        if(dv.dataElement && dv.value){                            
                            if(dv.dataElement === $scope.dataElementForCurrentApprovalLevel.id){
                                activityConducted[dv.dataElement] = new Number(dv.value);
                            }
                            else{
                                if(dv.dataElement === $scope.dataElementForCurrentApprovalStatus.id){
                                    activityConducted.currentApprovalStatus = dv.value;
                                }
                                activityConducted[dv.dataElement] = dv.value;
                            }
                        }                                            
                    });
                    

                    $scope.activitiesConducted.push(activityConducted);
                });

                //sort activities by their activity date
                $scope.activitiesConducted = orderByFilter($scope.activitiesConducted, '-activityDate');

            });
        }
        else{
            //invalid db configuration
            var dialogOptions = {
                    headerText: 'invalid_db_configuration',
                    bodyText: $translate('stage_missing_service_owner_config')
                };
            DialogService.showDialog({}, dialogOptions);
            $scope.enrollmentSuccess = false;
            return;
        }        
    };
    
    $scope.cancel = function(){
        $scope.selectedProgramStage = $scope.selectedActivityProgram = $scope.newActivity = null;
        $scope.outerForm.submitted = false;
    };
    
    $scope.addActivity = function(){
        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid ){
            return false;
        } 
        
        //the form is valid, get the values
        //but there could be a case where all dataelements are non-mandatory and
        //the event form comes empty, in this case enforce at least one value
        $scope.valueExists = false;
        var dataValues = [];
        angular.forEach($scope.selectedProgramStage.programStageDataElements, function(prStDe){
            if( prStDe.dataElement && 
                    prStDe.dataElement.id &&
                    !prStDe.dataElement.PaymentSanctioned && 
                    !prStDe.dataElement.ServiceOwner && 
                    !prStDe.dataElement.ApprovalLevel &&
                    !prStDe.dataElement.ApprovalStatus){
                
                var val = $scope.newActivity[prStDe.dataElement.id];
                
                if(val){
                    $scope.valueExists = true;            
                    if(prStDe.dataElement.type === 'string'){
                        if(prStDe.dataElement.optionSet){                                       
                            val = OptionSetService.getCode($scope.optionSets[prStDe.dataElement.optionSet.id].options, val);                        
                        }
                    }
                    if(prStDe.dataElement.type === 'date'){
                        val = DateUtils.formatFromUserToApi(val);
                    }
                }                
                dataValues.push({dataElement: prStDe.dataElement.id, value: val});
            }
        });
        
        if(!$scope.valueExists){
            var dialogOptions = {
                headerText: 'empty_form',
                bodyText: 'fill_at_least_one_dataelement'
            };

            DialogService.showDialog({}, dialogOptions);
            return false;
        }
        
        dataValues.push({dataElement: $scope.dataElementForServiceOwner.id, value: $scope.ashaEvent});
        var dhis2Event = {
                program: $scope.selectedActivityProgram.id,
                programStage: $scope.selectedProgramStage.id,
                orgUnit: $scope.selectedOrgUnit.id,
                status: 'VISITED',            
                eventDate: DateUtils.formatFromUserToApi($scope.newActivity.eventDate),
                dataValues: dataValues
        };
        
        DHIS2EventFactory.create(dhis2Event).then(function(data){
            if (data.importSummaries[0].status === 'ERROR') {
                var dialogOptions = {
                    headerText: 'activity_registration_error',
                    bodyText: data.importSummaries[0].description
                };

                DialogService.showDialog({}, dialogOptions);
            }
            else {
                
                //add the new event to the grid                
                $scope.newActivity.event = data.importSummaries[0].reference;
                $scope.newActivity.status = 'VISITED';
                $scope.newActivity.program = $scope.selectedActivityProgram.id;
                $scope.newActivity.programStage = $scope.selectedProgramStage.id;
                if( !$scope.activitiesConducted ){
                    $scope.activitiesConducted = [];
                }                
                $scope.activitiesConducted.splice($scope.activitiesConducted.length,0, angular.copy($scope.newActivity));                
            }
            
            $scope.cancel();
        });
    };
    
    $scope.saveActivityApproval = function(activity){
        
        var stage = $scope.stagesById[activity.programStage];
        
        if( stage && stage.id ){
            
            var modalOptions = {
                closeButtonText: 'cancel',
                actionButtonText: 'yes',
                headerText: activity.latestApprovalStatus,
                bodyText: $translate('proceed_?')
            };

            ModalService.showModal({}, modalOptions).then(function(result){
                var obj = AshaPortalUtils.saveApproval( activity, 
                                          stage, 
                                          $scope.optionSets, 
                                          $scope.dataElementForCurrentApprovalLevel.id, 
                                          $scope.dataElementForCurrentApprovalStatus.id);                
                DHIS2EventFactory.update( obj.model ).then(function(){
                    activity.currentApprovalLevel = activity[$scope.dataElementForCurrentApprovalLevel.id] = obj.display[$scope.dataElementForCurrentApprovalLevel.id];
                    activity[$scope.dataElementForCurrentApprovalStatus.id] = activity.latestApprovalStatus;   
                    activity.currentApprovalStatus = activity.latestApprovalStatus;
                });                           
            }, function(){
                activity.latestApprovalStatus = null;
            });            
        }        
    };
    
    $scope.generatePaymentSlip = function(){

        var modalInstance = $modal.open({
            templateUrl: 'components/payment/activity-payment-slip.html',
            controller: 'PaymentController',
            windowClass: 'modal-full-window',
            resolve: {
                payments: function(){
                    return $scope.activitiesConducted;
                },
                paymentRate: function(){
                    return $scope.paymentRate;
                },
                orgUnitName: function(){
                    return $scope.orgUnitName;
                },
                programs: function(){
                    return $scope.activityPrograms;
                },
                programsById: function(){
                    return $scope.activityProgramsById;
                },
                stages: function(){
                    return $scope.stages;
                },
                stagesById: function(){
                    return $scope.stagesById;
                },
                ashaDetails: function(){
                    return $scope.ashaDetails;
                },
                ashaPeriod: function(){
                    return $scope.ashaPeriod;
                },
                ashaEvent: function(){
                    return $scope.ashaEvent;
                },
                slipType: function(){
                    return 'ACTIVITY';
                }
            }
        });
        
        modalInstance.result.then(function () {                 
        });
    };       
});