

package com.webank.weid.kit.auth;

import com.webank.weid.constant.DataDriverConstant;
import com.webank.weid.constant.ParamKeyConstant;
import com.webank.weid.kit.amop.AmopService;
import com.webank.weid.kit.amop.AmopServiceImpl;
import com.webank.weid.kit.amop.request.GetWeIdAuthArgs;
import com.webank.weid.kit.amop.request.RequestVerifyChallengeArgs;
import com.webank.weid.kit.amop.callback.RequestVerifyChallengeCallbackWeId;
import com.webank.weid.kit.amop.callback.WeIdAuthWeIdAmopCallback;
import com.webank.weid.kit.amop.entity.AmopMsgType;
import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.protocol.response.GetWeIdAuthResponse;
import com.webank.weid.kit.protocol.response.RequestVerifyChallengeResponse;
import com.webank.weid.kit.protocol.response.ResponseData;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.protocol.base.Challenge;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.base.WeIdDocument;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.service.rpc.WeIdService;
import com.webank.weid.suite.persistence.Persistence;
import com.webank.weid.suite.persistence.mysql.driver.MysqlDriver;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * weIdAuth service.
 *
 * @author tonychen 2020年3月10日
 */
@Setter
@Getter
public class WeIdAuthImpl implements WeIdAuth {

    private static final Logger logger = LoggerFactory.getLogger(WeIdAuthImpl.class);

    /**
     * amop service instance.
     */
    private static AmopService amopService = new AmopServiceImpl();
    private static WeIdAuthCallback weIdAuthCallback;
    private static WeIdAuthWeIdAmopCallback weIdAuthAmopCallback = new WeIdAuthWeIdAmopCallback();
    private static RequestVerifyChallengeCallbackWeId VerifyChallengeCallback =
        new RequestVerifyChallengeCallbackWeId();

    private static Persistence dataDriver;
    /**
     * specify who has right to get weid auth.
     */
    private static List<String> whitelistWeId;

    static {
        amopService.registerCallback(
            AmopMsgType.GET_WEID_AUTH.getValue(),
            weIdAuthAmopCallback
        );
        amopService.registerCallback(AmopMsgType.REQUEST_VERIFY_CHALLENGE.getValue(),
            VerifyChallengeCallback);
    }

    private WeIdService weIdService = new WeIdServiceImpl();

    private static Persistence getDataDriver() {
        if (dataDriver == null) {
            dataDriver = new MysqlDriver();
        }
        return dataDriver;
    }

