/**
 * Allows you to limit the number of characters in a textarea and
 * gives the user feedback on the amount he/she has to type/remove
 *
 * <textarea app-text-counter min-length="\d+" max-length="\d+"></textarea>
 *
 * the counter label will be put on the closest .row or .row-role class, so this
 * works particularly well in combination with the app-form-field directive.
 *
 * depending on its state the counter label has the "good" and "bad" class, make sure
 * these are styled somewhere.
 */
OSNZ.common.directive("textCounter", ['utils', function() {

    return {

        /**
         * Attribute type
         */
        restrict : 'A',

        /**
         * Scope.
         */
        scope : true,

        /**
         * Setup the counter template on the textarea
         *
         * @param scope
         * @param element
         * @param attrs
         */
        link : function(scope, element, attrs)   {
            var html = [
                '<div class="counter"><!----></div>'
            ].join("");

            scope.minLength = parseInt(attrs.ngMinlength, 10);
            scope.maxLength = parseInt(attrs.ngMaxlength, 10);

            // create counter element
            scope.counter = angular.element(html);

            // find parent to insert it in (usually form .row)
            var parent = element.closest(".row, .row-role");
            if (parent.length === 0) {
                parent = element.closest("div");
            }
            parent.append(scope.counter);

            // update when key up is fired
            element.keyup(function() {
                    scope.updateCounter();
                });

            // Not the best idea, but currently the only thing that seems to work,
            // listening to ngModel in the scope makes it no longer synchronize on assign,
            // listening to the same model property in two different attributes also doesn't
            // work. This is why I introduced a periodic check. I'm sorry.
            setInterval(
                function() {
                    scope.updateCounter();
                    return true;
                },
                1000
            );

            // set initial state
            scope.updateCounter();

        },

        /**
         * Control the behaviors
         */
        controller : ['$scope', '$element', function($scope, $element) {

            _.extend($scope, {

                /**
                 * @return {integer} with the length of the textarea
                 */
                currentLength : function() {
                    var textLength = $element.val().length;
                    return textLength;
                },

                /**
                 * @return {boolean} true if min length specified and not long enough
                 */
                tooShort : function() {
                    return $scope.minLength && $scope.currentLength() < $scope.minLength;
                },

                /**
                 * @return {boolean} true if max length specified and too long
                 */
                tooLong : function() {
                    return $scope.maxLength && $scope.currentLength() > $scope.maxLength;
                },

                /**
                 * @return {Boolean) when the length is just right
                 */
                justRight : function() {
                    return (!$scope.minLength || !$scope.tooShort()) && (!$scope.maxLength || !$scope.tooLong());
                },

                /**
                 * Set state
                 *
                 * @param msg   is the message to set
                 * @param good  whether it is a "good" state or not
                 * @private
                 */
                _setState : function(msg, good) {
                    $scope.counter
                        .html(msg)
                        .toggleClass("good", good)
                        .toggleClass("bad", !good);
                },

                /**
                 * Update the counter information
                 */
                updateCounter : function() {
                    if ($scope.tooShort()) {
                        $scope._setState(($scope.minLength - $scope.currentLength()) + " characters too short", false);
                    }
                    else if ($scope.tooLong()) {
                        $scope._setState(($scope.currentLength() - $scope.maxLength) + " characters too long", false);
                    }
                    else if ($scope.justRight()) {

                        var text = null;

                        if ($scope.maxLength) {
                            text = ($scope.maxLength - $scope.currentLength()) + " characters remaining";
                        }
                        else {
                            text = "&#x2714;";  // unicode checkmark
                        }

                        $scope._setState(text, true);
                    }
                }

            });



        }]
    };

}]);