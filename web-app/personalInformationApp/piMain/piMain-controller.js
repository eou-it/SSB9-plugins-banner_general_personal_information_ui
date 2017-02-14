/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
personalInformationAppControllers.controller('piMainController',['$scope', '$rootScope', '$state', '$stateParams', '$modal',
    '$filter', '$q', '$timeout', '$window', 'notificationCenterService', 'piCrudService', 'piConfigResolve','personalInformationService',
    function ($scope, $rootScope, $state, $stateParams, $modal, $filter, $q, $timeout, $window, notificationCenterService,
              piCrudService, piConfigResolve, personalInformationService) {


        var displayNotificationsOnStateLoad = function() {
            $timeout(function() {
                _.each($stateParams.onLoadNotifications, function(notification) {
                    notificationCenterService.addNotification(notification.message, notification.messageType, notification.flashType);
                });
            }, 0);
        },

        /**
         * Sort addresses by type (e.g. Mailing, Permanent) and whether current or future.
         * @param addresses
         * @returns Object containing a "current" array and "future" array.
         */
        sortAddresses = function(addresses) {
            var sorted = {},
                addrType,
                timePeriod;

            _.each(addresses, function(addr) {
                addrType = addr.addressType.description;

                if (!(addrType in sorted)) {
                    sorted[addrType] = {
                        current: [],
                        future: []
                    };
                }

                timePeriod = addr.isFuture ? 'future' : 'current';
                sorted[addrType][timePeriod].push(addr);
            });

            return sorted;
        },

        // TODO: need different algorithm for this?
        formatAddressForSingleLine = function(addr) {
            if (!addr) return '';

            var addrLines = [],
                key;

            for (key in addr.displayAddress) {
                if (addr.displayAddress.hasOwnProperty(key) && !_.isEmpty(addr.displayAddress[key])) {
                    addrLines.push(addr.displayAddress[key]);
                }
            }

            return addrLines.join(', ');
        },

        // Return true if item1 is higher priority than item2
        isHigherPriority = function(item1, item2) {
            var priority1 = item1 && item1.displayPriority,
                priority2 = item2 && item2.displayPriority;

            return !priority1 ? false : !priority2 ? true : priority1 < priority2;
        },

        getAddressForOverview = function(addrGroup) {
            var key,
                address = null,
                addresses;

            if (addrGroup) {
                // Iterate through each ADDRESS TYPE
                for (key in addrGroup) {
                    // Only consider CURRENT addresses
                    if (addrGroup.hasOwnProperty(key) && addrGroup[key].hasOwnProperty('current')) {
                        // Find the HIGHEST PRIORITY address or, if we don't have one yet, start with the first one
                        addresses = addrGroup[key]['current'];

                        if (!_.isEmpty(addresses)) {
                            if (address === null || isHigherPriority(addresses[0], address)) {
                                address = addresses[0];
                            }
                        }
                    }
                }
            }

            return address;
        },

        putPreferredEmailFirst = function() {
            //preferred emails come before unpreferred, then sort the unpreferred alphabetically
            $scope.emails.sort(function(a, b) {
                var aTypeDesc = a.emailType.description.toUpperCase(),
                    bTypeDesc = b.emailType.description.toUpperCase();

                if(a.preferredIndicator) {
                    return -1;
                }
                if(b.preferredIndicator) {
                    return 1;
                }
                if (aTypeDesc < bTypeDesc) {
                    return -1;
                }
                if(aTypeDesc > bTypeDesc) {
                    return 1;
                }
                return 0;
            });
        },

        getPhoneNumberForOverview = function(phoneList) {
            var phone = null;

            if (!_.isEmpty(phoneList)) {
                _.each(phoneList, function(ph) {
                    if (phone === null || isHigherPriority(ph, phone)) {
                        phone = ph;
                    }
                });
            }

            return phone;
        };

        /**
         * Initialize controller
         */
        this.init = function() {
            var preferredNameParams = {pageName: 'PersonalInformation', sectionName: 'Overview'},

                setStartingTab = function(tabs) {
                    if ($stateParams.startingTab) {
                        $scope.startingTab = $stateParams.startingTab;
                    }
                    else if(tabs.length) {
                        $scope.startingTab = tabs[0].startingTab;
                    }
                };


            $scope.piConfig = piConfigResolve;
            $scope.sectionsToDisplay = $scope.getSectionsToDisplayForMobile(); // Depends on piConfig
            setStartingTab($scope.sectionsToDisplay);


            piCrudService.get('MaskingRules').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.maskingRules = response;
                }
            });


            piCrudService.get('Addresses').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.addressGroup = sortAddresses(response.addresses);
                    $scope.addressForOverview = formatAddressForSingleLine(getAddressForOverview($scope.addressGroup));
                }
            });

            piCrudService.get('TelephoneNumbers').$promise.then(function(response) {
                var phone;

                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.phones = response.telephones;
                    phone = getPhoneNumberForOverview($scope.phones);
                    $scope.phoneForOverview = phone ? phone.displayPhoneNumber : '';
                }
            });

            piCrudService.get('Emails').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.emails = response.emails;
                    putPreferredEmailFirst();
                    $scope.preferredEmail = $scope.emails[0] ? $scope.emails[0] : null;
                }
            });

            piCrudService.get('EmergencyContacts').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.emergencyContacts = response.emergencyContacts;
                    $scope.hasMaxEmergencyContacts = $scope.haveMaxEmergencyContacts();
                }
            });

            piCrudService.get('PreferredName', preferredNameParams).$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.preferredName = response.preferredName;
                }
            });

            piCrudService.get('UserName').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.userName = response;
                }
            });

            piCrudService.get('PersonalDetails').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.personalDetails = response;
                    $scope.ethnicity = response.ethnic === '1' ? 'personInfo.label.notHispanic' :
                                            (response.ethnic === '2' ? 'personInfo.label.hispanic' : null);
                    $scope.sexDescription = response.sex === 'M' ? 'personInfo.label.male' :
                                            (response.sex === 'F' ? 'personInfo.label.female' : 'personInfo.label.unknownSex');

                    if (!$scope.personalDetails.maritalStatus) {
                        $scope.personalDetails.maritalStatus = {code: null, description: null};
                    }
                    //initialize all veteran related scope variables
                    $scope.veteranDisabled='';
                    $scope.veteranRecent='';
                    $scope.veteranCategory='';
                    $scope.veteranArmedForcesServiceMedal='';

                    if ( $scope.piConfig.isVetClassificationDisplayable) {
                        //get all veteran related values
                        if (!$scope.personalDetails.veraIndicator) {
                            $scope.veteranCategory = $filter('i18n')('personinfo.veteran.classification.fourth');
                        } else if ($scope.personalDetails.veraIndicator == 'B') {
                            $scope.veteranCategory = $filter('i18n')('personinfo.veteran.classification.second');
                        } else if ($scope.personalDetails.veraIndicator == 'O') {
                            $scope.veteranCategory = $filter('i18n')('personinfo.veteran.classification.active.veteran');
                            if ($scope.personalDetails.activeDutySeprDate) {
                                var nowDate = new Date();
                                if (new Date($scope.personalDetails.activeDutySeprDate) >= nowDate.setFullYear((nowDate.getFullYear()) - 3)) {
                                    $scope.veteranRecent = $filter('i18n')('personinfo.veteran.classification.recentlySeparated');
                                }
                            }
                        } else if ($scope.personalDetails.veraIndicator == 'V') {
                            $scope.veteranCategory = $filter('i18n')('personinfo.veteran.classification.third');
                        }
                        if ($scope.personalDetails.sdvetIndicator == "Y") {
                            $scope.veteranDisabled = $filter('i18n')('personinfo.veteran.classification.disabled.veteran');
                        }
                        if ($scope.personalDetails.armedServiceMedalVetIndicator) {
                            $scope.veteranArmedForcesServiceMedal = $filter('i18n')('personinfo.veteran.classification.medal.veteran');
                        }
                    }

                }
            });

            if ($scope.piConfig.isDisabilityStatusDisplayable) {
                piCrudService.get('DisabilityStatus').$promise.then(function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                    } else {
                        $scope.disabilityStatus = response.disabilityStatusCode;
                        $scope.disabilityStatusText = $filter('i18n')('personinfo.disability.' + response.disabilityStatusCode);
                    }
                });
            } else {
                $scope.disabilityStatus = '';
            }

            piCrudService.get('Races').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.races = response.races;
                    $scope.racesDisplay = $scope.races.map(function(currentItem){return currentItem.description;}).join(', ');
                }
            });

            piCrudService.get('BannerId').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.bannerId = response.bannerId;
                }
            });

            displayNotificationsOnStateLoad();
        };


        // CONTROLLER CONSTANTS
        // --------------------
        $scope.SECTION_HIDDEN = '0';
        $scope.SECTION_UPDATEABLE = '2';


        // CONTROLLER VARIABLES
        // --------------------
        $scope.maskingRules = null;
        $scope.addressGroup = null;
        $scope.addressForOverview;
        $scope.emails = null;
        $scope.preferredEmail;
        $scope.phones = null;
        $scope.phoneForOverview;
        $scope.emergencyContacts = [];
        $scope.preferredName;
        $scope.userName = null;
        $scope.bannerId;
        $scope.races = [];
        $scope.racesDisplay = '';
        $scope.personalDetails = null;
        $scope.piConfig;
        $scope.sectionsToDisplay;
        $scope.hasMaxEmergencyContacts = false;


        // CONTROLLER FUNCTIONS
        // --------------------
        /**
         * Get all addresses for specified address type (e.g. Mailing, Permanent).
         * Addresses will be in order of time period, so "Current" will be contiguous, then "Future," etc.
         * @param addrType
         * @returns {Array}
         */
        $scope.getOrderedAddressesForType = function(addrType) {
            var ordered = [];

            _.each($scope.addressGroup[addrType], function(addresses) {
                ordered = ordered.concat(addresses);
            });

            return ordered;
        };

        $scope.cancelNotification = function () {
            notificationCenterService.clearNotifications();
        };

        $scope.stringToDate = personalInformationService.stringToDate;

        $scope.openEditPersonalDetailsModal = function() {
            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piPersonalDetails/piEditPersonalDetails.html'),
                windowClass: 'edit-pers-details pi-modal',
                keyboard: true,
                controller: "piEditPersonalDetailsController",
                scope: $scope
            });
        };

        $scope.openEditAddressModal = function(currentAddress) {

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piAddress/piEditAddress.html'),
                windowClass: 'edit-addr pi-modal',
                keyboard: true,
                controller: "piEditAddressController",
                scope: $scope,
                resolve: {
                    editAddressProperties: function () {
                        return {
                            currentAddress: currentAddress
                        };
                    }
                }

            });
        };

        $scope.openEditEmailModal = function(currentEmail) {

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piEmail/piEditEmail.html'),
                windowClass: 'edit-email pi-modal',
                keyboard: true,
                controller: "piEditEmailController",
                scope: $scope,
                resolve: {
                    editEmailProperties: function () {
                        return {
                            currentEmail: currentEmail
                        };
                    }
                }
            });
        };

        $scope.openEditEmergencyContactModal = function(currentEmergencyContact) {
            var numContacts = $scope.emergencyContacts.length;

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piEmergencyContact/piEditEmergencyContact.html'),
                windowClass: 'edit-emer-contact pi-modal',
                keyboard: true,
                controller: "piEditEmergencyContactController",
                scope: $scope,
                resolve: {
                    editEmergencyContactProperties: function () {
                        return {
                            currentEmergencyContact: currentEmergencyContact,
                            highestPriority: currentEmergencyContact ? numContacts : numContacts + 1
                        };
                    }
                }
            });
        };

        $scope.openEditVeteranModal = function() {

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piAdditionalDetails/piVeteranClassification.html'),
                windowClass: 'edit-veteran pi-modal',
                keyboard: true,
                controller: "piEditVeteranClassificationController",
                scope: $scope,
                resolve: {
                }
            });
        };

        $scope.openEditPhoneModal = function(currentPhone) {

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piPhone/piEditPhone.html'),
                windowClass: 'edit-phone pi-modal',
                keyboard: true,
                controller: "piEditPhoneController",
                scope: $scope,
                resolve: {
                    editPhoneProperties: function () {
                        return {
                            currentPhone: currentPhone
                        };
                    }
                }
            });
        };

        $scope.openEditDisplayProfileModal = function() {
            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piDirectoryProfile/piEditDirectoryProfile.html'),
                windowClass: 'edit-directory-profile pi-modal',
                keyboard: true,
                controller: "piEditDirectoryProfileController"
            });
        };

        $scope.openSecurityQAModal = function() {
            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piOther/piEditSecurityQA.html'),
                windowClass: 'security-qa pi-modal',
                keyboard: true,
                controller: "piEditSecurityQAController"
            });
        };

        $scope.openDisabilityStatusModal = function() {
            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piAdditionalDetails/piEditDisabilityStatus.html'),
                windowClass: 'disability-status pi-modal',
                keyboard: true,
                resolve: {
                    disabilityStatus: function() {
                        return ($scope.disabilityStatus === 0 ? null : $scope.disabilityStatus);
                    }
                },
                controller: "piEditDisabilityStatusController"
            });
        };

        // Display address delete confirmation modal
        $scope.confirmAddressDelete = function (address) {
            var deleteAddress = function () {
                $scope.cancelNotification();

                piCrudService.delete('Address', address).$promise.then(function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                    } else {
                        // Refresh address info
                        var addrType = address.addressType.description;
                        var timePeriod = address.isFuture ? 'future' : 'current';
                        var addressIndex = $scope.addressGroup[addrType][timePeriod].indexOf(address);
                        $scope.addressGroup[addrType][timePeriod].splice(addressIndex, 1);
                        if($scope.addressGroup[addrType].future.length === 0 &&
                            $scope.addressGroup[addrType].current.length === 0) {
                            delete $scope.addressGroup[addrType];
                        }
                    }
                });
            };

            var prompts = [
                {
                    label: $filter('i18n')('personInfo.button.prompt.cancel'),
                    action: $scope.cancelNotification
                },
                {
                    label: $filter('i18n')('personInfo.button.delete'),
                    action: deleteAddress
                }
            ];

            notificationCenterService.displayNotification('personInfo.confirm.address.delete.text', 'warning', false, prompts);
        };

        $scope.confirmEmailDelete = function (email) {
            var deleteEmail = function () {
                $scope.cancelNotification();

                piCrudService.delete('Email', email).$promise.then(function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                    } else {
                        // Refresh email info
                        $scope.emails.splice($scope.emails.indexOf(email), 1);
                        if (email.id === $scope.preferredEmail.id) {
                            putPreferredEmailFirst();
                            $scope.preferredEmail = $scope.emails[0] ? $scope.emails[0] : null;
                        }
                    }
                });
            };

            var prompts = [
                {
                    label: $filter('i18n')('personInfo.button.prompt.cancel'),
                    action: $scope.cancelNotification
                },
                {
                    label: $filter('i18n')('personInfo.button.delete'),
                    action: deleteEmail
                }
            ];

            notificationCenterService.displayNotification('personInfo.confirm.email.delete.text', 'warning', false, prompts);
        };

        $scope.confirmEmergencyContactDelete = function (contact) {
            var deleteEmergencyContact = function () {
                $scope.cancelNotification();

                piCrudService.delete('EmergencyContact', contact).$promise.then(function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                    } else {
                        // Refresh contact info
                        $scope.emergencyContacts = response.emergencyContacts;
                        $scope.hasMaxEmergencyContacts = $scope.haveMaxEmergencyContacts();
                    }
                });
            };

            var prompts = [
                {
                    label: $filter('i18n')('personInfo.button.prompt.cancel'),
                    action: $scope.cancelNotification
                },
                {
                    label: $filter('i18n')('personInfo.button.delete'),
                    action: deleteEmergencyContact
                }
            ];

            notificationCenterService.displayNotification('personInfo.confirm.emergencyContact.delete.text', 'warning', false, prompts);
        };

        $scope.confirmPhoneDelete = function (phone) {
            var deletePhone = function () {
                $scope.cancelNotification();

                piCrudService.delete('TelephoneNumber', phone).$promise.then(function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                    } else {
                        // Refresh phone info
                        var overviewPhone = getPhoneNumberForOverview($scope.phones);
                        $scope.phones.splice($scope.phones.indexOf(phone), 1);
                        if(phone.id === overviewPhone.id) {
                            overviewPhone = getPhoneNumberForOverview($scope.phones);
                            $scope.phoneForOverview = overviewPhone ? overviewPhone.displayPhoneNumber : '';
                        }
                    }
                });
            };

            var prompts = [
                {
                    label: $filter('i18n')('personInfo.button.prompt.cancel'),
                    action: $scope.cancelNotification
                },
                {
                    label: $filter('i18n')('personInfo.button.delete'),
                    action: deletePhone
                }
            ];

            notificationCenterService.displayNotification('personInfo.confirm.phone.delete.text', 'warning', false, prompts);
        };

        $scope.resetPassword = function() {
            personalInformationService.checkSecurityQuestionsExist().$promise.then(function (response) {
                if (response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $window.location.href = '/BannerGeneralSsb/ssb/personalInformation/resetPasswordWithSecurityQuestions';
                }
            });
        };

        $scope.getSectionsToDisplayForMobile = function() {
            var personalDetailsSectionMode =   $scope.piConfig.personalDetailsSectionMode,
                emailSectionMode =             $scope.piConfig.emailSectionMode,
                telephoneSectionMode =         $scope.piConfig.telephoneSectionMode,
                addressSectionMode =           $scope.piConfig.addressSectionMode,
                emergencyContactSectionMode =  $scope.piConfig.emergencyContactSectionMode,
                additionalDetailsSectionMode = $scope.piConfig.additionalDetailsSectionMode,
                otherSectionMode =             $scope.piConfig.otherSectionMode,
                sections = [];

            if (personalDetailsSectionMode !== $scope.SECTION_HIDDEN) {
                sections.push({
                        heading: 'personInfo.title.personalDetails',
                        startingTab: 'personalDetails',
                        template: 'personalInformationApp/piPersonalDetails/piViewPersonalDetails.html',
                        clickFunction: $scope.openEditPersonalDetailsModal,
                        footerButtonLabel: 'personInfo.label.edit',
                        // TODO: update with proper config code when available
                        isUpdateable: !personalDetailsSectionMode || personalDetailsSectionMode === $scope.SECTION_UPDATEABLE,
                        isEdit: true
                    }
                );
            }

            if (emailSectionMode !== $scope.SECTION_HIDDEN) {
                sections.push(                {
                        heading: 'personInfo.title.email',
                        startingTab: 'email',
                        template: 'personalInformationApp/piEmail/piViewEmail.html',
                        clickFunction: $scope.openEditEmailModal,
                        footerButtonLabel: 'personInfo.title.addEmail',
                        isUpdateable: !emailSectionMode || emailSectionMode === $scope.SECTION_UPDATEABLE
                    }
                );
            }

            if (telephoneSectionMode !== $scope.SECTION_HIDDEN) {
                sections.push(                {
                        heading: 'personInfo.title.phoneNumber',
                        startingTab: 'phoneNumber',
                        template: 'personalInformationApp/piPhone/piPhoneList.html',
                        clickFunction: $scope.openEditPhoneModal,
                        footerButtonLabel: 'personInfo.label.addPhone',
                        isUpdateable: !telephoneSectionMode || telephoneSectionMode === $scope.SECTION_UPDATEABLE
                    }
                );
            }

            if (addressSectionMode !== $scope.SECTION_HIDDEN) {
                sections.push(                {
                        heading: 'personInfo.title.addresses',
                        startingTab: 'address',
                        template: 'personalInformationApp/piAddress/piAddressList.html',
                        clickFunction: $scope.openEditAddressModal,
                        footerButtonLabel: 'personInfo.label.addAddress',
                        isUpdateable: !addressSectionMode || addressSectionMode === $scope.SECTION_UPDATEABLE
                    }
                );
            }

            if (emergencyContactSectionMode !== $scope.SECTION_HIDDEN) {
                sections.push(                {
                        heading: 'personInfo.title.emergencyContact',
                        startingTab: 'emergencyContact',
                        template: 'personalInformationApp/piEmergencyContact/piEmergencyContactList.html',
                        clickFunction: $scope.openEditEmergencyContactModal,
                        footerButtonLabel: 'personInfo.label.addEmergencyContact',
                        isUpdateable: !emergencyContactSectionMode || emergencyContactSectionMode === $scope.SECTION_UPDATEABLE,
                        isFooterButtonDisabledFunction: $scope.haveMaxEmergencyContacts,
                        disabledMsg: 'personInfo.message.maxEmergencyContacts'
                    }
                );
            }

            if (additionalDetailsSectionMode) {
                sections.push({
                        heading: 'personInfo.title.additionalDetails',
                        startingTab: 'additionalDetails',
                        template: 'personalInformationApp/piAdditionalDetails/piViewAdditionalDetails.html',
                        // TODO: update with proper config code when available
                        isUpdateable: true
                    }
                );
            }

            if (otherSectionMode) {
                sections.push({
                        heading: 'personInfo.title.other',
                        startingTab: 'other',
                        template: 'personalInformationApp/piOther/piOther.html',
                        // TODO: update with proper config code when available
                        isUpdateable: true
                    }
                );
            }

            return sections;
        };

        $scope.isAddressGroupEmpty = function() {
            return _.isEmpty($scope.addressGroup);
        };

        $scope.haveMaxEmergencyContacts = function() {
            var MAX_EMERGENCY_CONTACTS = 9;

            return $scope.emergencyContacts.length >= MAX_EMERGENCY_CONTACTS;
        };




        // INITIALIZE
        // ----------
        this.init();
    }
]);
