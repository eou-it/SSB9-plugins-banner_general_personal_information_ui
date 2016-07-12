/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personProfileApp.service('ppAddressService', ['$resource', '$filter', '$timeout', 'notificationCenterService',
    function ($resource, $filter, $timeout, notificationCenterService) {
    var getAddresses = $resource('../ssb/:controller/:action',
            {controller: 'PersonProfileDetails', action: 'getActiveAddressesForCurrentUser'});

    this.getAddresses = function () {
        return getAddresses.get();
    };

}]);
