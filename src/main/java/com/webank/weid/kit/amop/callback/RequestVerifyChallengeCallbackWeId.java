

package com.webank.weid.kit.amop.callback;

import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.amop.request.RequestVerifyChallengeArgs;
import com.webank.weid.kit.protocol.response.RequestVerifyChallengeResponse;
import com.webank.weid.kit.auth.WeIdAuthImpl;
import com.webank.weid.kit.auth.WeIdAuth;
import com.webank.weid.kit.auth.WeIdAuthObj;
import com.webank.weid.protocol.base.Challenge;
import com.webank.weid.protocol.base.WeIdDocument;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.service.rpc.WeIdService;
import com.webank.weid.util.DataToolUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * amop callback for verifying challenge.
 *
 * @author tonychen 2020年3月10日
 */
public class RequestVerifyChallengeCallbackWeId extends WeIdAmopCallback {

    private static final Logger logger = LoggerFactory
        .getLogger(RequestVerifyChallengeCallbackWeId.class);

    private WeIdService weIdService = new WeIdServiceImpl();

    private WeIdAuth weIdAuthService = new WeIdAuthImpl();

    /**
     * 默认获取weIdAuthObj回调.
     *
     * @param args 获取weIdAuthObj需要的参数
     * @return 返回weIdAuthObj的响应体
     */
    public RequestVerifyChallengeResponse onPush(RequestVerifyChallengeArgs args) {

        RequestVerifyChallengeResponse result = new RequestVerifyChallengeResponse();
        String signData = args.getSignData();
        String weId = args.getSelfWeId();
        String channelId = args.getChannelId();
        Challenge challenge = args.getChallenge();
        WeIdAuthObj weidAuth = weIdAuthService.getWeIdAuthObjByChannelId(channelId);
        if (!StringUtils.equals(weidAuth.getCounterpartyWeId(), weId)) {

            logger.error("[RequestVerifyChallengeCallback] the weId :{} has no permission.", weId);
            result.setErrorCode(KitErrorCode.WEID_AUTH_NO_PERMISSION.getCode());
            result.setErrorMessage(KitErrorCode.WEID_AUTH_NO_PERMISSION.getCodeDesc());
            return result;
        }
        String rawData = challenge.toJson();
        com.webank.weid.blockchain.protocol.response.ResponseData<WeIdDocument> weIdDocResp = weIdService.getWeIdDocument(weId);
        com.webank.weid.blockchain.constant.ErrorCode errorCode = DataToolUtils
            .verifySignatureFromWeId(rawData, signData, weIdDocResp.getResult(), null);
        if (errorCode.getCode() != KitErrorCode.SUCCESS.getCode()) {
            logger.error("[RequestVerifyChallengeCallback] verify challenge signature failed.");
            result.setErrorCode(errorCode.getCode());
            result.setErrorMessage(errorCode.getCodeDesc());
            return result;
        }

        result.setErrorCode(KitErrorCode.SUCCESS.getCode());
        result.setErrorMessage(KitErrorCode.SUCCESS.getCodeDesc());
        return result;
    }
}
