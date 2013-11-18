package nz.net.osnz.common.sms.thread

import nz.net.osnz.sms.common.utils.TaskResult
import nz.net.osnz.sms.common.utils.TransmissionResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.Callable

/**
 * @author Kefeng Deng
 */
class TransmitterThread implements Callable<TaskResult> {

    private static final Logger LOG = LoggerFactory.getLogger(TransmitterThread)

    private static final String HTML_CHARSET = 'UTF-8'

    private static final String SUCCESS_MESSAGE = 'Failure:0'

    private static final String USER_AGENT = 'Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0'

    private final String mobile

    private final String url

    public TransmitterThread(String m, String u) {
        LOG.trace "<${m}> : ${u}"
        this.mobile = m
        this.url = u
    }

    @Override
    TaskResult call() throws Exception {
        TaskResult taskResult = new TaskResult(mobile, TransmissionResult.FAILED)
        try {
            HttpURLConnection conn = new URL(url).openConnection() as HttpURLConnection
            try {
                conn.setRequestMethod('GET')
                conn.setDoOutput(false)
                conn.setUseCaches(false);
                conn.setConnectTimeout(60 * 1000)
                conn.setReadTimeout(60*1000)
                System.setProperty("http.agent", "")
                conn.setRequestProperty("User-Agent", USER_AGENT)
                conn.connect()
                String html = conn.getInputStream().getText(HTML_CHARSET)

                int status = conn.getResponseCode()
                LOG.trace("Connection status is : ${status}")

                if (status == 200 && html.contains(SUCCESS_MESSAGE)) {
                    return taskResult.result = TransmissionResult.SUCCESS
                }

            } finally {
                conn.disconnect()
            }
        } catch ( IOException ex ) {
            if (LOG.isDebugEnabled() || LOG.isTraceEnabled()) {
                LOG.warn "Cannot send message to mobile (${this.mobile}) because of ${ex.getMessage()}", ex
            } else {
                LOG.warn "Cannot send message to mobile (${this.mobile}) because of ${ex.getMessage()}"
            }
        }
        return taskResult
    }

}
