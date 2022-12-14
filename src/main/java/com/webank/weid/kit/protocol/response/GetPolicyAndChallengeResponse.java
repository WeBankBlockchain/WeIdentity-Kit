package com.webank.weid.kit.protocol.response;

import com.webank.weid.protocol.base.PolicyAndChallenge;
import lombok.Getter;
import lombok.Setter;

/**
 * the getPolicyAndChallenge response.
 * @author tonychen 2019年5月7日
 *
 */
@Getter
@Setter
public class GetPolicyAndChallengeResponse {


    /**
     * 返回的消息.
     */
    private PolicyAndChallenge policyAndChallenge;

    /**
     * 错误码.
     */
    private Integer errorCode;

    /**
     * 错误信息.
     */
    protected String errorMessage;
}
