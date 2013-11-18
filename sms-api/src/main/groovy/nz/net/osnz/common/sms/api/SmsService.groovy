package nz.net.osnz.common.sms.api

import nz.net.osnz.common.sms.utils.TransmissionResult

/**
 * @author Kefeng Deng
 *
 * The interface to send SMS messages
 */
public interface SmsService {

    /**
     * Send a single message to a single person
     * @param mobile    - the target mobile
     * @param message   - the sending message
     * @return          - whether send the message to target mobile successfully
     */
    public Map<String, TransmissionResult> sendMessage(String mobile, String message)

    /**
     * Send multiple messages to multiple persons
     * @param messages - the sending messages (Map Key : target mobile number, Map value: the sending message)
     * @return         - whether send the messages successfully
     */
    public Map<String, TransmissionResult> sendMessages(Map<String, List<String>> messages)

    /**
    * Determines whether or not an area code for a phone number passed through is a mobile number
    *
    * @param areaCode
    * @return true if it is a recognized mobile prefix
    */
    public boolean isMobile(String areaCode);

}