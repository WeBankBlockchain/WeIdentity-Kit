

package com.webank.weid.kit.amop;

import com.webank.weid.kit.amop.callback.RegistCallBack;
import com.webank.weid.kit.amop.callback.WeIdAmopCallback;
import com.webank.weid.kit.amop.base.AmopCommonArgs;
import com.webank.weid.kit.amop.request.GetEncryptKeyArgs;
import com.webank.weid.kit.amop.request.GetPolicyAndPreCredentialArgs;
import com.webank.weid.kit.amop.request.GetWeIdAuthArgs;
import com.webank.weid.kit.amop.request.RequestIssueCredentialArgs;
import com.webank.weid.kit.amop.request.RequestVerifyChallengeArgs;
import com.webank.weid.kit.amop.base.AmopResponse;
import com.webank.weid.kit.protocol.response.GetEncryptKeyResponse;
import com.webank.weid.kit.protocol.response.GetWeIdAuthResponse;
import com.webank.weid.kit.protocol.response.PolicyAndPreCredentialResponse;
import com.webank.weid.kit.protocol.response.RequestIssueCredentialResponse;
import com.webank.weid.kit.protocol.response.RequestVerifyChallengeResponse;
import com.webank.weid.kit.protocol.response.ResponseData;
import com.webank.weid.protocol.base.PolicyAndChallenge;

/**
 * Created by Junqi Zhang on 2019/4/10.
 */
public interface AmopService {

    void registerCallback(Integer directRouteMsgType, WeIdAmopCallback directRouteCallback);

    ResponseData<AmopResponse> request(String toAmopId, AmopCommonArgs args);

    RegistCallBack getPushCallback();

    ResponseData<PolicyAndChallenge> getPolicyAndChallenge(
            String amopId,
            Integer policyId,
            String targetUserWeId
    );

    ResponseData<GetEncryptKeyResponse> getEncryptKey(
            String toAmopId,
            GetEncryptKeyArgs args
    );

    ResponseData<PolicyAndPreCredentialResponse> requestPolicyAndPreCredential(
            String toAmopId,
            GetPolicyAndPreCredentialArgs args
    );

    ResponseData<RequestIssueCredentialResponse> requestIssueCredential(
            String toAmopId,
            RequestIssueCredentialArgs args
    );

    ResponseData<AmopResponse> send(String toAmopId, AmopCommonArgs args);

    /**
     * get weIdAuth object.
     * @param toAmopId target organization id
     * @param args random number
     * @return return the GetWeIdAuthResponse
     */
    ResponseData<GetWeIdAuthResponse> getWeIdAuth(
            String toAmopId,
            GetWeIdAuthArgs args
    );

    /**
     * verify challenge signature.
     * @param toAmopId target organization id
     * @param args verify args
     * @return return the RequestVerifyChallengeResponse
     */
    ResponseData<RequestVerifyChallengeResponse> requestVerifyChallenge(
            String toAmopId,
            RequestVerifyChallengeArgs args
    );

}
