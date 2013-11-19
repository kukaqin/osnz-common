/**
 * Contains useful form utility functions that help you manage your forms better.
 */
OSNZ.common.factory("formUtils", ['utils', function(utils) {

    return utils.instantiate({

        /**
         * Reset a form
         *
         * @param element
         */
        resetForm : function(element) {
            utils.clearAttributes(element);
            element.find("[data-rest-id]").each(function() {
                angular.element(this).text("").hide();
            });
        },

        /**
         * Set errors on the form fields. Make sure you specify the `events-bind`
         * attribute on the form-field directive otherwise it won't be able to
         * bind them back.
         *
         * @param element   is the form element to search for
         * @param errors    is a list of errors {field: .., message: ..}
         */
        distributeErrors : function(element, errors) {

            // reset existing errors
            element.find("[data-rest-id]").each(function() {
                angular.element(this).css("display", "none").text("");
            });

            // go through new errors
            _.each(errors, function(error) {
                var el = element.find("[data-rest-id='" + error.field + "']");
                if (el.length > 0) {
                    el.css("display", "block");

                    if (_.isDefined(error.message)) {
                        el.text(error.message);
                    }
                    else if (_.isDefined(error.messageCode) && _.isDefined(_t)) {
                        el.text(_t(error.messageCode));
                    }

                }
            });
        },


        /**
         * Find the REST binding and create a key/value-mapping from these, it's
         * an easy way to get the information you need to send to the server.
         *
         * @param element is the root element to go looking for bound elements
         */
        gatherRestFormValues : function(element) {

            var form = {};

            element.find("[data-rest-id]").each(function() {
                var
                    el = angular.element(this),
                    row = el.closest(".row"),
                    field = el.data("rest-id"),
                    inputField = null;

                // no row found? try nearest div.
                if (row.length === 0) {
                    row = el.closest("div");
                    inputField = el;
                }
                else {
                    inputField = row.find("input, textarea, select");
                }

                if (!row.is(":hidden") && angular.isDefined(inputField) && inputField !== "") {

                    var value = null;

                    // has custom events-value attribute?
                    if (row.attr("rest-value")) {
                        value = row.attr("rest-value");
                    }

                    // more than one? probably radiobutton
                    else if (inputField.length > 1) {
                        value = $(inputField).filter(":checked").val();
                    }

                    // only store value if the checkbox has been checked
                    else if (inputField.is(":checkbox")) {

                        if (inputField.is(":checked")) {
                            value = inputField.val();
                        }
                    }

                    // just get the value
                    else {
                        value = inputField.first().val();
                    }

                    form[field] = value;
                }
            });

            return form;
        }

    });

}]);