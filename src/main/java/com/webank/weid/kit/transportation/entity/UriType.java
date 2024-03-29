

package com.webank.weid.kit.transportation.entity;

import com.webank.weid.blockchain.constant.ErrorCode;
import com.webank.weid.exception.WeIdBaseException;

/**
 * 协议URI类型.
 * 
 * @author yanggang
 *
 */
public enum UriType {
    /**
     * 协议为机构协议,表明协议后面的为机构名称.
     */
    ORG(0);
    
    private Integer code;

    UriType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    /**
     * 根据编号获取枚举值.
     *
     * @param code 类型对应的编码
     * @return UriType 返回枚举值
     */
    public static UriType getUriByCode(Integer code) {
        for (UriType type : UriType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new WeIdBaseException(ErrorCode.TRANSPORTATION_URI_TYPE_INVALID);
    }
}
