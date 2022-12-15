

package com.webank.weid.kit.amop.request;

import java.util.List;

import com.webank.weid.protocol.base.CredentialPojo;
import com.webank.weid.protocol.base.PolicyAndPreCredential;
import com.webank.weid.protocol.base.WeIdAuthentication;
import lombok.Getter;
import lombok.Setter;

import com.webank.weid.kit.amop.base.AmopBaseMsgArgs;

/**
 * args for RequestIssueCredential.
 *
 * @author tonychen 2019年12月4日
 */
@Getter
@Setter
public class RequestIssueCredentialArgs extends AmopBaseMsgArgs {

    /**
     * policyAndPreCredential from issuer.
     */
    private PolicyAndPreCredential policyAndPreCredential;

    /**
     * user's credential list.
     */
    private List<CredentialPojo> credentialList;

    /**
     * user's claim.
     */
    private String claim;

    /**
     * user's authentication.
     */
    private WeIdAuthentication auth;

}
