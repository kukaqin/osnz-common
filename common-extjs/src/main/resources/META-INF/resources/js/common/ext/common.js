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

_isDefined = function isDefined(value) {
	return typeof value != 'undefined';
}

window._t = function(label, defaultValue) {
	return _isDefined(OSNZ.i18n[label]) ? OSNZ.i18n[label] : (_isDefined(defaultValue)? defaultValue : label);
}

window.template = function(templateName, app) {
    var uri = "/" + app + "/views/" + templateName;
    if (OSNZ && _isDefined(OSNZ.extTemplates) && _isDefined(OSNZ.extTemplates[uri])) {
        return OSNZ.extTemplates[uri];
    }
    return "";
};
