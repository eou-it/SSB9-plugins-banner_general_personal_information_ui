personalInformationAppControllers.controller('piEditEmergencyContactController',['$scope', '$modalInstance', 'piEmergencyContactService','$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService', 'editEmergencyContactProperties', 'personalInformationService',
    function ($scope, $modalInstance, piEmergencyContactService, $rootScope, $state, $filter, notificationCenterService, piCrudService, editEmergencyContactProperties,
              personalInformationService){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.isCreateNew = true;
        $scope.highestPriority = 1;

        $scope.firstNameErrMsg = '';
        $scope.lastNameErrMsg = '';
        $scope.streetLine1ErrMsg = '';
        $scope.cityErrMsg = '';
        $scope.stateErrMsg = '';
        $scope.zipErrMsg = '';
        $scope.nationErrMsg = '';
        $scope.addressErrMsg = '';


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.removeContactFieldErrors = function() {
            if($scope.firstNameErrMsg) {
                $scope.firstNameErrMsg = piEmergencyContactService.getErrorFirstName($scope.emergencyContact);
            }
            if($scope.lastNameErrMsg) {
                $scope.lastNameErrMsg = piEmergencyContactService.getErrorLastName($scope.emergencyContact);
            }
            if($scope.streetLine1ErrMsg) {
                $scope.streetLine1ErrMsg = piEmergencyContactService.getErrorStreetLine1($scope.emergencyContact);
            }
            if($scope.cityErrMsg){
                $scope.cityErrMsg = piEmergencyContactService.getErrorCity($scope.emergencyContact);
            }
            if($scope.stateErrMsg) {
                $scope.stateErrMsg = piEmergencyContactService.getErrorState($scope.emergencyContact);
            }
            if($scope.zipErrMsg) {
                $scope.zipErrMsg = piEmergencyContactService.getErrorZip($scope.emergencyContact);
            }
            if($scope.nationErrMsg) {
                $scope.nationErrMsg = piEmergencyContactService.getErrorNation($scope.emergencyContact);
            }
        };

        var isValidContact = function (contact) {
            $scope.firstNameErrMsg = piEmergencyContactService.getErrorFirstName(contact);
            $scope.lastNameErrMsg = piEmergencyContactService.getErrorLastName(contact);
            $scope.streetLine1ErrMsg = piEmergencyContactService.getErrorStreetLine1(contact);
            $scope.cityErrMsg = piEmergencyContactService.getErrorCity(contact);
            $scope.stateErrMsg = piEmergencyContactService.getErrorState(contact);
            $scope.zipErrMsg = piEmergencyContactService.getErrorZip(contact);
            $scope.nationErrMsg = piEmergencyContactService.getErrorNation(contact);

            return !($scope.firstNameErrMsg || $scope.lastNameErrMsg || $scope.streetLine1ErrMsg || $scope.cityErrMsg ||
                    $scope.stateErrMsg || $scope.zipErrMsg || $scope.nationErrMsg);
        };

        $scope.saveEmergencyContact = function() {
            if(isValidContact($scope.emergencyContact)) {
                var handleResponse = function (response) {
                    if (response.failure) {
                        $scope.addressErrMsg = response.message;
                        piEmergencyContactService.displayErrorMessage(response.message);
                    } else {
                        var notifications = [];
                        notifications.push({message: 'personInfo.save.success.message',
                            messageType: $scope.notificationSuccessType,
                            flashType: $scope.flashNotification}
                        );

                        $state.go(personalInformationService.getFullProfileState(),
                            {onLoadNotifications: notifications, startingTab: 'emergencyContact'},
                            {reload: true, inherit: false, notify: true}
                        );
                    }
                };

                if($scope.isCreateNew) {
                    piCrudService.create('EmergencyContact', $scope.emergencyContact).$promise.then(handleResponse);
                }
                else {
                    piCrudService.update('EmergencyContact', $scope.emergencyContact).$promise.then(handleResponse);
                }
            }
            else {
                piEmergencyContactService.displayMessages();
            }
        };

        $scope.getRelationships = piCrudService.getListFn('Relationship');
        $scope.getNations = piCrudService.getListFn('Nation');
        $scope.getStates = piCrudService.getListFn('State');

        this.init = function() {

            if (editEmergencyContactProperties.currentEmergencyContact) {
                // Set up for "update emergency contact"
                $scope.isCreateNew = false;
                $scope.emergencyContact = angular.copy(editEmergencyContactProperties.currentEmergencyContact);
                if($scope.emergencyContact.nation) {
                    $scope.emergencyContact.nation = {
                        code: $scope.emergencyContact.nation.code,
                        description: $scope.emergencyContact.nation.nation
                    };
                }
            } else {
                // Create "new emergency contact" object
                $scope.emergencyContact = {
                    priority: editEmergencyContactProperties.highestPriority,
                    relationship: {},
                    state: {},
                    nation: {},
                    city: null,
                    houseNumber: null,
                    streetLine1: null,
                    streetLine2: null,
                    streetLine3: null,
                    streetLine4: null,
                    zip: null
                };

            }

            $scope.highestPriority = editEmergencyContactProperties.highestPriority;
            $scope.priorities = _.range(1, $scope.highestPriority+1);
        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);