    /* (non-Javadoc)
     * @see com.webank.weid.suite.auth.inf.WeIdAuth#createAuthenticatedChannel(java.lang.String,
     * com.webank.weid.protocol.base.WeIdAuthentication)
     */
    @Override
    public ResponseData<WeIdAuthObj> createAuthenticatedChannel(
        String toAmopId,
        WeIdAuthentication weIdAuthentication) {

        if (StringUtils.isBlank(toAmopId) || weIdAuthentication == null) {

            logger.error("[createAuthenticatedChannel] illegal input!");
            return new ResponseData<WeIdAuthObj>(null, KitErrorCode.ILLEGAL_INPUT);
        }

        Challenge challenge = Challenge
            .create(String.valueOf(System.currentTimeMillis()), DataToolUtils.getRandomSalt());
        GetWeIdAuthArgs getWeIdAuthArgs = new GetWeIdAuthArgs();
        getWeIdAuthArgs.setChallenge(challenge);
        getWeIdAuthArgs.setWeId(weIdAuthentication.getWeId());
        //single auth
        getWeIdAuthArgs.setType(0);
        ResponseData<GetWeIdAuthResponse> weIdAuthObjResp = amopService
            .getWeIdAuth(toAmopId, getWeIdAuthArgs);
        Integer errCode = weIdAuthObjResp.getErrorCode();
        String errMsg = weIdAuthObjResp.getErrorMessage();
        if (errCode.intValue() != KitErrorCode.SUCCESS.getCode()) {
            logger.error(
                "[createAuthenticatedChannel] get weid auth object failed. error code: {}, "
                    + "error message is:{}",
                errCode, errMsg);
            return new ResponseData<WeIdAuthObj>(null, KitErrorCode.getTypeByErrorCode(errCode));
        }

        logger.info("[createAuthenticatedChannel] get weid auth object with success.");
        byte[] encryptData = weIdAuthObjResp.getResult().getData();
        //decrypt
        byte[] originalData = null;
        try {
            originalData = DataToolUtils
                .decrypt(encryptData, weIdAuthentication.getWeIdPrivateKey().getPrivateKey());
        } catch (Exception e) {
            logger.error(
                "[createAuthenticatedChannel] decrypt weid auth object failed.  "
                    + "error message is:{}",
                e);
            return new ResponseData<WeIdAuthObj>(null, KitErrorCode.DECRYPT_DATA_FAILED);
        }
        String dataStr = DataToolUtils.byteToString(originalData);
        Map<String, Object> dataMap = DataToolUtils.deserialize(dataStr, HashMap.class);
        String weidAuth = (String) dataMap.get(ParamKeyConstant.WEID_AUTH_OBJ);
        WeIdAuthObj weIdAuthObj = DataToolUtils.deserialize(weidAuth, WeIdAuthObj.class);
        String challengeSignData = (String) dataMap.get(ParamKeyConstant.WEID_AUTH_SIGN_DATA);
        String rawData = challenge.toJson();
        com.webank.weid.blockchain.protocol.response.ResponseData<WeIdDocument> weIdDoc = weIdService.getWeIdDocument(weIdAuthObj.getSelfWeId());
        Integer weidDocErrorCode = weIdDoc.getErrorCode();
        if (weidDocErrorCode != KitErrorCode.SUCCESS.getCode()) {
            logger
                .error("[createMutualAuthenticatedChannel] get weid document failed,"
                        + " Error code:{}",
                    weidDocErrorCode);
            return new ResponseData<WeIdAuthObj>(null,
                KitErrorCode.getTypeByErrorCode(weidDocErrorCode));
        }
        WeIdDocument weIdDocument = weIdDoc.getResult();
        com.webank.weid.blockchain.constant.ErrorCode verifyErrorCode = DataToolUtils
            .verifySignatureFromWeId(rawData, challengeSignData, weIdDocument, null);
        if (verifyErrorCode.getCode() != KitErrorCode.SUCCESS.getCode()) {
            return new ResponseData<WeIdAuthObj>(null, KitErrorCode.getTypeByErrorCode(verifyErrorCode.getCode()));
        }
        return new ResponseData<WeIdAuthObj>(weIdAuthObj, KitErrorCode.SUCCESS);
    }

