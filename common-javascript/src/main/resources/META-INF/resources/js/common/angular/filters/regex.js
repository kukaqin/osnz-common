/**
 * Allow filtering of string values on javascript regular expressions.
 */
OSNZ.common.filter("regex", function() {
    return function(input, regex) {

        // compile the regex (may throw exception if invalid).
        var regexp = new RegExp(regex);

        // test the truth of the input against the expression.
        return regexp.test(input);

    };
});