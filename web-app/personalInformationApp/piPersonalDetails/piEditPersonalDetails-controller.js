personalInformationAppControllers.controller('piEditPersonalDetailsController',['$scope', '$modalInstance', 'piPersonalDetailsService','$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService', 'personalInformationService',
    function ($scope, $modalInstance, piPersonalDetailsService, $rootScope, $state, $filter, notificationCenterService, piCrudService,
              personalInformationService){

        // CONTROLLER VARIABLES
        // --------------------


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.savePersonalDetails = function() {
            // TODO: stub
        };

        this.init = function() {

        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);
