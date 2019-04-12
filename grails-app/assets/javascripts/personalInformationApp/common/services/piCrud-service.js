personalInformationApp.service('piCrudService', ['$resource',
    function ($resource) {

        this.get = function (entityName, params) {
            return $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationDetails', action: 'get'+entityName}).get(params);
        };

        this.getListFn = function(entityName) {
            return function (params) {
                return $resource('../ssb/:controller/:action',
                    {controller: 'PersonalInformationDetails', action: 'get'+ entityName +'List'}).query(params);
            };
        };

        this.create = function (entityName, entity) {
            return $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationDetails', action: 'add'+entityName}, {save: {method: 'POST'}}).save(entity);
        };

        this.update = function (entityName, entity) {
            return $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationDetails', action: 'update'+entityName}, {save: {method: 'POST'}}).save(entity);
        };

        this.delete = function (entityName, entity) {
            return $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationDetails', action: 'delete'+entityName}, {delete: {method:'POST'}}).delete(entity);
        };
    }
]);