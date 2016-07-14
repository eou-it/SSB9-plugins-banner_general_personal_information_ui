personProfileApp.service('ppAddressService', ['$resource', 'notificationCenterService',
    function ($resource, notificationCenterService) {

        var createAddress = $resource('../ssb/:controller/:action',
                {controller: 'PersonProfileDetails', action: 'addAddress'}, {save: {method: 'POST'}}),

            getAddresses = $resource('../ssb/:controller/:action',
                {controller: 'PersonProfileDetails', action: 'getActiveAddressesForCurrentUser'}),


            deleteAddresses = $resource('../ssb/:controller/:action',
                {controller: 'PersonProfileDetails', action: 'deleteAddresses'}, {delete: {method:'POST', isArray:true}}),


            messages = [];

        this.getAddresses = function () {
            return getAddresses.get();
        };

        this.isValidAddress = function (address) {
            var result  = true;
            if(!address.fromDate) {
                result = false;
                messages.push({msg: 'personInfo.address.error.fromDate', type: 'error'});
            }
            if(!address.streetLine1) {
                result = false;
                messages.push({msg: 'personInfo.address.error.streetLine1', type: 'error'});
            }
            if(!address.city) {
                result = false;
                messages.push({msg: 'personInfo.address.error.city', type: 'error'});
            }
            if((!address.state && !address.nation) || (!address.nation && !address.zip) ||
                (address.state && !address.zip)){
                result = false;
                messages.push({msg: 'personInfo.address.error.stateNationZip', type: 'error'});
            }
            return result;
        };

        this.saveNewAddress = function (address) {
            return createAddress.save(address);
        };

        this.deleteAddresses = function (addresses) {
            return deleteAddresses.delete(addresses);
        };

        this.displayMessages = function() {
            _.each(messages, function(message) {
                notificationCenterService.addNotification(message.msg, message.type);
            });

            messages = [];
        };
    }
]);
