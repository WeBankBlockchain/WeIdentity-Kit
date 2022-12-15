

package com.webank.weid.kit.transportation.entity;

import lombok.Getter;
import lombok.Setter;

import com.webank.weid.kit.transportation.entity.TransBaseData;

/**
 * JSON协议实体.
 * @author v_wbgyang
 *
 */
@Getter
@Setter
public class JsonBaseData extends TransBaseData {

    /**
     * 协议通讯类型.
     */
    @Deprecated
    private String type = "AMOP";
}
