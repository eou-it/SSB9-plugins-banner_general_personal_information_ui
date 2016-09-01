personalInformationApp.service('piPhoneService', ['$resource', 'notificationCenterService',
    function ($resource, notificationCenterService) {

        var getPhoneNumbers = $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationDetails', action: 'getTelephoneNumbers'});

        this.getPhoneNumbers = function () {
            return getPhoneNumbers.query();
        };
    }
]);
