package com.webank.weid.kit.amop.callback;

import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.amop.base.AmopNotifyMsgResult;
import com.webank.weid.kit.amop.base.AmopBaseCallback;
import com.webank.weid.kit.util.KitUtils;
import org.fisco.bcos.sdk.amop.AmopResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmopNotifyMsgCallback extends AmopBaseCallback {

    private static final Logger logger = LoggerFactory.getLogger(AmopNotifyMsgCallback.class);
    public AmopNotifyMsgResult amopNotifyMsgResult;

    public AmopNotifyMsgCallback() {

    }

    @Override
    public void onResponse(AmopResponse response) {
        super.onResponse(response);
        amopNotifyMsgResult = KitUtils.deserialize(response.getAmopMsgIn().getContent().toString(),
                AmopNotifyMsgResult.class);
        if (null == amopNotifyMsgResult) {
            super.responseStruct.setErrorCode(KitErrorCode.UNKNOW_ERROR);
        }
    }
}
