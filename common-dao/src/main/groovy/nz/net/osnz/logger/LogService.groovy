package nz.net.osnz.logger

import groovy.transform.CompileStatic

/**
 * @author Kefeng Deng
 *
 * This service exists solely to unwind cyclical dependencies that Spring (for some reason) can't seem to handle.
 */
@CompileStatic
public interface LogService {

    String logMessage( String message, Map data )

    String logMessageWithJSON( String message, Map data )

}
