/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personalInformationAppDirectives.directive('emailTypeSelectBox', function() {
    return {
        scope: true,
        link: function(scope, elem, attrs, ctrl) {
            var data = angular.fromJson(attrs.forSelect),
                dataModelItem = scope.$eval(data.model),
                maxItems = 10;

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
                        var results = [];
                        $.each(data, function(i, item) {
                            results.push({
                                id: item.code,
                                text: item.description,
                                urlIndicator: item.urlIndicator
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
                    dataModelItem.code = item.id;
                    dataModelItem.description = item.text;
                    dataModelItem.urlIndicator = item.urlIndicator;

                    return item.text;
                },
                initSelection: function(element, callback) {
                    if (dataModelItem) {
                        var data = {id: dataModelItem.code, text: dataModelItem.description};

                        callback(data);
                    }
                }
            }).select2("val", "_"); // Dummy value needed to make initSelection do its thing

            if(!scope.isCreateNew) {
                elem.select2("enable", false);
            }
        }
    };
});
