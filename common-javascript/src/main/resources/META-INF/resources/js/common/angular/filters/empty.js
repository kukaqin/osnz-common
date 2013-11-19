/**
 * Show a checkmark when the value is true, otherwise return nothing
 */
OSNZ.common.filter("empty", function() {
    return function(input, alternative) {
        if (input) {
            return input;
        }
        return alternative;
    };
});