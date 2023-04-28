

package com.webank.weid.kit.exception;

import com.webank.weid.blockchain.constant.ErrorCode;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.kit.constant.KitErrorCode;

/**
 * 协议处理异常.
 * @author v_wbgyang
 *
 */
public class ProtocolSuiteException extends WeIdBaseException {

    private static final long serialVersionUID = 1L;
    
    private ErrorCode errorCode;

    public ProtocolSuiteException(ErrorCode errorCode) {
        super(errorCode.getCodeDesc());
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return this.errorCode;
    }
}
