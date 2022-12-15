

package com.webank.weid.kit.amop.callback;

import com.webank.weid.kit.amop.request.CheckAmopMsgHealthArgs;
import com.webank.weid.kit.amop.base.AmopNotifyMsgResult;

/**
 * Created by junqizhang on 17/5/24.
 */
public interface PushNotifyAllCallback {


    /**
     * 链上链下health check, 不需要覆盖实现.
     *
     * @param arg echo arg
     * @return amopNotifyMsgResult
     */
    AmopNotifyMsgResult onPush(CheckAmopMsgHealthArgs arg);
}
