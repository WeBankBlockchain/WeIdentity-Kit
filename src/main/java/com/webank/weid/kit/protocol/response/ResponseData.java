

package com.webank.weid.kit.protocol.response;

import com.webank.weid.blockchain.protocol.response.TransactionInfo;
import lombok.Data;

import com.webank.weid.kit.constant.KitErrorCode;

/**
 * The internal base response result class.
 *
 * @param <T> the generic type
 * @author tonychen 2018.9.29
 */
@Data
public class ResponseData<T> {

    /**
     * The generic type result object.
     */
    private T result;

    /**
     * The error code.
     */
    private Integer errorCode;

    /**
     * The error message.
     */
    private String errorMessage;

    /**
     * The blockchain transaction info. Note that this transaction only becomes valid (not null nor
     * blank) when a successful transaction is sent to chain with a block generated.
     */
    private TransactionInfo transactionInfo = null;

    /**
     * Instantiates a new response data.
     */
    public ResponseData() {
        this.setErrorCode(KitErrorCode.SUCCESS);
    }

    /**
     * Instantiates a new response data. Transaction info is left null to avoid unnecessary boxing.
     *
     * @param result the result
     * @param kitErrorCode the return code
     */
    public ResponseData(T result, KitErrorCode kitErrorCode) {
        this.result = result;
        if (kitErrorCode != null) {
            this.errorCode = kitErrorCode.getCode();
            this.errorMessage = kitErrorCode.getCodeDesc();
        }
    }

    /**
     * Instantiates a new response data with transaction info.
     *
     * @param result the result
     * @param kitErrorCode the return code
     * @param transactionInfo transactionInfo
     */
    public ResponseData(T result, KitErrorCode kitErrorCode, TransactionInfo transactionInfo) {
        this.result = result;
        if (kitErrorCode != null) {
            this.errorCode = kitErrorCode.getCode();
            this.errorMessage = kitErrorCode.getCodeDesc();
        }
        if (transactionInfo != null) {
            this.transactionInfo = transactionInfo;
        }
    }

    /**
     * set a ErrorCode type errorCode.
     * 
     * @param kitErrorCode the errorCode
     */
    public void setErrorCode(KitErrorCode kitErrorCode) {
        if (kitErrorCode != null) {
            this.errorCode = kitErrorCode.getCode();
            this.errorMessage = kitErrorCode.getCodeDesc();
        }
    }

    /**
     * Instantiates a new Response data based on the error code and error message.
     * 
     * @param result the result
     * @param errorCode code number
     * @param errorMessage errorMessage
     */
    public ResponseData(T result, Integer errorCode, String errorMessage) {
        this.result = result;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
