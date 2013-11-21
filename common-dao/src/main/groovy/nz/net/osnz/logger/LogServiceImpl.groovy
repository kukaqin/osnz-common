package nz.net.osnz.logger

import groovy.transform.CompileStatic
import nz.ac.auckland.util.JacksonHelperApi
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component

import javax.inject.Inject

/**
 * @author Kefeng Deng
 */
@CompileStatic
@Component('logService')
class LogServiceImpl implements LogService {

    @Inject
    JacksonHelperApi jacksonHelperApi

    @Inject
    MessageSource messageSource

    /**
     * This helper method takes an error message, adds a colon and space to the end, and finally tacks on a json
     * serialised data object including information from the current request object before returning the result.
     * Uses to tag the message appropriately, and attempts to convert the input message using
     * i18n if it is a valid code.
     * @param message The message to start with.
     * @param data The data to serialise.
     * @return A String in the format {message}: {data as JSON}.
     */
    @Override
    String logMessage(String message, Map data) {
        logMessageWithJSON(message, data)
    }

    /**
     * This helper method takes an error message, adds a colon and space to the end, and finally tacks on a json
     * serialised data object before returning the result. Uses to tag the message appropriately,
     * and attempts to convert the input message using i18n if it is a valid code.
     * @param message The message to start with.
     * @param data The data to serialise.
     * @return A String in the format {message}: {data as JSON}.
     */
    @Override
    String logMessageWithJSON(String message, Map data) {
        return messageSource.getMessage(message, null, message, null) + ': ' + jacksonHelperApi.jsonSerialize(data)
    }

}
