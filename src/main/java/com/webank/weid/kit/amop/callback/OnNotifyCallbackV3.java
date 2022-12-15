

package com.webank.weid.kit.amop.callback;

import com.webank.weid.kit.amop.entity.AmopMsgType;
import com.webank.weid.kit.amop.base.AmopRequestBody;
import com.webank.weid.kit.util.KitUtils;
import java.util.HashMap;
import java.util.Map;

import org.fisco.bcos.sdk.jni.amop.AmopRequestCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by junqizhang on 08/07/2017.
 */
public class OnNotifyCallbackV3
        implements AmopRequestCallback, RegistCallBack {

    private static final Logger logger = LoggerFactory.getLogger(OnNotifyCallbackV3.class);

    private Map<Integer, WeIdAmopCallback> amopCallBackMap = new HashMap<Integer, WeIdAmopCallback>();

    private WeIdAmopCallback defaultWeIdAmopCallBack = new WeIdAmopCallback();

    @Override
    public void registAmopCallback(Integer msgType, WeIdAmopCallback routeCallBack) {
        amopCallBackMap.put(msgType, routeCallBack);
    }
    
    @Override
    public WeIdAmopCallback getAmopCallback(Integer msgType) {
        return amopCallBackMap.get(msgType);
    }


    /**
     * recieve amop request message
     *
     * @param endpoint nodeIpPort
     * @param seq 序列号
     * @param data 数据
     */
    @Override
    public void onRequest(String endpoint, String seq, byte[] data) {
         // todo 支持amop callback, 怎么返回result
        String content = new String(data);
        logger.info("received ChannelPush v2 from:{},seq:{},msg:{} ", endpoint, seq, content);
        if (0 == amopCallBackMap.size()) {
            logger.warn("directRouteCallback is null on server side!");
            return;
//            return "directRouteCallback is null on server side!".getBytes();
        }

        AmopRequestBody amopRequestBody = KitUtils.deserialize(content, AmopRequestBody.class);
        AmopMsgType msgType = amopRequestBody.getMsgType();
        WeIdAmopCallback weIdAmopCallBack = amopCallBackMap.get(msgType.getValue());
        if (weIdAmopCallBack == null) {
            weIdAmopCallBack = defaultWeIdAmopCallBack;
        }
        String messageBody = amopRequestBody.getMsgBody();
        String result = null;
        try {
            result = msgType.callOnPush(weIdAmopCallBack, null, messageBody);
        } catch (Exception e) {
            logger.error("callOnPush error, please check the log.", e);
        }


        //接收到以后需要给发送端回包 todo
//        return result.getBytes();
    }

}
