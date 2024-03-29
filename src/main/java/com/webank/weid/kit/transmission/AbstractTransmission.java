

package com.webank.weid.kit.transmission;

import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.kit.protocol.response.ResponseData;
import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.kit.crypto.CryptoServiceFactory;
import com.webank.weid.kit.crypto.params.CryptoType;
import com.webank.weid.kit.auth.WeIdAuthImpl;
import com.webank.weid.kit.auth.WeIdAuth;
import com.webank.weid.kit.auth.WeIdAuthObj;
import com.webank.weid.kit.util.KitUtils;

/**
 * 传输公共处理类.
 * 
 * @author yanggang
 *
 */
public abstract class AbstractTransmission implements Transmission {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTransmission.class);

    private static WeIdAuth weIdAuthService;

    private WeIdAuth getWeIdAuthService() {
        if (weIdAuthService == null) {
            weIdAuthService = new WeIdAuthImpl();
        }
        return weIdAuthService;
    }

    /**
     * 认证处理.
     * 
     * @param <T> 请求实例中具体数据类型
     * @param request 用于做weAuth验证的用户身份信息
     * @return 返回加密后的数据对象
     */
    protected <T> TransmissionlRequestWarp<T> authTransmission(TransmissionRequest<T> request) {
        logger.info("[AbstractTransmission.auth] begin auth the transmission.");
        ResponseData<WeIdAuthObj> authResponse = getWeIdAuthService().createAuthenticatedChannel(
            request.getAmopId(), request.getWeIdAuthentication());
        if (authResponse.getErrorCode().intValue() != KitErrorCode.SUCCESS.getCode()) {
            //认证失败
            logger.error("[AbstractTransmission.auth] auth fail:{}-{}.",
                authResponse.getErrorCode(),
                authResponse.getErrorMessage());
            throw new WeIdBaseException(com.webank.weid.blockchain.constant.ErrorCode.getTypeByErrorCode(authResponse.getErrorCode()));
        }
        logger.info("[AbstractTransmission.auth] auth the transmission successfully.");
        WeIdAuthObj weIdAuth = authResponse.getResult();
        TransmissionlRequestWarp<T> reqeustWarp = new TransmissionlRequestWarp<T>(
            request, weIdAuth);
        String encodeData = encryptData(getOriginalData(request.getArgs()), weIdAuth);
        reqeustWarp.setEncodeData(encodeData);
        return reqeustWarp;
    }

    /**
     * 解密数据.
     * 
     * @param encodeData 密文数据
     * @param weIdAuth 通道协议对象
     * @return 返回明文数据
     */
    protected String decryptData(String encodeData, WeIdAuthObj weIdAuth) {
        return CryptoServiceFactory
            .getCryptoService(CryptoType.AES)
            .decrypt(encodeData, weIdAuth.getSymmetricKey());
    }

    /**
     * 加密传输数据.
     * 
     * @param originalData 原文
     * @param weIdAuth 通道协议对象
     * @return 返回加密后的数据
     */
    protected String encryptData(String originalData, WeIdAuthObj weIdAuth) {
        return CryptoServiceFactory
            .getCryptoService(CryptoType.AES)
            .encrypt(originalData, weIdAuth.getSymmetricKey());
    }

    protected <T> String getOriginalData(T args) {
        String originalData = null;
        if (args instanceof String) {
            originalData = (String)args;
        } else {
            originalData = KitUtils.serialize(args);
        }
        return originalData;
    }

    @Data
    protected class TransmissionlRequestWarp<T> {
        
        TransmissionRequest<T> request;
        WeIdAuthObj weIdAuthObj;
        String encodeData;
        
        TransmissionlRequestWarp(TransmissionRequest<T> request, WeIdAuthObj weIdAuthObj) {
            this.request = request;
            this.weIdAuthObj = weIdAuthObj;
        }
    }
}
