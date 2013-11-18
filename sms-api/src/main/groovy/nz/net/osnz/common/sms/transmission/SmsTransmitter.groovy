package nz.net.osnz.common.sms.transmission

import nz.ac.auckland.common.config.ConfigKey

import nz.net.osnz.sms.common.api.SmsService
import nz.net.osnz.sms.common.thread.TransmitterThread
import nz.net.osnz.sms.common.utils.TaskResult
import nz.net.osnz.sms.common.utils.TransmissionResult
import nz.net.osnz.sms.common.utils.ValidationResult

import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.regex.Pattern

/**
 * @author Kefeng Deng
 *
 * The implmentation of SMS service
 */
@Component('smsService')
class SmsTransmitter implements SmsService {

    private static final Logger LOG = LoggerFactory.getLogger(SmsTransmitter)

    // mobile phone prefix source: http://en.wikipedia.org/wiki/Telephone_numbers_in_New_Zealand#Mobile_phones
    protected static final List<String> mobilePrefixes = ["21", "22", "27", "26", "28", "29", "20"]

    protected static final String NZ_COUNTRY_CODE = '64'

    protected static final regexp = Pattern.compile("[@£¥èéùìòç Øø ÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-.\\\\/0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà]{1,160}")

    protected ExecutorService executor

    @ConfigKey('sms.username')
    String smsUsername

    @ConfigKey('sms.password')
    String smsPassword

    @ConfigKey("sms.url")
    String smsUrl

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(50);
    }

    @PreDestroy
    public void preDestroy() {
        executor.shutdown()
    }

    @Override
    public Map<String, TransmissionResult> sendMessage(String mobile, String message) {
        Map<String, List<String>> map = new HashMap<>()
        map.put(mobile, [message])
        return transmitMessage(map)
    }

    @Override
    public Map<String, TransmissionResult> sendMessages(Map<String, List<String>> messages) {
        return transmitMessage(messages)
    }

    @Override
    public boolean isMobile(String areaCode) {
        if (!areaCode)
          return false

        if (areaCode.startsWith("0"))
          areaCode = areaCode.substring(1)

        if (areaCode.length() > 2) {
          areaCode = areaCode.substring(0,2)
        }

        return mobilePrefixes.contains(areaCode)
    }



    /**
     * Transmit message to mobile
     *
     * @param message - the messages need to be sent
     * @return  - transmission result for each mobile
     */
    protected Map<String, TransmissionResult> transmitMessage(Map<String, List<String>> messagesMap) {

        Map<String, TransmissionResult> resultMap = new HashMap<>()

        Map<String, String> smsMap = new HashMap<>()

        messagesMap.each {String mobile, List<String> messages ->

            LOG.debug("SMS request to <${mobile?:"null"}>:[${messages?:"null"}]")

            ValidationResult mobileResult = formatMobile(mobile)
            ValidationResult messageResult = validateMessage(messages)

            if ( mobileResult.valid && messageResult.valid ) {
                messages.each {String m->
                    LOG.info("SMS request to <${mobileResult.mobile?:"null"}>:[${m?:"null"}]")
                    smsMap[mobile] = buildRequestURL(mobileResult.mobile, m)
                }
            } else {
                if ( !mobileResult.valid ) {
                    resultMap[mobile] = mobileResult.result
                } else {
                    resultMap[mobile] = messageResult.result
                }
            }
        }

        if ( smsMap.size() > 0 ) {
            resultMap.putAll( transmissionSimpleMessage(smsMap) )
        }

        return resultMap
    }

    /**
     *
     * @param mobile
     * @param message
     * @return
     */
    protected Map<String, TransmissionResult> transmissionSimpleMessage(Map<String, String> messagesMap) {

        Map<String, TransmissionResult> resultMap = new HashMap<>()

        List<Future<TaskResult>> resultList = new ArrayList<>()

        Collection<TransmitterThread> taskList = new ArrayList<>()

        messagesMap.each {String mobile, String url ->
            taskList.add(new TransmitterThread(mobile, url))
        }

        try {
            resultList.addAll( executor.invokeAll(taskList) )
        } catch(InterruptedException ie) {
            LOG.error "Interrupt exception during transmit messages"
        }

        resultList.each { Future<TaskResult> task ->
            resultMap.put(task.get().getMobile(), task.get().result)
        }

        return resultMap

    }

    /**
     * Generate a SMS request URL
     *
     * @param mobile
     * @param message
     * @return
     */
    protected String buildRequestURL(String mobile, String message) {
        StringBuilder sb = new StringBuilder()
        sb.append(smsUrl).append('?smsprovider=1&method=2&')
        sb.append('USERNAME=').append(smsUsername).append('&')
        sb.append('PASSWORD=').append(smsPassword).append('&')
        sb.append('smsnum=').append(mobile).append('&')
        sb.append('Memo=').append(URLEncoder.encode(message, "UTF-8"))
        return sb.toString()
    }

    protected ValidationResult formatMobile(String mobile) {

        ValidationResult validationResult = new ValidationResult()
        validationResult.valid = true

        if ( StringUtils.isBlank(mobile) ) {
            validationResult.result = TransmissionResult.BLANK_CELLPHONE
            validationResult.valid = false
            return validationResult
        }

        if ( mobile.startsWith("+")) {
            mobile = mobile.substring(1)
        }

        while ( mobile.startsWith('0') ) {
            mobile = mobile.substring(1);
        }

        if ( mobile.startsWith(NZ_COUNTRY_CODE) ) {
            mobile = mobile.substring(2);
        }

        if ( mobile.trim().length() <= 5 ) {
            validationResult.result = TransmissionResult.WRONG_CELLPHONE
            validationResult.valid = false
        } else {
            String carrierCode = mobile.substring(0, 2)
            if ( !mobilePrefixes.contains(carrierCode) ) {
                validationResult.result =  TransmissionResult.NO_SUCH_CARRIER
                validationResult.valid = false
            }
        }

        if ( validationResult.valid ) {
            validationResult.mobile = String.format("%s%s", NZ_COUNTRY_CODE, mobile)
        }


        return validationResult
    }


    protected ValidationResult validateMessage(List<String> messages) {

        ValidationResult validationResult = new ValidationResult()
        validationResult.valid = true

        if ( !messages && messages.size() == 0 ) {
            validationResult.result =  TransmissionResult.BLANK_MESSAGE
            validationResult.valid = false
        }

        if ( validationResult.valid ) {
            messages.each {String m->
                if (StringUtils.isBlank(m)) {
                    validationResult.result =  TransmissionResult.BLANK_MESSAGE
                    validationResult.valid = false
                }
                if ( m.length() > 150) {
                    validationResult.result =  TransmissionResult.TOO_LONG
                    validationResult.valid = false
                }
                if (!regexp.matcher(m).matches()) {
                    validationResult.result =  TransmissionResult.BAD_FORMAT
                    validationResult.valid = false
                }
            }
        }

        return validationResult

    }

}
