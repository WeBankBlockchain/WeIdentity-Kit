package com.webank.weid.kit.protocol.response;

import lombok.Getter;
import lombok.Setter;

/**
 * get weIdAuth response.
 * @author tonychen 2020年3月10日
 *
 */
@Getter
@Setter
public class GetWeIdAuthResponse {

    /**
     * encrypt data, including challenge sign, weIdAuthObj.
     */
    private byte[] data;
    
    /**
     * error code.
     */
    private Integer errorCode;

    /**
     * error message.
     */
    protected String errorMessage;
}
