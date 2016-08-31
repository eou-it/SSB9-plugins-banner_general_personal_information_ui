personalInformationApp.service('piEmailService', ['$resource', 'notificationCenterService',
    function ($resource, notificationCenterService) {

        var createEmail = $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationDetails', action: 'addEmail'}, {save: {method: 'POST'}}),

            updateEmail = $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationDetails', action: 'updateEmail'}, {save: {method: 'POST'}}),

            getEmails = $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationDetails', action: 'getEmails'}),


            deleteEmail = $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationDetails', action: 'deleteEmail'}, {delete: {method:'POST'}});


        this.getEmails = function () {
            return getEmails.get();
        };

        this.saveNewEmail = function (email) {
            return createEmail.save(email);
        };

        this.updateEmail = function (email) {
            return updateEmail.save(email);
        };

        this.deleteEmail = function (email) {
            return deleteEmail.delete(email);
        };
    }
]);
