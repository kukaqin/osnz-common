/**
 * Convert new lines to <br/>
 */
OSNZ.common.filter("nl2br", function() {
    return function(input) {
        if (input) {
            return input.replace(/\n/g, "<br/>");
        }
        return input;
    };
});