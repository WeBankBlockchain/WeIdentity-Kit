package com.webank.weid.kit.amop.base;

import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.protocol.response.ResponseData;
import org.fisco.bcos.sdk.amop.AmopResponse;
import org.fisco.bcos.sdk.amop.AmopResponseCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmopBaseCallback<T> extends AmopResponseCallback {

    private static final Logger logger = LoggerFactory.getLogger(AmopBaseCallback.class);

    public ResponseData<T> responseStruct;


    /**
     * Constructor.
     */
    public AmopBaseCallback() { }

    @Override
    public void onResponse(AmopResponse response) {
        logger.info("direct route response, seq : {}, errorCode : {}, errorMsg : {}, messageIn: {}",
                response.getMessageID(),
                response.getErrorCode(),
                response.getErrorMessage(),
                response.getAmopMsgIn()
        );
        responseStruct = new ResponseData<>();
        if (102 == response.getErrorCode()) {
            responseStruct.setErrorCode(KitErrorCode.DIRECT_ROUTE_REQUEST_TIMEOUT);
        } else if (0 != response.getErrorCode()) {
            responseStruct.setErrorCode(KitErrorCode.DIRECT_ROUTE_MSG_BASE_ERROR);
        } else {
            responseStruct.setErrorCode(KitErrorCode.getTypeByErrorCode(response.getErrorCode()));
        }
    }
}
