/**
 * Tooltip directive declaration
 */
OSNZ.common.directive("tooltip", function() {


    // generic tooltip template
    var
        templateHtml = [
                '<a class="tooltip-icon">',
                    'More information',
                '</a>'
            ].join("");

    return {

        /**
         * Restrict to attribute
         */
        restrict : 'A',

        /**
         * Compile the tooltip element
         */
        compile : function(element, attrs) {

            // has text?
            if (typeof(attrs.tooltip) !== "undefined" && _.str.trim(attrs.tooltip) !== "") {

                var tDom = $(templateHtml);

                tDom.tooltip({

                    tooltipClass : "tooltip",

                    /**
                     * Where to display
                     */
                    position: {
                        my : "left-23 bottom-23",
                        collision : "none"
                    },

                    /**
                     * Layout
                     */
                    content :
                        [
                            '<div>',
                                '<div class="tooltip-top"></div>',
                                    '<div class="tooltip-body">',
                                        '<p>', attrs.tooltip, '</p>',
                                    '</div>',
                                '<div class="tooltip-bottom"></div>',
                            '</div>'
                        ].join(""),

                    /**
                     * What triggers it?
                     */
                    items : "a"

                });

                element.append(tDom);

            }

            return null;
        }

    };
});
