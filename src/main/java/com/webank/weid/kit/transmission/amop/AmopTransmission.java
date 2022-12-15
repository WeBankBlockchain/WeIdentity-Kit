

package com.webank.weid.kit.transmission.amop;

import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.kit.amop.base.AmopResponse;
import com.webank.weid.kit.protocol.response.ResponseData;
import com.webank.weid.kit.amop.base.AmopCommonArgs;
import com.webank.weid.kit.transmission.TransmissionRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.kit.transmission.AbstractTransmission;
import com.webank.weid.kit.transmission.Transmission;
import com.webank.weid.util.DataToolUtils;

/**
 * AMOP传输处理器.
 * 
 * @author yanggang
 *
 */
public class AmopTransmission extends AbstractTransmission implements Transmission {

    private static final Logger logger = LoggerFactory.getLogger(AmopTransmission.class);

    private static AmopTransmissionProxy amopTransmissionPoxy;

    private void initAmopChannelPoxy() {
        if (amopTransmissionPoxy == null) {
            amopTransmissionPoxy = new AmopTransmissionProxy();
        }
    }

    @Override
    public ResponseData<String> send(TransmissionRequest<?> request) {
        logger.info(
            "[AmopTransmission.send] this is amop transmission and the service type is: {}", 
            request.getServiceType());
        try {
            initAmopChannelPoxy();
            //如果请求机构和目标机构为同机构，则走本地模式，不加密
            if (amopTransmissionPoxy.getCurrentAmopId().equals(request.getAmopId())) {
                return sendLocal(request);
            } else {
                return sendAmop(request);
            } 
        } catch (WeIdBaseException e) {
            logger.error("[AmopTransmission.send] send amop fail.", e);
            return new ResponseData<String>(StringUtils.EMPTY, KitErrorCode.getTypeByErrorCode(e.getErrorCode().getCode()));
        } catch (Exception e) {
            logger.error("[AmopTransmission.send] send amop due to unknown error.", e);
            return new ResponseData<String>(StringUtils.EMPTY, KitErrorCode.UNKNOW_ERROR);
        }
    }

    private ResponseData<String> sendLocal(TransmissionRequest<?> request) {
        logger.info("[AmopTransmission.sendLocal] request to local.");
        AmopResponse response = amopTransmissionPoxy.sendLocal(buildAmopCommonArgs(request));
        logger.info("[AmopTransmission.sendLocal] the amop response: {}", response);
        return new ResponseData<String>(response.getResult(), 
            response.getErrorCode(), response.getErrorMessage());
    }

    private ResponseData<String> sendAmop(TransmissionRequest<?> request) {
        logger.info("[AmopTransmission.sendAmop] request by AMOP.");
        // 此处由于集群环境下的bug，暂时不走认证通道
        // TransmissionlRequestWarp<?> requestWarp = super.authTransmission(request);
        AmopCommonArgs amopCommonArgs = buildAmopCommonArgs(request);
        logger.info("[AmopTransmission.sendAmop] messageId:{}, request: {}", 
            amopCommonArgs.getMessageId(), amopCommonArgs);
        ResponseData<AmopResponse> amopResponse = amopTransmissionPoxy.send(amopCommonArgs);
        logger.info("[AmopTransmission.sendAmop] messageId:{}, response: {}.", 
            amopResponse.getResult().getMessageId(), amopResponse);
        ResponseData<String> response = processResult(amopResponse);
        /*
        // 数据不走认证通道 不需要解密
        if (response.getErrorCode().intValue() == ErrorCode.SUCCESS.getCode()) {
            //请求成功解密数据
            String original = super.decryptData(response.getResult(), requestWarp.getWeIdAuthObj());
            response.setResult(original);
        }
        */
        return response;
    }
    
    /*
    private AmopCommonArgs buildAmopCommonArgs(TransmissionlRequestWarp<?> requestWarp) {
        AmopCommonArgs amopCommonArgs = buildAmopCommonArgs(requestWarp.getRequest());
        amopCommonArgs.setChannelId(requestWarp.getWeIdAuthObj().getChannelId());
        amopCommonArgs.setMessage(requestWarp.getEncodeData());
        return amopCommonArgs;
    }
    */
    
    private AmopCommonArgs buildAmopCommonArgs(TransmissionRequest<?> request) {
        AmopCommonArgs args = new AmopCommonArgs();
        args.setServiceType(request.getServiceType());
        args.setFromAmopId(amopTransmissionPoxy.getCurrentAmopId());
        args.setMessage(super.getOriginalData(request.getArgs()));
        args.setTopic(request.getAmopId());
        args.setMessageId(DataToolUtils.getUuId32());
        return args;
    }

    private ResponseData<String> processResult(ResponseData<AmopResponse> response) {
        if (response.getErrorCode().intValue() != KitErrorCode.SUCCESS.getCode()) {
            return new ResponseData<String>(StringUtils.EMPTY, 
                response.getErrorCode(), response.getErrorMessage());
        }
        AmopResponse amopResponse = response.getResult();
        return new ResponseData<String>(amopResponse.getResult(), 
            amopResponse.getErrorCode(), amopResponse.getErrorMessage());
    }
}
