/**
 * Simple interface to window functionality. Put in helper so it can be stubbed
 * during testing.
 */
OSNZ.common.factory('windowHelper', ['utils', function(utils) {

    return utils.instantiate({

        /**
         * Move scroll to top
         */
        scrollToTop : function() {
            window.scrollTo(0, 0);
        }

    });

}]);