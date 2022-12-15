

package com.webank.weid.kit.amop.callback;

import java.util.HashMap;
import java.util.Map;

import com.webank.weid.kit.amop.entity.AmopMsgType;
import com.webank.weid.kit.amop.base.AmopRequestBody;
import org.fisco.bcos.sdk.amop.topic.AmopMsgIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.kit.util.KitUtils;

/**
 * Created by junqizhang on 08/07/2017.
 */
/*public class OnNotifyCallbackV2 extends ChannelPushCallback implements RegistCallBack {*/
public class OnNotifyCallbackV2
        extends org.fisco.bcos.sdk.amop.AmopCallback
        implements RegistCallBack {

    private static final Logger logger = LoggerFactory.getLogger(OnNotifyCallbackV2.class);

    private Map<Integer, WeIdAmopCallback> amopCallBackMap = new HashMap<Integer, WeIdAmopCallback>();

    private WeIdAmopCallback defaultWeIdAmopCallBack = new WeIdAmopCallback();

    public void registAmopCallback(Integer msgType, WeIdAmopCallback routeCallBack) {
        amopCallBackMap.put(msgType, routeCallBack);
    }
    
    public WeIdAmopCallback getAmopCallback(Integer msgType) {
        return amopCallBackMap.get(msgType);
    }
    
    @Override
    public byte[] receiveAmopMsg(AmopMsgIn amopMsgIn) {
        String content = new String(amopMsgIn.getContent());
        logger.info("received ChannelPush v2 msg : " + content);
        if (0 == amopCallBackMap.size()) {
            return "directRouteCallback is null on server side!".getBytes();
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
            //result = msgType.callOnPush(amopCallBack, push.getMessageID(), messageBody);
            result = msgType.callOnPush(weIdAmopCallBack, amopMsgIn.getMessageID(), messageBody);
        } catch (Exception e) {
            logger.error("callOnPush error, please check the log.", e);
            return null;
        }
         return result.getBytes();
    }
}
