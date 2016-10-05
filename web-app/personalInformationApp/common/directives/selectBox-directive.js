personalInformationAppDirectives.directive('selectBox',['$filter', function($filter) {

    // Get description from an address field item, e.g. an item for a state or nation
    var getDescriptionFromAddressComponent = function(item) {
        if('webDescription' in item && item.webDescription) {
            return item.webDescription;
        }
        else if('nation' in item && item.nation) {
            return item.nation;
        }
        else {
            return item.description;
        }
    },
    notApplicableText = $filter('i18n')('personInfo.label.notApplicable');

    return {
        scope: true,
        link: function(scope, elem, attrs) {
            var data = angular.fromJson(attrs.forSelect),
                dataModelItem = scope.$eval(data.model),
                maxItems = 10,
                showNA = data.showNA;

            elem.select2({
                width: '100%',
                placeholder: data.placeholder,
                ajax: {
                    url: data.action,
                    dataType: 'json',
                    quietMillis: 800,
                    data: function(term, page) {
                        return  {
                            searchString: term,
                            offset: page-1,
                            //max: $filter('i18n')('default.select2.maxLimit')
                            max: maxItems
                        };
                    },
                    cache: true,
                    allowClear: true,
                    results: function(data, page) {
                        var results;
                        if(showNA && page === 1) {
                            results = [{id: 'not/app', text: notApplicableText}];
                        }
                        else {
                            results = [];
                        }
                        $.each(data, function(i, item) {
                            results.push({
                                id: item.code,
                                text: getDescriptionFromAddressComponent(item)
                            });
                        });
                        var more = (page * 1) < data.length;
                        return {
                            results: results,
                            more: more
                        };
                    }
                },
                formatSelection: function(item) {
                    if(showNA && item.id === 'not/app') {
                        dataModelItem.code = '';
                        dataModelItem.description = '';
                    }
                    else {
                        dataModelItem.code = item.id;
                        dataModelItem.description = item.text;
                    }

                    return item.text;
                },
                initSelection: function(element, callback) {
                    if (dataModelItem) {
                        var data = {id: dataModelItem.code, text: getDescriptionFromAddressComponent(dataModelItem)};

                        callback(data);
                    }
                }
            }).select2("val", "_"); // Dummy value needed to make initSelection do its thing
        }
    };
}]);

personalInformationAppDirectives.directive('emergencyContactPrioritySelectBox', function() {

    return {
        scope: {
            highestPriority: '@',
            currentContact: '='
        },
        link: function(scope, elem, attrs) {
            elem.select2({
                width: '100%',
                minimumResultsForSearch: -1, // Hide search box
                query: function(query) {
                    var i = 0,
                        data = {results: []};

                    for (var i = 1; i <= scope.highestPriority; i++) {
                        data.results.push({id: i, text: '' + i});
                    }

                    query.callback(data);
                },
                formatSelection: function(item) {
                    scope.currentContact.priority = item.id;

                    return item.text;
                },
                initSelection: function(element, callback) {
                    var data = {id: scope.currentContact.priority, text: '' + scope.currentContact.priority};

                    callback(data);
                }
            });
        }
    };
});
