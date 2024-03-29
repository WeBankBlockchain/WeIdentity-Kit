package com.webank.weid.kit.amop.callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.webank.weid.constant.ParamKeyConstant;
import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.amop.request.GetWeIdAuthArgs;
import com.webank.weid.kit.protocol.response.GetWeIdAuthResponse;
import com.webank.weid.protocol.base.*;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.service.rpc.WeIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.kit.auth.WeIdAuthImpl;
import com.webank.weid.kit.auth.WeIdAuth;
import com.webank.weid.kit.auth.WeIdAuthObj;
import com.webank.weid.util.DataToolUtils;


/**
 * amop callback for weIdAuth.
 * @author tonychen 2020年3月10日
 */
public class WeIdAuthWeIdAmopCallback extends WeIdAmopCallback {

    private static final Logger logger = LoggerFactory.getLogger(WeIdAuthWeIdAmopCallback.class);

    private WeIdService weIdService = new WeIdServiceImpl();

    private WeIdAuth weIdAuthService = new WeIdAuthImpl();


    /**
     * 默认获取weIdAuthObj回调.
     *
     * @param args 获取weIdAuthObj需要的参数
     * @return 返回weIdAuthObj的响应体
     */
    public GetWeIdAuthResponse onPush(GetWeIdAuthArgs args) {

        String fromWeId = args.getWeId();
        //call callback
        WeIdAuthentication weIdAuth = weIdAuthService.getCallBack().onChannelConnecting(fromWeId);
        GetWeIdAuthResponse result = new GetWeIdAuthResponse();

        //1. sign the data(challenge) with self private key to finish this challenge.
        Map<String, Object> dataMap = new HashMap<String, Object>();
        Challenge challenge = args.getChallenge();
        String rawData = challenge.toJson();
        String privateKey = weIdAuth.getWeIdPrivateKey().getPrivateKey();
        //String challengeSign = DataToolUtils.secp256k1Sign(rawData, new BigInteger(privateKey));
        String challengeSign = DataToolUtils.SigBase64Serialization(
                DataToolUtils.signToRsvSignature(rawData, privateKey)
        );
        dataMap.put(ParamKeyConstant.WEID_AUTH_SIGN_DATA, challengeSign);

        com.webank.weid.blockchain.protocol.response.ResponseData<WeIdDocument> weIdDocResp = weIdService.getWeIdDocument(fromWeId);
        if (weIdDocResp.getErrorCode() != KitErrorCode.SUCCESS.getCode()) {
            logger.error("[WeIdAuthCallback->onPush] get weid document by weid ->{} failed.",
                fromWeId);
            result.setErrorCode(weIdDocResp.getErrorCode());
            result.setErrorMessage(weIdDocResp.getErrorMessage());
            return result;
        }
        WeIdDocument document = weIdDocResp.getResult();
        List<AuthenticationProperty> authList = document.getAuthentication();
        //PublicKeyMultibase直接解码
        String pubKey = authList.get(0).getPublicKey();
        //2. generate a symmetricKey
        String symmetricKey = UUID.randomUUID().toString();
        String channelId = UUID.randomUUID().toString();
        WeIdAuthObj weIdAuthObj = new WeIdAuthObj();
        weIdAuthObj.setSymmetricKey(symmetricKey);
        weIdAuthObj.setCounterpartyWeId(fromWeId);
        weIdAuthObj.setSelfWeId(weIdAuth.getWeId());
        weIdAuthObj.setChannelId(channelId);

        Integer type = args.getType();
        //mutual
        if (type == 1) {
            Challenge challenge1 = Challenge.create(fromWeId, DataToolUtils.getRandomSalt());
            dataMap.put(ParamKeyConstant.WEID_AUTH_CHALLENGE, challenge1.toJson());
        }

        //将weidAuth对象缓存
        weIdAuthService.addWeIdAuthObj(weIdAuthObj);
        dataMap.put(ParamKeyConstant.WEID_AUTH_OBJ, DataToolUtils.serialize(weIdAuthObj));

        //3. use fromWeId's public key to encrypt data
        String data = DataToolUtils.serialize(dataMap);
        byte[] encryptData = null;
        try {
            encryptData = DataToolUtils.encrypt(data, pubKey);
        } catch (Exception e) {
            logger.error("[WeIdAuthCallback] encrypt data failed.{}", e);
            result.setErrorCode(KitErrorCode.ENCRYPT_DATA_FAILED.getCode());
            result.setErrorMessage(KitErrorCode.ENCRYPT_DATA_FAILED.getCodeDesc());
            return result;
        }
        result.setData(encryptData);

        //call callback
        result.setErrorCode(KitErrorCode.SUCCESS.getCode());
        result.setErrorMessage(KitErrorCode.SUCCESS.getCodeDesc());
        weIdAuthService.getCallBack().onChannelConnected(weIdAuthObj);
        return result;
    }


}
