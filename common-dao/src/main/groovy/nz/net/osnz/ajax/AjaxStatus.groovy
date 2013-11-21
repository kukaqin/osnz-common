package nz.net.osnz.ajax

import com.fasterxml.jackson.annotation.JsonFormat

/**
 * @author Kefeng Deng
 *
 * This enumeration stores all the defined response codes for ajax methods, and is mappable, so can easily be added to
 * the JSON payload.
 */
@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public enum AjaxStatus {

    OK(100, 'ok'),

    ERROR_ACCESS_DENIED(201, 'Access denied'),
    ERROR_PARAMETER_MISSING(202, 'Request parameter missing'),
    ERROR_NOT_FOUND(203, 'Resource not found'),
    ERROR_DATABASE_PERSIST(204, 'Database persist failed'),
    ERROR_NOT_LOGGED_IN(205, 'There is currently no user logged'),
    ERROR_NOT_AVAILABLE(206, 'Service is unavailable'),
    ERROR_EXCEPTION(207, 'Internal error'),

    FORM_REQUIRED_FIELD(301, 'Required field'),
    FORM_BAD_FORMATTING(302, 'Invalid formatting'),
    FORM_BAD_FIELD_SIZE(303, 'Too many characters in field'),
    FORM_HAS_ERRORS(304, 'Your submitted form has errors'),
    FORM_SUBMIT_ERRORS(305, 'Your submitted form cannot be processed now. Please try again later.')

    private final int code
    private final String description

    private AjaxStatus(int code, String description) {
        this.code = code
        this.description = description
    }

    public int getCode() {
        return code
    }

    public String getDescription() {
        return this.description
    }

    public String getName() {
        return name()
    }

}
