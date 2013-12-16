/**
 * File upload directive
 *
 * Usage:
 * <input type="file"
 *       id="agencyUpload"
 *       osnz-file-upload
 *       on-upload="newImageUpload"                 // triggered functon when upload complete
 *       upload-type="agency-logo"                  // type of upload server expects (sent as parameter)
 *       upload-tracker="uploadInProgress"          // upload tracker: true if upload busy
 *       end-point="agencyUploadLogo"               // the name of the endpoint to send it to
 *  />
 */
OSNZ.common.directive("osnzFileUpload", [function() {

    var
        /**
         * Every upload needs a unique file upload identifier
         */
        FILE_ID = 0,

        /**
         * Stores the callbacks
         */
        CALLBACKS = {},

        /**
         * Where to send the form.
         */
        FILE_ENDPOINT = JS_SETTING.fileUploadPath || OSNZ.endpoints.fileUpload,

        /**
         * Array with form attributes to save.
         */
        SAVE_THESE_ATTRIBUTES = ['target', 'action', 'method', 'enctype'];

    /**
     * Global callback invoker
     */
    VA.fileUploadCompleted = function(number, url, formElement) {
        if (_.isDefined(CALLBACKS[number])) {
            CALLBACKS[number](url, formElement);
            delete CALLBACKS[number];
        }
    };


    return {

        /**
         * Restrict to attribute use
         */
        restrict : 'A',

        /**
         * Scope declarations
         */
        scope : {
            /**
             * On upload callback from parent component
             */
            onUpload : "&",

            /**
             * The type of upload this file is (necessary for server-side validation)
             */
            uploadType : "@",

            /**
             * Progress tracker. If set to true parent component might show loader icon.
             */
            uploadTracker : "=",

            /**
             * Name of the endpoint to send the request to
             */
            endPoint : "@"
        },

        /**
         * Compile
         */
        link : function(scope, element, attrs) {

            var
                formFileId = ++FILE_ID,
                uploadTypeField = angular.element("<input type='hidden' name='fileType' value='' />"),
                uploadNumberField = angular.element("<input type='hidden' name='fileNumber' value='' />"),
                form = element.closest("form"),
                iframeName = "_file_form_" + formFileId,
                iframeTpl =
                    '<iframe src="' + FILE_ENDPOINT + '" name="' + iframeName + '" class="fileupload_frame">' +
                    '</iframe>'
                ;


            /**
             * extend scope with functionality
             */
            _.extend(scope, {

                /**
                 * Move the file form into a separate form and submit it
                 */
                startUpload : function() {

                    var originalValues = this._getCurrentFormAttributes();

                    // set the new form attribute values
                    form.attr({
                        target : iframeName,
                        method : "post",
                        action : OSNZ.endpoints[scope.endPoint],
                        enctype : "multipart/form-data"
                    });

                    // set element name
                    element.attr("name", "fileUpload");
                    uploadNumberField.val(formFileId);
                    uploadTypeField.val(scope.uploadType);

                    // add result iframe.
                    element.closest(".row").append(iframeTpl);

                    // store callback
                    CALLBACKS[FILE_ID] = this._callbackGenerator();

                    // indicate ongoing upload
                    scope.uploadTracker = true;

                    form.submit();

                    this._restoreFormAttributes(originalValues);

                },

                /**
                 * Return a generated function that will act as a callback
                 * to the file upload. These callbacks are stored on the
                 * anonymous inner-scope of this file.
                 *
                 * @return {Function}  an onComplete function specific to this fileupload
                 * @private
                 */
                _callbackGenerator : function() {
                    return function(url) {
                        if (_.isDefined(scope.onUpload)) {

                            var clbFun = scope.onUpload();
                            if (_.isFunction(clbFun)) {

                                // apply properly within angular
                                scope.$apply(function() {

                                    // no longer in progress
                                    scope.uploadTracker = false;
                                    clbFun(url, element);
                                });
                            }
                            else {
                                console.error("on-upload callback was empty. please specify in your directive.");
                            }
                        }
                    };
                },

                /**
                 * Get the current form attributes
                 * @private
                 */
                _getCurrentFormAttributes : function() {

                    var originalValues = {};

                    // retrieve original form attributes
                    _.each(
                        SAVE_THESE_ATTRIBUTES,
                        function(v) {
                            originalValues[v] = form.attr(v);
                        });

                    return originalValues;
                },

                /**
                 * Restore information
                 *
                 * @param storedValues
                 * @private
                 */
                _restoreFormAttributes : function(storedValues) {
                    _.each(storedValues, function(val, key) {
                        form.attr(key, val);
                    });
                }

            });

            element.closest("form")
                .append(uploadNumberField)
                .append(uploadTypeField);

            element.change(function() {
                scope.startUpload();
            });

        }
    };


}]);