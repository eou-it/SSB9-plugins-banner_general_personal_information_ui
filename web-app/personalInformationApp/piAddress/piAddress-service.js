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

            return this.getErrorDateFormat(address.fromDate);
        };

        this.getErrorDateFormat = function (date) {
            if(date){
                var msg = 'personInfo.address.error.dateFormat';
                if (isNaN(Date.parse(date))) {
                    messages.push({msg: msg, type: 'error'});

                    return msg;
                }
                else {
                    notificationCenterService.removeNotification(msg);
                }
            }
        };

        this.getErrorDateRange = function (address, addressList) {
            if (address.fromDate) {
                var msg = 'personInfo.address.error.dateOrder',
                    MAX_DATE = 8640000000000000,
                    fromDate = new Date(Date.parse(address.fromDate)),
                    toDate = address.toDate ? new Date(Date.parse(address.toDate)) : new Date(MAX_DATE),
                    flatList = _.flatten(addressList);

                if(fromDate > toDate){
                    messages.push({msg: msg, type: 'error'});

                    return $filter('i18n')(msg);
                }
                else {
                    notificationCenterService.removeNotification(msg);
                }

                msg = 'personInfo.address.error.dateRange';
                var overlappedAddress = _.find(flatList,
                    function(listItem) {
                        var isRangeError = false;
                        if(address.id !== listItem.id) {
                            var listFromDate = new Date(Date.parse(listItem.fromDate)),
                                listToDate = listItem.toDate ? new Date(Date.parse(listItem.toDate)) : new Date(MAX_DATE);

                            isRangeError = (fromDate < listToDate) ? toDate >= listFromDate : fromDate === listToDate;
                        }
                        return isRangeError;
                    }
                );

                if(overlappedAddress) {
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
