package nz.net.osnz.validatior

import groovy.transform.CompileStatic

/**
 * @author Kefeng Deng
 */
@CompileStatic
public abstract class ValidationConstants {

    private ValidationConstants() {}

    public static final String REQUIRED = 'required'

    public static final String TOO_SHORT = 'too.short'

    public static final String TOO_LONG = 'too.long'

    public static final String INVALID = 'invalid'

    public static final String BLANK = 'blank'

}
