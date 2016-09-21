personalInformationApp.service('piAddressService', ['notificationCenterService', '$filter',
    function (notificationCenterService, $filter) {

        var messages = [];

        this.getErrorAddressType = function (address) {
            var msg = 'personInfo.address.error.addressType';
            if (!address.addressType.code) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorFromDate = function (address) {
            var msg = 'personInfo.address.error.fromDate';
            if (!address.fromDate) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorDateRange = function (address, addressList) {
            var msg = 'personInfo.address.error.dateRange';
            if (address.fromDate) {
                var MAX_DATE = 8640000000000000;
                var fromDate = new Date(Date.parse(address.fromDate));
                var toDate = address.toDate ? new Date(Date.parse(address.toDate)) : new Date(MAX_DATE) ;
                var flatList = _.flatten(addressList);

                var overlappedAddress = _.find(flatList,
                    function(listItem){
                        var isRangeError = false;
                        if(address.id !== listItem.id) {
                            var listFromDate = new Date(Date.parse(listItem.fromDate));
                            var listToDate = listItem.toDate ? new Date(Date.parse(listItem.toDate)) : new Date(MAX_DATE);

                            if (fromDate < listToDate) {
                                isRangeError = toDate >= listFromDate;
                            }
                            else {
                                isRangeError = fromDate === listToDate;
                            }
                        }
                        return isRangeError;
                    }
                );

                if(overlappedAddress){
                    var data = [];
                    data[0] = overlappedAddress.fromDate;
                    data[1] = overlappedAddress.toDate ?
                        overlappedAddress.toDate : '('+ $filter('i18n')('personInfo.no.end.date') +')';
                    msg = $filter('i18n')(msg, data);
                    messages.push({msg: msg, type: 'error'});

                    return msg;
                }
                else {
                    notificationCenterService.removeNotification(msg);
                }
            }
        };

        this.getErrorStreetLine1 = function(address) {
            var msg = 'personInfo.address.error.streetLine1';
            if (!address.streetLine1) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorCity = function(address) {
            var msg = 'personInfo.address.error.city';
            if (!address.city) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorStateCountyNation = function(address) {
            var msg = 'personInfo.address.error.stateNationZip';
            if((!address.state.code && !address.nation.code) || (address.state.code && !address.zip)){
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.displayMessages = function() {
            _.each(messages, function(message) {
                notificationCenterService.addNotification(message.msg, message.type);
            });

            messages = [];
        };
    }
]);
