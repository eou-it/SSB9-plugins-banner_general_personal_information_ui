personProfileApp.service('ppAddressService', ['$resource', 'notificationCenterService',
    function ($resource, notificationCenterService) {

        var createAddress = $resource('../ssb/:controller/:action',
                {controller: 'PersonProfileDetails', action: 'addAddress'}, {save: {method: 'POST'}});

        var messages = [];

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

        this.displayMessages = function() {
            _.each(messages, function(message) {
                notificationCenterService.addNotification(message.msg, message.type);
            });

            messages = [];
        };
    }
]);