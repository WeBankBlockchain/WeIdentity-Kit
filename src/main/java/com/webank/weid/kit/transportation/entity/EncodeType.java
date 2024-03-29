

package com.webank.weid.kit.transportation.entity;

import com.webank.weid.exception.WeIdBaseException;

/**
 * enumeration of supported coding types.
 * 
 * @author v_wbgyang
 *
 */
public enum EncodeType {

    /**
     * The original type.
     */
    ORIGINAL(0),
    
    /**
     * The cipher type.
     */
    CIPHER(1);
    
    /**
     * encode number.
     */
    private int code;

    
    EncodeType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    
    /**
     * get EncodeType by code.
     * @param code code value
     * @return EncodeType
     */
    public static EncodeType getEncodeType(int code) {
        for (EncodeType codeType : EncodeType.values()) {
            if (code == codeType.getCode()) {
                return codeType;
            }
        }
        throw new WeIdBaseException(com.webank.weid.blockchain.constant.ErrorCode.TRANSPORTATION_PROTOCOL_ENCODE_ERROR);
    }
}
