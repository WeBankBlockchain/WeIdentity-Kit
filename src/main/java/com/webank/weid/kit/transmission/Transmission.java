

package com.webank.weid.kit.transmission;

import com.webank.weid.kit.protocol.response.ResponseData;

/**
 * 传输处理器公共接口.
 * 
 * @author yanggang
 *
 */
public interface Transmission {
    
    /**
     * 传输处理器公共请求接口.
     * 
     * @param request 请求数据
     * @return 返回处理结果
     */
    public ResponseData<String> send(TransmissionRequest<?> request);
}
