personProfileAppDirectives.directive('selectBox', function() {
    return {
        scope: true,
        link: function(scope, elem, attrs) {
            var data = angular.fromJson(attrs.forSelect),
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
                            var desc;
                            if('webDescription' in item && item.webDescription) {
                                desc = item.webDescription;
                            }
                            else if('nation' in item && item.nation) {
                                desc = item.nation;
                            }
                            else {
                                desc = item.description;
                            }

                            results.push({
                                id: item.code,
                                text: desc
                            });
                        });
                        var more = (page * 1) < data.length;
                        return {
                            results: results,
                            more: more
                        };
                    }
                }
            });

            elem.on('change', function() {
                var selectedItem = elem.select2("data");
                scope.$eval(data.model).code = selectedItem.id;
                scope.$eval(data.model).description = selectedItem.text;
                scope.$apply();
            });

            scope.$on('$destroy', function () {
                elem.unbind('change');
            });
        }
    };
});