    /* (non-Javadoc)
     * @see com.webank.weid.suite.auth.inf.WeIdAuth#createMutualAuthenticatedChannel(
     * java.lang.String, com.webank.weid.protocol.base.WeIdAuthentication)
     */
    @Override
    public ResponseData<WeIdAuthObj> createMutualAuthenticatedChannel(
        String toOrgId,
        WeIdAuthentication weIdAuthentication) {

        //检查参数
        if (StringUtils.isBlank(toOrgId) || weIdAuthentication == null) {

            logger.error("[createMutualAuthenticatedChannel] illegal input!");
            return new ResponseData<WeIdAuthObj>(null, KitErrorCode.ILLEGAL_INPUT);
        }

        //生成随机的challenge，发给对手方，进行challenge
        Challenge challenge = Challenge
            .create(String.valueOf(System.currentTimeMillis()), DataToolUtils.getRandomSalt());
        GetWeIdAuthArgs getWeIdAuthArgs = new GetWeIdAuthArgs();
        getWeIdAuthArgs.setChallenge(challenge);
        getWeIdAuthArgs.setWeId(weIdAuthentication.getWeId());
        //单向auth
        getWeIdAuthArgs.setType(1);
        ResponseData<GetWeIdAuthResponse> weIdAuthObjResp = amopService
            .getWeIdAuth(toOrgId, getWeIdAuthArgs);
        Integer errCode = weIdAuthObjResp.getErrorCode();
        String errMsg = weIdAuthObjResp.getErrorMessage();
        if (errCode.intValue() != KitErrorCode.SUCCESS.getCode()) {
            logger.error(
                "[createMutualAuthenticatedChannel] get weid auth object failed. "
                    + "error code: {}, error message is:{}",
                errCode, errMsg);
            return new ResponseData<WeIdAuthObj>(null, KitErrorCode.getTypeByErrorCode(errCode));
        }

        logger.info("[createMutualAuthenticatedChannel] get weid auth object with success.");

        //拿自己私钥解密对手方发来的auth相关的数据
        byte[] encryptData = weIdAuthObjResp.getResult().getData();
        //decrypt
        byte[] originalData = null;
        try {
            originalData = DataToolUtils
                .decrypt(encryptData, weIdAuthentication.getWeIdPrivateKey().getPrivateKey());
        } catch (Exception e) {
            logger.error("[createMutualAuthenticatedChannel] decrypt data failed, "
                + "message:{}", e);
            return new ResponseData<WeIdAuthObj>(null, KitErrorCode.DECRYPT_DATA_FAILED);
        }
        String dataStr = DataToolUtils.byteToString(originalData);
        Map<String, Object> dataMap = DataToolUtils.deserialize(dataStr, HashMap.class);
        String weidAuth = (String) dataMap.get(ParamKeyConstant.WEID_AUTH_OBJ);
        WeIdAuthObj weIdAuthObj = DataToolUtils.deserialize(weidAuth, WeIdAuthObj.class);

        String challengeSignData = (String) dataMap.get(ParamKeyConstant.WEID_AUTH_SIGN_DATA);
        String rawData = challenge.toJson();
        com.webank.weid.blockchain.protocol.response.ResponseData<WeIdDocument> weIdDoc = weIdService.getWeIdDocument(weIdAuthObj.getSelfWeId());
        Integer weidDocErrorCode = weIdDoc.getErrorCode();
        if (weidDocErrorCode != KitErrorCode.SUCCESS.getCode()) {
            logger
                .error("[createMutualAuthenticatedChannel] get weid document failed, "
                        + "Error code:{}",
                    weidDocErrorCode);
            return new ResponseData<WeIdAuthObj>(null,
                KitErrorCode.getTypeByErrorCode(weidDocErrorCode));
        }
        WeIdDocument weIdDocument = weIdDoc.getResult();

        //验证对手方对challenge的签名
        com.webank.weid.blockchain.constant.ErrorCode verifyErrorCode = DataToolUtils
            .verifySignatureFromWeId(rawData, challengeSignData, weIdDocument, null);
        if (verifyErrorCode.getCode() != KitErrorCode.SUCCESS.getCode()) {
            return new ResponseData<WeIdAuthObj>(null, KitErrorCode.getTypeByErrorCode(verifyErrorCode.getCode()));
        }

        //双向auth，发起方也需要对对手方的challenge进行签名
        String challenge1 = (String) dataMap.get(ParamKeyConstant.WEID_AUTH_CHALLENGE);
        /*String signData = DataToolUtils.secp256k1Sign(
            challenge1, new BigInteger(weIdAuthentication.getWeIdPrivateKey().getPrivateKey()));*/
        String signData = DataToolUtils.SigBase64Serialization(
                DataToolUtils.signToRsvSignature(rawData, weIdAuthentication.getWeIdPrivateKey().getPrivateKey())
        );
        RequestVerifyChallengeArgs verifyChallengeArgs = new RequestVerifyChallengeArgs();
        verifyChallengeArgs.setSignData(signData);
        verifyChallengeArgs.setChallenge(Challenge.fromJson(challenge1));
        verifyChallengeArgs.setChannelId(weIdAuthObj.getChannelId());
        ResponseData<RequestVerifyChallengeResponse> verifyResult = amopService
            .requestVerifyChallenge(toOrgId, verifyChallengeArgs);
        int code = verifyResult.getErrorCode();
        if (code != KitErrorCode.SUCCESS.getCode()) {
            logger.error(
                "[createMutualAuthenticatedChannel] request verify challenge signature "
                    + "failed, Error code:{}",
                code);
            return new ResponseData<WeIdAuthObj>(null, KitErrorCode.getTypeByErrorCode(code));
        }

        //都认证完之后，则可以将WeIdAuthObj数据返回给调用方
        return new ResponseData<WeIdAuthObj>(weIdAuthObj, KitErrorCode.SUCCESS);
    }


