trackerCapture.controller('EnrollmentController',
        function($rootScope,
                $scope,  
                $location,
                $timeout,
                DateUtils,
                EventUtils,
                storage,
                DHIS2EventFactory,
                AttributesFactory,
                CurrentSelection,
                TEIService,
                TEFormService,
                CustomFormService,
                EnrollmentService,
                ModalService,
                DialogService) {
    
    $scope.today = DateUtils.getToday();
    $scope.selectedOrgUnit = storage.get('SELECTED_OU');  
    
    //listen for the selected items
    var selections = {};
    $scope.$on('selectedItems', function(event, args) {   
        $scope.attributes = [];
        $scope.historicalEnrollments = [];
        $scope.showEnrollmentDiv = false;
        $scope.showEnrollmentHistoryDiv = false;
        $scope.hasEnrollmentHistory = false;
        $scope.selectedEnrollment = null;
        $scope.currentEnrollment = null;
        $scope.newEnrollment = {};
        
        selections = CurrentSelection.get();        
        processSelectedTei();       
        
        $scope.selectedEntity = selections.te;
        $scope.selectedProgram = selections.pr;
        $scope.optionSets = selections.optionSets;
        $scope.programs = selections.prs;
        var selectedEnrollment = selections.selectedEnrollment;
        $scope.enrollments = selections.enrollments ? selections.enrollments : [];        
        $scope.programExists = args.programExists;
        $scope.programNames = selections.prNames;
        $scope.programStageNames = selections.prStNames;
        
        if($scope.selectedProgram){
            
            $scope.selectedProgramWithStage = [];        
            angular.forEach($scope.selectedProgram.programStages, function(stage){
                $scope.selectedProgramWithStage[stage.id] = stage;
            });
            
            angular.forEach($scope.enrollments, function(enrollment){
                if(enrollment.program === $scope.selectedProgram.id ){
                    if(enrollment.status === 'ACTIVE'){
                        selectedEnrollment = enrollment;
                        $scope.currentEnrollment = enrollment;
                    }
                    if(enrollment.status === 'CANCELLED' || enrollment.status === 'COMPLETED'){
                        $scope.historicalEnrollments.push(enrollment);
                        $scope.hasEnrollmentHistory = true;
                    }
                }
            });
            
            if(selectedEnrollment){
                $scope.selectedEnrollment = selectedEnrollment;
                $scope.loadEnrollmentDetails(selectedEnrollment);
            }
            else{
                $scope.selectedEnrollment = null;
                $scope.broadCastSelections('dashboardWidgets');
            }
        }
        else{
            $scope.broadCastSelections('dashboardWidgets');
        }        
    });
    
    $scope.loadEnrollmentDetails = function(enrollment) {
        
        $scope.attributesForEnrollment = [];
        $scope.attributes = [];
        $scope.showEnrollmentHistoryDiv = false;
        $scope.selectedEnrollment = enrollment;
        
        if(!$scope.selectedEnrollment.enrollment){//prepare for possible enrollment
            AttributesFactory.getByProgram($scope.selectedProgram).then(function(atts){
                $scope.attributes = atts;                
                $scope.selectedProgram.hasCustomForm = false;               
                TEFormService.getByProgram($scope.selectedProgram, atts).then(function(teForm){                    
                    if(angular.isObject(teForm)){                        
                        $scope.selectedProgram.hasCustomForm = true;
                        $scope.selectedProgram.displayCustomForm = $scope.selectedProgram.hasCustomForm ? true:false;
                        $scope.trackedEntityForm = teForm;
                        $scope.customForm = CustomFormService.getForTrackedEntity($scope.trackedEntityForm, 'ENROLLMENT');
                    }
                    $scope.broadCastSelections('dashboardWidgets');
                });
            });                
        }
        else{
            $scope.broadCastSelections('dashboardWidgets');
        }
    };
        
    $scope.showNewEnrollment = function(){       
        if($scope.showEnrollmentDiv){
            $scope.hideEnrollmentDiv();
        }
       
        $scope.showEnrollmentDiv = !$scope.showEnrollmentDiv;
        $timeout(function() { 
            $rootScope.$broadcast('enrollmentEditing', {enrollmentEditing: $scope.showEnrollmentDiv});
        }, 100);        
        
        if($scope.showEnrollmentDiv){            
            $scope.showEnrollmentHistoryDiv = false;
            
            //load new enrollment details
            $scope.selectedEnrollment = {};            
            $scope.loadEnrollmentDetails($scope.selectedEnrollment);
            
            //check custom form for enrollment
            $scope.selectedProgram.hasCustomForm = false;
            $scope.registrationForm = '';
            TEFormService.getByProgram($scope.selectedProgram, $scope.attributes).then(function(teForm){
                if(angular.isObject(teForm)){
                    $scope.selectedProgram.hasCustomForm = true;
                    $scope.registrationForm = teForm;
                }                
                $scope.selectedProgram.displayCustomForm = $scope.selectedProgram.hasCustomForm ? true:false;
                $scope.broadCastSelections('dashboardWidgets');
            });            
        }
    };
       
    $scope.showEnrollmentHistory = function(){
        
        $scope.showEnrollmentHistoryDiv = !$scope.showEnrollmentHistoryDiv;
        
        if($scope.showEnrollmentHistoryDiv){
            $scope.selectedEnrollment = null;
            $scope.showEnrollmentDiv = false;
            
            $scope.broadCastSelections('dashboardWidgets');
        }
    };
    
    $scope.enroll = function(){    
        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid ){
            return false;
        }
        
        //form is valid, continue with enrollment
        var result = getProcessedForm();
        $scope.formEmpty = result.formEmpty;
        var tei = result.tei;
        
        if($scope.formEmpty){//form is empty
            return false;
        }
        
        var enrollment = {trackedEntityInstance: tei.trackedEntityInstance,
                            program: $scope.selectedProgram.id,
                            status: 'ACTIVE',
                            dateOfEnrollment: $scope.selectedEnrollment.dateOfEnrollment,
                            dateOfIncident: $scope.selectedEnrollment.dateOfIncident ? $scope.selectedEnrollment.dateOfIncident : $scope.selectedEnrollment.dateOfEnrollment
                        };
                        
        TEIService.update(tei, $scope.optionSets).then(function(updateResponse){            
            
            if(updateResponse.status === 'SUCCESS'){
                //registration is successful, continue for enrollment               
                EnrollmentService.enroll(enrollment).then(function(enrollmentResponse){                    
                    if(enrollmentResponse.status !== 'SUCCESS'){
                        //enrollment has failed
                        var dialogOptions = {
                                headerText: 'enrollment_error',
                                bodyText: enrollmentResponse
                            };
                        DialogService.showDialog({}, dialogOptions);
                        return;
                    }
                    
                    enrollment.enrollment = enrollmentResponse.reference;
                    $scope.selectedEnrollment = enrollment;
                    $scope.enrollments.push($scope.selectedEnrollment);
                    
                    $scope.autoGenerateEvents(tei, $scope.selectedProgram, $scope.selectedOrgUnit, $scope.selectedEnrollment);                    
                });
            }
            else{
                //update has failed
                var dialogOptions = {
                        headerText: 'registration_error',
                        bodyText: updateResponse.description
                    };
                DialogService.showDialog({}, dialogOptions);
                return;
            }
        });
    };
    
    $scope.broadCastSelections = function(listeners){
        var selections = CurrentSelection.get();
        var tei = selections.tei;
        var pr = selections.pr;;
        var enrollment = null;      
        if($scope.selectedEnrollment && $scope.selectedEnrollment.enrollment){
            enrollment = $scope.selectedEnrollment;
        }
        CurrentSelection.set({tei: tei, te: $scope.selectedEntity, prs: $scope.programs, pr: pr, prNames: $scope.programNames, prStNames: $scope.programStageNames, enrollments: $scope.enrollments, selectedEnrollment: enrollment, optionSets: $scope.optionSets});
        $timeout(function() { 
            $rootScope.$broadcast(listeners, {});
        }, 100);
    };    
    
    var getProcessedForm = function(){        
        $scope.attributesById = CurrentSelection.getAttributesById();
        var tei = angular.copy(selections.tei);
        tei.attributes = [];
        var formEmpty = true;
        for(var k in $scope.attributesById){
            if( $scope.selectedTei[k] ){
                tei.attributes.push({attribute: $scope.attributesById[k].id, value: $scope.selectedTei[k], displayName: $scope.attributesById[k].name, type: $scope.attributesById[k].valueType});
                formEmpty = false;
            }
        }
        
        return {tei: tei, formEmpty: formEmpty};
    };
    
    var processSelectedTei = function(){
        $scope.selectedTei = angular.copy(selections.tei);
        angular.forEach($scope.selectedTei.attributes, function(att){
            $scope.selectedTei[att.attribute] = att.value;
        });
        delete $scope.selectedTei.attributes;
    };
    
    $scope.hideEnrollmentDiv = function(){
        
        /*currently the only way to cancel enrollment window is by going through
         * the main dashboard controller. Here I am mixing program and programId, 
         * as I didn't want to refetch program from server, the main dashboard
         * has already fetched the programs. With the ID passed to it, it will
         * pass back the actual program than ID. 
         */
        processSelectedTei();
        $scope.selectedProgram = ($location.search()).program;
        $scope.broadCastSelections('mainDashboard'); 
    };
    
    $scope.terminateEnrollment = function(){        

        var modalOptions = {
            closeButtonText: 'cancel',
            actionButtonText: 'terminate',
            headerText: 'terminate_enrollment',
            bodyText: 'are_you_sure_to_terminate_enrollment'
        };

        ModalService.showModal({}, modalOptions).then(function(result){            
            EnrollmentService.cancel($scope.selectedEnrollment).then(function(data){                
                $scope.selectedEnrollment.status = 'CANCELLED';
                $scope.loadEnrollmentDetails($scope.selectedEnrollment.status);                
            });
        });
    };
    
    $scope.completeEnrollment = function(){        

        var modalOptions = {
            closeButtonText: 'cancel',
            actionButtonText: 'complete',
            headerText: 'complete_enrollment',
            bodyText: 'are_you_sure_to_complete_enrollment'
        };

        ModalService.showModal({}, modalOptions).then(function(result){            
            EnrollmentService.complete($scope.selectedEnrollment).then(function(data){                
                $scope.selectedEnrollment.status = 'COMPLETED';
                $scope.loadEnrollmentDetails($scope.selectedEnrollment);                
            });
        });
    };
            
    $scope.autoGenerateEvents = function(tei, program, orgUnit, enrollment){
        if(tei && program && orgUnit && enrollment){            
            var dhis2Events = {events: []};
            angular.forEach(program.programStages, function(stage){
                if(stage.autoGenerateEvent){
                    var newEvent = {
                            trackedEntityInstance: tei.trackedEntityInstance,
                            program: program.id,
                            programStage: stage.id,
                            orgUnit: orgUnit.id,                        
                            dueDate: DateUtils.formatFromUserToApi( EventUtils.getEventDueDate(null, stage, enrollment) ),
                            status: 'SCHEDULE'
                        };
                    
                    if(stage.openAfterEnrollment){
                        if(stage.reportDateToUse === 'dateOfIncident'){
                            newEvent.eventDate = DateUtils.formatFromUserToApi(enrollment.dateOfIncident);
                        }
                        else{
                            newEvent.eventDate = DateUtils.formatFromUserToApi(enrollment.dateOfEnrollment);
                        }
                    }
                    
                    dhis2Events.events.push(newEvent);    
                }
            });
            
            $scope.showEnrollmentDiv = false;
            $scope.outerForm.submitted = false;
            
            CurrentSelection.set({tei: tei, te: $scope.selectedEntity, prs: $scope.programs, pr: program, prNames: $scope.programNames, prStNames: $scope.programStageNames, enrollments: $scope.enrollments, selectedEnrollment: enrollment, optionSets: $scope.optionSets});
            if(dhis2Events.events.length > 0){
                DHIS2EventFactory.create(dhis2Events).then(function(data) {
                    $scope.broadCastSelections('dashboardWidgets');
                });
            }
            else{
                $scope.broadCastSelections('dashboardWidgets');
            }
        }
    };
    
    $scope.markForFollowup = function(){
        $scope.selectedEnrollment.followup = !$scope.selectedEnrollment.followup; 
        EnrollmentService.update($scope.selectedEnrollment).then(function(data){         
        });
    };
    
    $scope.switchRegistrationForm = function(){
        $scope.selectedProgram.displayCustomForm = !$scope.selectedProgram.displayCustomForm;
    };
});
