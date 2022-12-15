


package com.webank.weid.kit.amop.request;

import lombok.Getter;
import lombok.Setter;

import com.webank.weid.kit.amop.base.AmopBaseMsgArgs;

/**
 * the request body for get PolicyAndChallenge.
 * 
 * @author tonychen 2019年5月7日
 *
 */

@Getter
@Setter
public class GetPolicyAndChallengeArgs extends AmopBaseMsgArgs {

    private String policyId;

    private String targetUserWeId;
}