    /* (non-Javadoc)
     * @see com.webank.weid.suite.auth.inf.WeIdAuth#setWhiteList(java.util.List, java.util.List)
     */
    @Override
    public Integer setWhiteList(List<String> whiteWeIdlist) {

        if (whitelistWeId != null) {

            whitelistWeId = whiteWeIdlist;
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see com.webank.weid.suite.auth.inf.WeIdAuth#addWeIdAuthObj(
     * com.webank.weid.suite.auth.protocol.WeIdAuthObj)
     */
    @Override
    public Integer addWeIdAuthObj(WeIdAuthObj weIdAuthObj) {

        String weIdAuthData = DataToolUtils.serialize(weIdAuthObj);
        String channelId = weIdAuthObj.getChannelId();
        com.webank.weid.blockchain.protocol.response.ResponseData<Integer> dbResp = getDataDriver().addOrUpdate(
            DataDriverConstant.DOMAIN_WEID_AUTH,
            channelId,
            weIdAuthData);
        Integer errorCode = dbResp.getErrorCode();
        if (errorCode != KitErrorCode.SUCCESS.getCode()) {
            logger.error(
                "[addWeIdAuthObj] save weIdAuthObj to db failed, channel id:{}, error code is {}",
                channelId,
                errorCode);
            return errorCode;
        }
        return KitErrorCode.SUCCESS.getCode();
    }

    /* (non-Javadoc)
     * @see com.webank.weid.suite.auth.inf.WeIdAuth#getWeIdAuthObjByChannelId(java.lang.String)
     */
    @Override
    public WeIdAuthObj getWeIdAuthObjByChannelId(String channelId) {

        com.webank.weid.blockchain.protocol.response.ResponseData<String> dbResp = getDataDriver().get(
            DataDriverConstant.DOMAIN_WEID_AUTH,
            channelId);
        Integer errorCode = dbResp.getErrorCode();
        if (errorCode != KitErrorCode.SUCCESS.getCode()) {
            logger.error(
                "[addWeIdAuthObj] get weIdAuthObj from db failed, channel id:{}, error code is {}",
                channelId,
                errorCode);
            return null;
        }
        String weIdAuthJson = dbResp.getResult();
        WeIdAuthObj weIdAuthObj = DataToolUtils.deserialize(weIdAuthJson, WeIdAuthObj.class);
        return weIdAuthObj;
    }

    /* (non-Javadoc)
     * @see com.webank.weid.suite.auth.inf.WeIdAuth#registerCallBack(
     * com.webank.weid.suite.auth.inf.WeIdAuthCallback)
     */
    @Override
    public Integer registerCallBack(WeIdAuthCallback callback) {

        weIdAuthCallback = callback;
        return 0;
    }

    /* (non-Javadoc)
     * @see com.webank.weid.suite.auth.inf.WeIdAuth#getCallBack()
     */
    @Override
    public WeIdAuthCallback getCallBack() {

        return weIdAuthCallback;
    }

}
