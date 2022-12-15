

package com.webank.weid.kit.amop.callback;

import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.amop.base.AmopCommonArgs;
import com.webank.weid.kit.amop.request.CheckAmopMsgHealthArgs;
import com.webank.weid.kit.amop.request.GetEncryptKeyArgs;
import com.webank.weid.kit.amop.request.GetPolicyAndChallengeArgs;
import com.webank.weid.kit.amop.request.GetPolicyAndPreCredentialArgs;
import com.webank.weid.kit.amop.request.GetWeIdAuthArgs;
import com.webank.weid.kit.amop.request.IssueCredentialArgs;
import com.webank.weid.kit.amop.request.RequestVerifyChallengeArgs;
import com.webank.weid.kit.amop.base.AmopNotifyMsgResult;
import com.webank.weid.kit.amop.base.AmopResponse;
import com.webank.weid.kit.protocol.response.GetEncryptKeyResponse;
import com.webank.weid.kit.protocol.response.GetPolicyAndChallengeResponse;
import com.webank.weid.kit.protocol.response.GetWeIdAuthResponse;
import com.webank.weid.kit.protocol.response.PolicyAndPreCredentialResponse;
import com.webank.weid.kit.protocol.response.RequestIssueCredentialResponse;
import com.webank.weid.kit.protocol.response.RequestVerifyChallengeResponse;

/**
 * Created by junqizhang on 08/07/2017. 业务方需要继承DirectRouteCallback，并实现需要实现的方法.
 */
public class WeIdAmopCallback implements PushNotifyAllCallback {

    private static final String MSG_HEALTH = "I am alive!";
    private static final String ERROR_MSG_NO_OVERRIDE =
        "server side have not handle this type of message!";

    @Override
    public AmopNotifyMsgResult onPush(CheckAmopMsgHealthArgs arg) {

        AmopNotifyMsgResult result = new AmopNotifyMsgResult();
        result.setMessage(MSG_HEALTH);
        result.setErrorCode(KitErrorCode.SUCCESS.getCode());
        result.setMessage(KitErrorCode.SUCCESS.getCodeDesc());
        return result;
    }

    /**
     * 默认针对TYPE_TRANSPORTATION消息的回调处理.
     *
     * @param arg AMOP请求参数
     * @return AMOP相应体
     */
    public AmopResponse onPush(AmopCommonArgs arg) {

        AmopResponse result = new AmopResponse();
        result.setErrorCode(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCode());
        result.setErrorMessage(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCodeDesc());
        return result;
    }

    /**
     * 默认获取秘钥的回调处理.
     *
     * @param arg 获取秘钥需要的参数
     * @return 返回秘钥的响应体
     */
    public GetEncryptKeyResponse onPush(GetEncryptKeyArgs arg) {

        GetEncryptKeyResponse result = new GetEncryptKeyResponse();
        result.setErrorCode(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCode());
        result.setErrorMessage(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCodeDesc());
        return result;
    }

    /**
     * 默认获取PolicyAndChallenge的回调处理.
     *
     * @param arg 获取PolicyAndChallenge需要的参数
     * @return 返回PolicyAndChallenge的响应体
     */
    public GetPolicyAndChallengeResponse onPush(GetPolicyAndChallengeArgs arg) {

        GetPolicyAndChallengeResponse result = new GetPolicyAndChallengeResponse();
        result.setErrorCode(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCode());
        result.setErrorMessage(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCodeDesc());
        return result;
    }

    /**
     * 默认获取PolicyAndChallenge的回调处理.
     *
     * @param args 获取PolicyAndChallenge需要的参数
     * @return 返回PolicyAndChallenge的响应体
     */
    public PolicyAndPreCredentialResponse onPush(GetPolicyAndPreCredentialArgs args) {

        PolicyAndPreCredentialResponse result = new PolicyAndPreCredentialResponse();
        result.setErrorCode(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCode());
        result.setErrorMessage(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCodeDesc());
        return result;
    }

    /**
     * 默认获取PolicyAndChallenge的回调处理.
     *
     * @param args 获取PolicyAndChallenge需要的参数
     * @return 返回PolicyAndChallenge的响应体
     */
    public RequestIssueCredentialResponse onPush(IssueCredentialArgs args) {

        RequestIssueCredentialResponse result = new RequestIssueCredentialResponse();
        result.setErrorCode(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCode());
        result.setErrorMessage(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCodeDesc());
        return result;
    }

    /**
     * 默认获取weIdAuthObj回调.
     *
     * @param args 获取weIdAuthObj需要的参数
     * @return 返回weIdAuthObj的响应体
     */
    public GetWeIdAuthResponse onPush(GetWeIdAuthArgs args) {

        GetWeIdAuthResponse result = new GetWeIdAuthResponse();
        result.setErrorCode(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCode());
        result.setErrorMessage(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCodeDesc());
        return result;
    }

    /**
     * 默认获取weIdAuthObj回调.
     *
     * @param args 获取weIdAuthObj需要的参数
     * @return 返回weIdAuthObj的响应体
     */
    public RequestVerifyChallengeResponse onPush(RequestVerifyChallengeArgs args) {

        RequestVerifyChallengeResponse result = new RequestVerifyChallengeResponse();
        result.setErrorCode(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCode());
        result.setErrorMessage(KitErrorCode.AMOP_MSG_CALLBACK_SERVER_SIDE_NO_HANDLE.getCodeDesc());
        return result;
    }
}
