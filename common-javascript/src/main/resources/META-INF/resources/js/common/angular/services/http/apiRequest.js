/**
 * This service allows the user to do API requests. Responses are handled
 * by wrapping them in a response object. Forbidden requests will be handled
 * elegantly.
 */
OSNZ.httpServices.factory("apiRequest", [
    'utils', '$http', '$rootScope', '$location', '$window',
    function(utils, $http, $rootScope, $location, $window) {

        function _determineCode(status) {
            switch(status) {
                case 302: return 'redirect';
                case 403: return 'forbidden';
                case 404: return 'missing';
                case 405: return 'method';
                case 418: return 'teapot';
                case 500: return 'server';
                default: return '' + status;
            }
        }

        return utils.instantiate({

            /**
             * Sends a request along the Syllabus. Returns a request context object that provides access to the
             * request, status, headers, and config objects provided by the angular $http callback signatures. This
             * object is also passed as the second argument of both success and failure callbacks.
             * @param {String} endpoint The endpoint to target.
             * @param {Object} parameters The data to send.
             * @param {Function} success What to do on success.
             * @param {Function} failure What to do on failure.
             * @returns {Object} The response context object.
             */
            send: function(endpoint, parameters, success, failure) {
                return this.request('post', endpoint, null, parameters, function(response, context) {

                    // if the response contains the error key...
                    if (response.error) {
                        // pass it to the failure callback.
                        failure(response, context);

                        //otherwise...
                    } else {

                        // pass it to the success callback.
                        success(response, context);
                    }

                }, function(response, context) {

                    // if the request totally bombs, wrap it in an error.
                    failure({
                        error: 'http.error.' + _determineCode(context.status),
                        context: {
                            payload: response,
                            context: context
                        }
                    }, context);

                });
            },

            /**
             * Get request
             */
            get : function(endpoint, parameters, complete, error) {
                // avoid caching (msie.)
                angular.extend(parameters, { "_tstamp" : new Date().getTime() });

                return this.request('get', endpoint, parameters, null, complete, error);
            },

            /**
             * Post request
             */
            post : function(endpoint, parameters, complete, error) {
                return this.request('post', endpoint, null, parameters, complete, error);
            },

            /**
             * Put request
             */
            put : function(endpoint, parameters, complete, error) {
                return this.request('put', endpoint, null, parameters, complete, error);
            },

            /**
             * Delete request
             */
            'delete' : function(endpoint, parameters, complete, error) {
                return this.request('delete', endpoint, null, parameters, complete, error);
            },

            /** MSIE-compatible Alias for the delete request */
            remove : function(endpoint, parameters, complete, error) {
                return this['delete'](endpoint, parameters, complete, error);
            },

            /**
             * Perform a request
             *
             * @param method is the method to use
             * @param endpoint is the URL to call
             * @param parameters are the parameters to send
             * @param dataParameters are parameters that are to be sent in the function body
             * @param complete is the callback on success
             * @param error is the callback on error
             */
            request : function(method, endpoint, parameters, dataParameters, complete, error) {

                if (typeof(complete) === "undefined") {
                    console.error('No `complete` callback specified; won\'t bother executing something that isn\'t ' +
                        'being handled at all');
                    return null;
                }

                // save original request parameters
                var originalRequest = {
                    endpoint : endpoint,
                    parameters : parameters,
                    method : method
                };

                var responseContext = {
                    request : null,
                    status : null,
                    headers : null,
                    config : null
                };

                var httpRequestFunc = function() {

                    $http.defaults.headers.common['X-Angular-Referer'] = $location.absUrl();

                    // do call
                    $http({
                        method : method,
                        url : endpoint,
                        params: parameters,
                        data: dataParameters
                    })

                    // handle success
                    .success(function(data, status, headers, config) {
                        angular.extend(responseContext, {
                            request: originalRequest,
                            status: status,
                            headers: headers,
                            config: config
                        });

                        complete(data, responseContext);
                    })

                    // handle error
                    .error(function(data, status, headers, config) {
                        angular.extend(responseContext, {
                            request: originalRequest,
                            status: status,
                            headers: headers,
                            config: config
                        });

                        switch (status) {
                            // reload the page on weird error to re-authenticate the user
                            case 0:
                            case 302:
                            case 403:
                            case 404:
                                $window.document.location.reload();
                                break;

                            default:

                                // no error callback specified, do something ourselves.
                                if (typeof(error) === "undefined") {
                                    console.log("No error handler was assigned, debug information below: ");
                                    console.log({ payload: data, context: responseContext });
                                }
                                else {
                                    error(data, responseContext);
                                }

                                break;
                        }
                    });
                };


                // determine whether it's safe to execute this request right now
                // if not, we'll defer it by using evalAsync.
                if ($rootScope.$$phase === '$digest' || $rootScope.$$phase === '$apply') {
                    $rootScope.$evalAsync(function(scope) {
                        httpRequestFunc();
                    });
                }
                else {
                    httpRequestFunc();
                }

                return responseContext;
            }

        });

    }
]);
