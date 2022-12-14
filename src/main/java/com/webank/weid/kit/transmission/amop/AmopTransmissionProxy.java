

package com.webank.weid.kit.transmission.amop;

import com.webank.weid.kit.amop.entity.AmopMsgType;
import com.webank.weid.kit.amop.base.AmopResponse;
import com.webank.weid.kit.protocol.response.ResponseData;
import com.webank.weid.kit.amop.AmopServiceImpl;
import com.webank.weid.kit.amop.base.AmopCommonArgs;
import com.webank.weid.kit.amop.callback.CommonCallbackWeId;
import com.webank.weid.kit.amop.AmopService;
import com.webank.weid.kit.transmission.TransmissionServiceCenter;

/**
 * AMOP处理器代理类.
 * 
 * @author yanggang
 *
 */
public class AmopTransmissionProxy {

    private static AmopService amopService;
    
    /**
     * 获取AMOP服务.
     * 
     * @return 返回AMOP服务
     */
    public AmopService getAmopService() {
        if (amopService == null) {
            amopService = new AmopServiceImpl();
        }
        return amopService;
    }

    /**
     * 发送AMOP远程服务.
     * 
     * @param amopCommonArgs AMOP公共请求参数
     * @return 返回AMOP处理结果
     */
    public ResponseData<AmopResponse> send(AmopCommonArgs amopCommonArgs) {
        return getAmopService().send(amopCommonArgs.getTopic(), amopCommonArgs);
    }
    
    /**
     * AMOP本地服务.
     * 
     * @param amopCommonArgs AMOP公共请求参数
     * @return 返回处理结果
     */
    public AmopResponse sendLocal(AmopCommonArgs amopCommonArgs) {
        ResponseData<?> response = TransmissionServiceCenter.getService(
            amopCommonArgs.getServiceType()).service(amopCommonArgs.getMessage());
        return ((CommonCallbackWeId)getAmopService().getPushCallback().getAmopCallback(
            AmopMsgType.COMMON_REQUEST.getValue())).buildAmopResponse(response, amopCommonArgs);
    }
    
    /**
     * 获取当前机构.
     * 
     * @return 返回当前机构名称
     */
    public String getCurrentAmopId() {
        return com.webank.weid.blockchain.service.fisco.BaseServiceFisco.fiscoConfig.getAmopId();
    }
}
