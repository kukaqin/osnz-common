/*
 * initialize AngularJS app namespace
 */
window.OSNZ = window.OSNZ || {};

window.JS_SETTINGS = window.JS_SETTINGS || {};

/**
 * This structure provides access to all the ajax url endpoints required for the application. 
 * All the values should be populated in a resource JS file
 */
OSNZ.endpoints = OSNZ.endpoints ? OSNZ.endpoints : { baseUrl: null };

OSNZ.dateConfiguration = {
    date : {
        dateFormat : "dd/mm/yy",
        showOn : 'both',
        buttonText : '&nbsp;'
    }
};

/**
 * Application module specific template locations
 */
OSNZ.template = function(templateName) {
    return window.template(OSNZ.endpoints.base, templateName, "osnz");
};

/*
 *  Create app common module
 */
OSNZ.common = angular.module("osnz-common", []);
OSNZ.services = angular.module("osnz-services", []);
OSNZ.mockServices = angular.module("osnz-mockServices", []);
OSNZ.restServices = angular.module("osnz-restServices", []);
OSNZ.httpServices = angular.module("osnz-httpServices", []);

OSNZ.library = angular.module("osnz-lib", ["osnz-common", "osnz-services", "osnz-mockServices", "osnz-restServices"]);
