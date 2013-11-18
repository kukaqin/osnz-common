package nz.net.osnz.common.sms.utils

public enum TransmissionResult {
    SUCCESS('success', 'Sent message successfully'),
    FAILED('failed', 'Failed to send message'),
    OUTSIDE_NZ('outside_nz', 'Cannot send message because cellphone is outside of New Zealand'),
    BLANK_MESSAGE('blank_message', 'Cannot send message because the message is blank'),
    BLANK_CELLPHONE('blank_cellphone', 'Cannot send message because the number is blank'),
    WRONG_CELLPHONE('wrong_cellphone', 'Cannot send message because the number is incorrect'),
    TOO_LONG('too_long', 'Cannot send message because the message is too long (over 160 characters)'),
    BAD_FORMAT('bad_format', 'Cannot send message bcause the message contains some illegal characters'),
    NO_SUCH_CARRIER('no_such_carrier', 'Cannot send message because no such SMS provider')

    private final String code

    private final String message

    public TransmissionResult(String c, String m) {
        this.code = c
        this.message = m
    }

    @Override
    public String toString() {
        return this.message
    }

    public String getCode() {
        return this.code
    }

    public boolean isOK() {
        return this == TransmitStatus.SUCCESS
    }

}
