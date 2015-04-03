/* global trackerCapture, angular */

trackerCapture.controller('BeneficiaryController',
        function($scope,
                $translate,
                orderByFilter,
                ProgramFactory,
                ProgramStageFactory,
                AttributesFactory,
                EntityQueryFactory,
                DHIS2EventFactory,
                RegistrationService,
                OptionSetService,
                SessionStorageService,
                EnrollmentService,
                EventReportService,
                TEIService,
                TEIGridService,
                DateUtils,
                DialogService,
                Paginator,
                CurrentSelection) {
    
    $scope.selectedService = {};
    
    $scope.showAddNewServiceDiv = false;
    $scope.showRegistrationDiv = false;
    
    $scope.ouModes = [{name: 'SELECTED'}, {name: 'CHILDREN'}, {name: 'DESCENDANTS'}, {name: 'ACCESSIBLE'}];
    $scope.selectedOuMode = $scope.ouModes[0];
    $scope.searchMode = { listAll: 'LIST_ALL', freeText: 'FREE_TEXT', attributeBased: 'ATTRIBUTE_BASED' };
    $scope.searchText = {value: null};    
    
    $scope.selectedOrgUnit = SessionStorageService.get('SELECTED_OU');
    $scope.selectedEnrollment = {dateOfEnrollment: DateUtils.getToday(), dateOfIncident: DateUtils.getToday()};
    
    //Paging
    $scope.pager = {pageSize: 50, page: 1, toolBarDisplay: 5};   
    
    $scope.attributesById = CurrentSelection.getAttributesById();
    if(!$scope.attributesById){
        $scope.attributesById = [];
        AttributesFactory.getAll().then(function(atts){
            angular.forEach(atts, function(att){
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
        $scope.ashaDetails = benOwners.asha;
        $scope.ashaPeriod = benOwners.period.eventDate;
        $scope.ashaEvent = benOwners.period.event;
        
        ProgramFactory.getBeneficairyPrograms().then(function(response){
            $scope.beneficiaryProgramsById = [];
            $scope.programStageIds = [];
            $scope.stagesById = [];
            $scope.enrollments = [];
            $scope.enrollmentsByProgram = [];

            if(response && response.beneficiaryPrograms && response.commonBenProgram){
                $scope.beneficiaryPrograms = response.beneficiaryPrograms;
                $scope.commonBeneficiaryProgram = response.commonBenProgram;
                
                $scope.beneficiaryPrograms = orderByFilter($scope.beneficiaryPrograms, '-id');

                angular.forEach($scope.beneficiaryPrograms, function(pr){
                    $scope.beneficiaryProgramsById[pr.id] = pr;
                    angular.forEach(pr.programStages, function(st){
                        $scope.programStageIds.push(st.id);
                    });            
                });

                if($scope.commonBeneficiaryProgram){
                    AttributesFactory.getByProgram($scope.commonBeneficiaryProgram).then(function(atts){
                        $scope.attributes = atts;
                        var grid = TEIGridService.generateGridColumns($scope.attributes, $scope.selectedOuMode.name);
                        $scope.gridColumns = grid.columns;
                        $scope.serviceGridColumns = [];

                        angular.forEach($scope.gridColumns, function(col){
                            $scope.serviceGridColumns.push(col);
                        });


                        ProgramStageFactory.getAll().then(function(stages){
                            $scope.stages = [];
                            $scope.dataElementForServiceOwner = null;
                            $scope.dataElementForPaymentSanctioned = null;
                            $scope.dataElementForLatestApprovalLevel = null;
                            $scope.dataElementForLatestApprovalStatus = null;
                            angular.forEach(stages, function(stage){
                                if($scope.programStageIds.indexOf( stage.id ) !== -1){                
                                    $scope.stages.push(stage);
                                    $scope.stagesById[stage.id] = stage;
                                }

                                for( var i=0; 
                                     i<stage.programStageDataElements.length && 
                                     !$scope.dataElementForServiceOwner || 
                                     !$scope.dataElementForPaymentSanctioned ||
                                     !$scope.dataElementForLatestApprovalLevel ||
                                     !$scope.dataElementForLatestApprovalStatus; 
                                     i++){
                                    if( stage.programStageDataElements[i] && stage.programStageDataElements[i].dataElement ) {

                                        if( stage.programStageDataElements[i].dataElement.PaymentSanctioned ){
                                            $scope.dataElementForPaymentSanctioned = stage.programStageDataElements[i].dataElement;
                                        }                                    
                                        if( stage.programStageDataElements[i].dataElement.ServiceOwner ){
                                            $scope.dataElementForServiceOwner = stage.programStageDataElements[i].dataElement;
                                        }                                    
                                        if( stage.programStageDataElements[i].dataElement.ApprovalLevel ){
                                            $scope.dataElementForLatestApprovalLevel = stage.programStageDataElements[i].dataElement;
                                        }                                    
                                        if( stage.programStageDataElements[i].dataElement.ApprovalStatus ){
                                            $scope.dataElementForLatestApprovalStatus = stage.programStageDataElements[i].dataElement;
                                        }
                                    }
                                }
                            });

                            $scope.serviceGridColumns.push({name: $translate('program'), id: 'program', type: 'string', displayInListNoProgram: false, showFilter: false, show: true});
                            $scope.serviceGridColumns.push({name: $translate('service'), id: 'serviceName', type: 'string', displayInListNoProgram: false, showFilter: false, show: true});
                            $scope.serviceGridColumns.push({name: $translate('service_date'), id: 'serviceProvisionDate', type: 'date', displayInListNoProgram: false, showFilter: false, show: true});
                            $scope.serviceGridColumns.push({name: $translate('approval_level'), id: $scope.dataElementForLatestApprovalLevel.id, type: 'string', displayInListNoProgram: false, showFilter: false, show: true});
                            $scope.serviceGridColumns.push({name: $translate('approval_status'), id: $scope.dataElementForLatestApprovalStatus.id, type: 'optionSet', displayInListNoProgram: false, showFilter: false, show: true});
                            
                            $scope.search($scope.searchMode.listAll);

                            $scope.getServicesProvided();

                        });
                    });
                }
            }
        });
    };
    
    var resetFields = function(){
        $scope.teiFetched = false;    
        $scope.emptySearchText = false;
        $scope.emptySearchAttribute = false;
        $scope.showSearchDiv = false;
        $scope.showRegistrationDiv = false;  
        $scope.showTrackedEntityDiv = false;
        $scope.trackedEntityList = null; 
        $scope.teiCount = null;

        $scope.queryUrl = null;
        $scope.programUrl = null;
        $scope.attributeUrl = {url: null, hasValue: false};
    };
    
    //listen to current ASHA and reporting period
    $scope.$on('beneficiaryRegistration', function(event, args){
        $scope.optionSets = args.optionSets;
        getOwnerDetails();
    });    
    
    $scope.registerBeneficiary = function(destination){        
        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid ){
            return false;
        }                   
        
        if( !$scope.commonBeneficiaryProgram || !$scope.commonBeneficiaryProgram.id){
            var dialogOptions = {
                headerText: 'program_not_defined',
                bodyText: 'common_beneficiary_program_undefined'
            };

            DialogService.showDialog({}, dialogOptions);
            return false;
            return false;
        }
        
        //form is valid, continue the registration
        //get selected entity        
        if(!$scope.selectedTei.trackedEntityInstance){
            $scope.selectedTei.trackedEntity = $scope.tei.trackedEntity = $scope.commonBeneficiaryProgram.trackedEntity.id;
            $scope.selectedTei.orgUnit = $scope.tei.orgUnit = $scope.selectedOrgUnit.id;
            $scope.selectedTei.attributes = $scope.selectedTei.attributes = [];
        }
        
        //get tei attributes and their values
        //but there could be a case where attributes are non-mandatory and
        //registration form comes empty, in this case enforce at least one value        
        
        var result = RegistrationService.processForm($scope.tei, $scope.selectedTei, $scope.attributesById);
        $scope.formEmpty = result.formEmpty;
        $scope.tei = result.tei;
        
        if($scope.formEmpty){//registration form is empty
            return false;
        }
        
        RegistrationService.registerOrUpdate($scope.tei, $scope.optionSets, $scope.attributesById).then(function(response){

            if(response.status === 'SUCCESS'){
                
                $scope.tei.trackedEntityInstance = response.reference;
                $scope.selectedTei.trackedEntityInstance = response.reference;
                
                var enrollment = {};
                enrollment.trackedEntityInstance = $scope.tei.trackedEntityInstance;
                enrollment.program = $scope.commonBeneficiaryProgram.id;
                enrollment.status = 'ACTIVE';
                enrollment.orgUnit = $scope.selectedOrgUnit.id;
                enrollment.dateOfEnrollment = $scope.selectedEnrollment.dateOfEnrollment;
                enrollment.dateOfIncident = $scope.selectedEnrollment.dateOfIncident === '' ? $scope.selectedEnrollment.dateOfEnrollment : $scope.selectedEnrollment.dateOfIncident;

                EnrollmentService.enroll(enrollment).then(function(data){
                    if(data.status !== 'SUCCESS'){
                        //enrollment has failed
                        var dialogOptions = {
                                headerText: 'enrollment_error',
                                bodyText: data.description
                            };
                        DialogService.showDialog({}, dialogOptions);
                        $scope.enrollmentSuccess = false;
                        return;
                    }
                    
                    $scope.showRegistrationDiv = false;
                    $scope.fetchTei();
                    
                    if(destination === 'SERVICE'){
                        $scope.selectedTei.id = $scope.selectedTei.trackedEntityInstance;
                        $scope.showAddNewService($scope.selectedTei);
                    }
                    
                    //reset form
                    $scope.selectedTei = {};
                    $scope.outerForm.submitted = false;
                });               
            }
            else{//update/registration has failed
                var dialogOptions = {
                        headerText: $scope.tei && $scope.tei.trackedEntityInstance ? 'update_error' : 'registration_error',
                        bodyText: response.description
                    };
                DialogService.showDialog({}, dialogOptions);
                return;
            }
        });        
    };
    
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
    
    $scope.getServicesProvided = function(){        
        $scope.servicesProvided = [];
        EventReportService.getEventReport($scope.selectedOrgUnit.id, 
                                          $scope.ouModes[1].name, 
                                          null, 
                                          null, 
                                          null, 
                                          'ACTIVE',
                                          'VISITED', 
                                          $scope.dataElementForServiceOwner && $scope.dataElementForServiceOwner.id ? $scope.dataElementForServiceOwner.id : null, 
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
                var serviceProvided = {};                    
                angular.forEach(row.attributes, function(att){
                    var val = AttributesFactory.formatAttributeValue(att, $scope.attributesById, $scope.optionSets, 'USER');                        
                    serviceProvided[att.attribute] = val;                        
                });

                if($scope.stagesById[row.programStage] && $scope.beneficiaryProgramsById[row.program]){
                    serviceProvided.serviceName = $scope.stagesById[row.programStage].name;
                    serviceProvided.programName = $scope.beneficiaryProgramsById[row.program].name; 
                }
                serviceProvided.serviceProvisionDate = DateUtils.formatFromApiToUser(row.dueDate);
                serviceProvided.event = row.event;
                serviceProvided.program = row.program;
                serviceProvided.programStage = row.programStage;
                serviceProvided.trackedEntietyInstance = row.trackedEntityInstance;
                
                angular.forEach(row.dataValues, function(dv){
                    if(dv.dataElement && 
                            dv.value && 
                            $scope.dataElementForServiceOwner && 
                            $scope.dataElementForServiceOwner.id && 
                            dv.dataElement !== $scope.dataElementForServiceOwner.id &&
                            dv.dataElement !== $scope.dataElementForPaymentSanctioned.id){                        
                        serviceProvided[dv.dataElement] = dv.value;                        
                    }                    
                });                
                
                $scope.servicesProvided.push(serviceProvided);

            });

            //sort services provided by their provision dates - this is default
            $scope.servicesProvided = orderByFilter($scope.servicesProvided, '-provisionDate');
            //$scope.servicesProvided.reverse();
            
        });
    };
    
    $scope.search = function(mode){   
        $scope.sortColumn = {};
        
        if( !$scope.commonBeneficiaryProgram ){
            console.log('There needs to be at least one beneficiary program');
            return false;
        }
        
        resetFields();
        
        $scope.selectedSearchMode = mode;        
   
        if($scope.commonBeneficiaryProgram){
            $scope.programUrl = 'program=' + $scope.commonBeneficiaryProgram.id;
        }        
        
        //check search mode
        if( $scope.selectedSearchMode === $scope.searchMode.freeText ){ 
            
            if(!$scope.searchText.value){                
                $scope.emptySearchText = true;
                $scope.teiFetched = false;   
                $scope.teiCount = null;
                return;
            }       
 
            $scope.queryUrl = 'query=' + $scope.searchText.value;                     
        }
        
        if( $scope.selectedSearchMode === $scope.searchMode.attributeBased ){            
            $scope.searchText.value = null;
            $scope.attributeUrl = EntityQueryFactory.getAttributesQuery($scope.attributes, $scope.enrollment);
            
            if(!$scope.attributeUrl.hasValue && !$scope.commonBeneficiaryProgram){
                $scope.emptySearchAttribute = true;
                $scope.teiFetched = false;   
                $scope.teiCount = null;
                return;
            }
        }
        
        $scope.fetchTei();
    };
    
    $scope.fetchTei = function(){

        $scope.selectedBeneficiary = null;
        
        //get events for the specified parameters
        TEIService.search($scope.selectedOrgUnit.id, 
                                            $scope.selectedOuMode.name,
                                            $scope.queryUrl,
                                            $scope.programUrl,
                                            $scope.attributeUrl.url,
                                            $scope.pager,
                                            true).then(function(data){            
            
            if(data.rows){
                $scope.teiCount = data.rows.length;
            }                    
            
            if( data.metaData.pager ){
                $scope.pager = data.metaData.pager;
                $scope.pager.toolBarDisplay = 5;

                Paginator.setPage($scope.pager.page);
                Paginator.setPageCount($scope.pager.pageCount);
                Paginator.setPageSize($scope.pager.pageSize);
                Paginator.setItemCount($scope.pager.total);                    
            }
            
            //process tei grid
            $scope.trackedEntityList = TEIGridService.format(data,false, $scope.optionSets);            
            $scope.showTrackedEntityDiv = true;
            $scope.teiFetched = true;            
            
            if(!$scope.sortColumn.id){                                      
                $scope.sortGrid({id: 'created', name: $translate('registration_date'), valueType: 'date', displayInListNoProgram: false, showFilter: false, show: false});
            }
        });
    };
    
    $scope.jumpToPage = function(){
        if($scope.pager && $scope.pager.page && $scope.pager.pageCount && $scope.pager.page > $scope.pager.pageCount){
            $scope.pager.page = $scope.pager.pageCount;
        }
        $scope.search();
    };
    
    $scope.resetPageSize = function(){
        $scope.pager.page = 1;        
        $scope.search();
    };
    
    $scope.getPage = function(page){    
        $scope.pager.page = page;
        $scope.search();
    };
    
    $scope.showAddServiceRow = function(){
        $scope.showAddNewServiceDiv = !$scope.showAddNewServiceDiv;        
        $scope.showSearchDiv = false;
        $scope.showRegistrationDiv = false;
    }; 
    
    $scope.showRegistration = function(){
        $scope.showRegistrationDiv = !$scope.showRegistrationDiv;
    };
    
    $scope.showHideSearch = function(simpleSearch){
        if(simpleSearch){
            $scope.showSearchDiv = false;
        }
        else{
            $scope.showSearchDiv = !$scope.showSearchDiv;
        }        
    };
    
    $scope.showAddNewService = function(selectedBeneficiary){        
        $scope.beneficiaryEnrollments = [];
        $scope.beneficiaryEnrollmentsByProgram = [];
        $scope.selectedBeneficiary = selectedBeneficiary;
        console.log('the beneficiary is:  ', $scope.selectedBeneficiary);
        
        if($scope.selectedBeneficiary && $scope.selectedBeneficiary.id){            
            EnrollmentService.getByEntity($scope.selectedBeneficiary.id).then(function(response){                
                angular.forEach(response.enrollments, function(en){
                    if($scope.beneficiaryProgramsById[en.program]){
                        if(en.status === 'ACTIVE'){
                            $scope.beneficiaryEnrollmentsByProgram[en.program] = en;
                            $scope.beneficiaryEnrollments.push(en);
                        }
                    }
                });
            });           
            $scope.showAddNewServiceDiv = true;
        }
    };
    
    $scope.hideAddNewService = function(){
        $scope.showAddNewServiceDiv = false;
        $scope.selectedBeneficiary = null;
    };
    
    $scope.addNewService = function(){
        
        if( $scope.selectedService.program && 
                $scope.selectedService.program.id && 
                $scope.selectedService.service &&
                $scope.selectedService.service.id &&
                $scope.selectedService.dueDate ){
            
            var dhis2Event = {};            
            dhis2Event.trackedEntityInstance = $scope.selectedBeneficiary.id;
            dhis2Event.program = $scope.selectedService.program.id;
            dhis2Event.programStage = $scope.selectedService.service.id;
            dhis2Event.orgUnit = $scope.selectedOrgUnit.id;
            dhis2Event.status = 'VISITED';
            dhis2Event.dueDate = dhis2Event.eventDate = DateUtils.formatFromUserToApi($scope.selectedService.dueDate);
            dhis2Event.dataValues = [];
            $scope.selectedServiceStage = $scope.stagesById[$scope.selectedService.service.id];
            
            for(var i=0; i<$scope.selectedServiceStage.programStageDataElements.length; i++){
                var prStDe = $scope.selectedServiceStage.programStageDataElements[i];
                if(prStDe.dataElement.ServiceOwner){
                    dhis2Event.dataValues.push({dataElement: prStDe.dataElement.id, value: $scope.ashaEvent});
                }
            }
            
            $scope.selectedEnrollment = $scope.beneficiaryEnrollmentsByProgram[$scope.selectedService.program.id];
            if($scope.selectedEnrollment && $scope.selectedEnrollment.enrollment){
                dhis2Event.enrollment = $scope.selectedEnrollment.enrollment;
                var dhis2Events = {events: [dhis2Event]};
                DHIS2EventFactory.create(dhis2Events).then(function(data){
                    appendNewService(data);
                });
            }
            else{
                $scope.selectedEnrollment = {};
                $scope.selectedEnrollment.dateOfEnrollment = $scope.selectedEnrollment.dateOfIncident = DateUtils.formatFromUserToApi(DateUtils.getToday());
                $scope.selectedEnrollment.trackedEntityInstance = $scope.selectedBeneficiary.id;
                $scope.selectedEnrollment.program = $scope.selectedService.program.id;
                $scope.selectedEnrollment.status = 'ACTIVE';
                $scope.selectedEnrollment.orgUnit = $scope.selectedOrgUnit.id;
                
                EnrollmentService.enroll($scope.selectedEnrollment).then(function(data){
                    if(data.status !== 'SUCCESS'){
                        //enrollment has failed
                        var dialogOptions = {
                                headerText: 'enrollment_error',
                                bodyText: data.description
                            };
                        DialogService.showDialog({}, dialogOptions);
                        return;
                    }
                    else{
                        $scope.selectedEnrollment.enrollment = data.reference;
                        dhis2Event.enrollment = $scope.selectedEnrollment.enrollment;
                        var dhis2Events = {events: [dhis2Event]};
                        DHIS2EventFactory.create(dhis2Events).then(function(data){                            
                            appendNewService(data);
                        });
                    }
                });
            }
        }        
    };
    
    function appendNewService(obj){

        if (obj.importSummaries[0].status === 'ERROR') {
            var dialogOptions = {
                headerText: 'service_registration_error',
                bodyText: obj.importSummaries[0].description
            };

            DialogService.showDialog({}, dialogOptions);
        }
        else{
            var newService = angular.copy($scope.selectedBeneficiary);
            newService.serviceProvisionDate = $scope.selectedService.dueDate;
            newService.event = obj.importSummaries[0].reference;
            newService.program = $scope.selectedService.program.id;
            newService.programStage = $scope.selectedService.service.id;
            newService.serviceName = $scope.selectedService.service.name;
            newService.programName = $scope.selectedService.program.name;
            newService.trackedEntietyInstance = $scope.selectedBeneficiary.id;
            
            if( !$scope.servicesProvided ){
                $scope.servicesProvided = [];
            }
            
            $scope.servicesProvided.splice($scope.servicesProvided.length,0, newService);
        }
        
        $scope.selectedService = {};
    }
});
