personProfileAppDirectives.directive('selectBox', function() {

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
    };

    return {
        scope: true,
        link: function(scope, elem, attrs) {
            var data = angular.fromJson(attrs.forSelect),
                dataModelItem = scope.$eval(data.model),
                maxItems = 10;

            elem.select2({
                width: '200px',
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
                initSelection: function(element, callback) {
                    if (dataModelItem) {
                        var data = {id: dataModelItem.code, text: getDescriptionFromAddressComponent(dataModelItem)};

                        callback(data);
                    }
                }

            }).select2("val", "_"); // Dummy value needed to make initSelection do its thing

            elem.on('change', function() {
                var selectedItem = elem.select2("data");
                dataModelItem.code = selectedItem.id;
                dataModelItem.description = selectedItem.text;
                scope.$apply();
            });

            scope.$on('$destroy', function () {
                elem.unbind('change');
            });
        }
    };
});