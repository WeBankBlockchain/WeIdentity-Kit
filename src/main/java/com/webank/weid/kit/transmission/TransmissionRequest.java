

package com.webank.weid.kit.transmission;

import com.webank.weid.protocol.base.WeIdAuthentication;
import lombok.Data;

import com.webank.weid.kit.transmission.entity.TransType;

@Data
public class TransmissionRequest<T> {
    private String amopId;
    private T args;
    private String serviceType;
    private TransType transType;
    private WeIdAuthentication weIdAuthentication;
}
