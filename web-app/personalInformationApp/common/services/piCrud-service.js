personalInformationApp.service('piCrudService', ['$resource',
    function ($resource) {

        this.get = function (entityName) {
            return $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationDetails', action: 'get'+entityName}).get();
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