

package com.webank.weid.kit.amop.callback;

import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.amop.request.GetPolicyAndChallengeArgs;
import com.webank.weid.kit.protocol.response.GetPolicyAndChallengeResponse;
import com.webank.weid.protocol.base.PolicyAndChallenge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于处理机构根据policyId获取policy的回调.
 * 
 * @author v_wbgyang
 *
 */
public class PresentationCallbackWeId extends WeIdAmopCallback {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(PresentationCallbackWeId.class);

    private PresentationPolicyService policyService;
    
    @Override
    public GetPolicyAndChallengeResponse onPush(GetPolicyAndChallengeArgs arg) {
        logger.info("PresentationCallback param:{}", arg);
        GetPolicyAndChallengeResponse response = new GetPolicyAndChallengeResponse();
        if (policyService == null) {
            logger.error("PresentationCallback policyService is null");
            response.setErrorCode(KitErrorCode.POLICY_SERVICE_NOT_EXISTS.getCode());
            response.setErrorMessage(KitErrorCode.POLICY_SERVICE_NOT_EXISTS.getCodeDesc());
            return response;
        }
        PolicyAndChallenge policyAndChallenge;
        try {
            policyAndChallenge = 
                policyService.policyAndChallengeOnPush(arg.getPolicyId(), arg.getTargetUserWeId());
        } catch (Exception e) {
            logger.error("the policy service call fail, please check the error log.", e);
            response.setErrorCode(KitErrorCode.POLICY_SERVICE_CALL_FAIL.getCode());
            response.setErrorMessage(KitErrorCode.POLICY_SERVICE_CALL_FAIL.getCodeDesc());
            return response;
        }
        response.setErrorCode(KitErrorCode.SUCCESS.getCode());
        response.setErrorMessage(KitErrorCode.SUCCESS.getCodeDesc());
        response.setPolicyAndChallenge(policyAndChallenge);
        return response;
    }

    public void registPolicyService(PresentationPolicyService service) {
        policyService = service;
    }
}
