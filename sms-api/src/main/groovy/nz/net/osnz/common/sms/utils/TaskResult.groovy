package nz.net.osnz.common.sms.utils

/**
 * @author Kefeng Deng
 *
 * This class represent the result in thread
 */
class TaskResult {

    private String mobile

    private TransmissionResult result

    public TaskResult(String mobile, TransmissionResult result) {
        this.mobile = mobile
        this.result = result
    }

    public void setResult(TransmissionResult result) {
        this.result = result
    }

    public String getMobile() {
        return this.mobile
    }

    public TransmissionResult getResult() {
        return this.result
    }

}
