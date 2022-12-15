package com.webank.weid.kit.amop.callback;

import com.webank.weid.kit.amop.entity.AmopMsgType;
import com.webank.weid.kit.amop.AmopService;
import com.webank.weid.kit.amop.AmopServiceImpl;
import com.webank.weid.protocol.base.PolicyAndChallenge;

public abstract class PresentationPolicyService {
    
    protected AmopService amopService = new AmopServiceImpl();
    
    private static PresentationCallbackWeId presentationCallback = new PresentationCallbackWeId();
    
    /**
     * 无参构造器,自动注册callback.
     */
    public PresentationPolicyService() {
        presentationCallback.registPolicyService(this);
        amopService.registerCallback(
            AmopMsgType.GET_POLICY_AND_CHALLENGE.getValue(), 
            presentationCallback
        );
    }

    /**
     * 获取PolicyAndChallenge.
     * @param policyId 策略编号
     * @param targetUserWeId user WeId
     * @return 返回PresentationPolicyE对象数据
     */
    public abstract PolicyAndChallenge policyAndChallengeOnPush(
        String policyId, 
        String targetUserWeId
    );
}
