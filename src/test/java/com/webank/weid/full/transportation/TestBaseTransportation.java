

package com.webank.weid.full.transportation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.full.TestBaseService;
import com.webank.weid.full.TestBaseUtil;
import com.webank.weid.protocol.base.*;
import com.webank.weid.protocol.response.ResponseData;

public abstract class TestBaseTransportation extends TestBaseService {

    protected static PresentationE presentationE;
    protected static PresentationPolicyE presentationPolicyE;
    protected static Challenge challenge;
    protected static List<String> verifier = new ArrayList<String>();
    protected static WeIdAuthentication weIdAuthentication;

    @Override
    public synchronized void testInit() {
        super.testInit();
        if (verifier.isEmpty()) {
            verifier.add(createWeIdResult.getWeId());
        }
        if (weIdAuthentication == null) {
            weIdAuthentication = new WeIdAuthentication();
            weIdAuthentication.setWeId(createWeIdResult.getWeId());
            weIdAuthentication.setAuthenticationMethodId(createWeIdResult.getWeId() + "#key-0");
            weIdAuthentication.setWeIdPrivateKey(new WeIdPrivateKey());
            weIdAuthentication.getWeIdPrivateKey().setPrivateKey(
                createWeIdResult.getUserWeIdPrivateKey().getPrivateKey());
        }
    }


    protected PresentationE getPresentationE() {
        ResponseData<CredentialPojo> credentialPojoResponse =
            credentialPojoService.createCredential(createCredentialPojoArgs);
        if (credentialPojoResponse.getErrorCode() != KitErrorCode.SUCCESS.getCode()) {
            return null;
        }
        List<CredentialPojo> credentialList = new ArrayList<>();
        credentialList.add(credentialPojoResponse.getResult());

        PresentationPolicyE presentationPolicyE = PresentationPolicyE.create("policy.json");
        if (presentationPolicyE == null) {
            return null;
        }
        presentationPolicyE.setPolicyPublisherWeId(createWeIdResultWithSetAttr.getWeId());
        Map<Integer, ClaimPolicy> policyMap = presentationPolicyE.getPolicy();
        ClaimPolicy cliamPolicy = policyMap.get(1000);
        policyMap.remove(1000);
        policyMap.put(createCredentialPojoArgs.getCptId(), cliamPolicy);

        this.presentationPolicyE = presentationPolicyE;
        Challenge challenge = Challenge.create(
            createWeIdResultWithSetAttr.getWeId(),
            String.valueOf(System.currentTimeMillis())
        );
        this.challenge = challenge;
        com.webank.weid.protocol.response.ResponseData<PresentationE> response = credentialPojoService.createPresentation(
            credentialList,
            presentationPolicyE,
            challenge,
            TestBaseUtil.buildWeIdAuthentication(createWeIdResultWithSetAttr)
        );

        if (response.getErrorCode() != KitErrorCode.SUCCESS.getCode()) {
            return null;
        }
        return response.getResult();
    }

    protected PresentationE getPresentationE4MlCpt() {
        com.webank.weid.protocol.response.ResponseData<CredentialPojo> credentialPojoResponse =
            credentialPojoService.createCredential(createCredentialPojoArgs1);
        if (credentialPojoResponse.getErrorCode() != KitErrorCode.SUCCESS.getCode()) {
            return null;
        }
        List<CredentialPojo> credentialList = new ArrayList<>();
        credentialList.add(credentialPojoResponse.getResult());

        PresentationPolicyE presentationPolicyE = PresentationPolicyE.create("test-policy.json");
        if (presentationPolicyE == null) {
            return null;
        }
        presentationPolicyE.setPolicyPublisherWeId(createWeIdResultWithSetAttr.getWeId());
        Map<Integer, ClaimPolicy> policyMap = presentationPolicyE.getPolicy();
        ClaimPolicy cliamPolicy = policyMap.get(1001);
        policyMap.remove(1000);
        policyMap.remove(1001);
        policyMap.remove(1002);
        policyMap.put(createCredentialPojoArgs1.getCptId(), cliamPolicy);

        this.presentationPolicyE = presentationPolicyE;
        Challenge challenge = Challenge.create(
            createWeIdResultWithSetAttr.getWeId(),
            String.valueOf(System.currentTimeMillis())
        );
        this.challenge = challenge;
        com.webank.weid.protocol.response.ResponseData<PresentationE> response = credentialPojoService.createPresentation(
            credentialList,
            presentationPolicyE,
            challenge,
            TestBaseUtil.buildWeIdAuthentication(createWeIdResultWithSetAttr)
        );

        if (response.getErrorCode() != KitErrorCode.SUCCESS.getCode()) {
            return null;
        }
        return response.getResult();
    }

    protected PresentationE getPresentationE4MultiCpt() {
        com.webank.weid.protocol.response.ResponseData<CredentialPojo> credentialPojoResponse2 =
            credentialPojoService.createCredential(createCredentialPojoArgs2);

        com.webank.weid.protocol.response.ResponseData<CredentialPojo> credentialPojoResponse3 =
            credentialPojoService.createCredential(createCredentialPojoArgs3);

        if (credentialPojoResponse2.getErrorCode() != KitErrorCode.SUCCESS.getCode()) {
            return null;
        }

        if (credentialPojoResponse3.getErrorCode() != KitErrorCode.SUCCESS.getCode()) {
            return null;
        }

        List<CredentialPojo> credentialList = new ArrayList<>();
        credentialList.add(credentialPojoResponse2.getResult());
        credentialList.add(credentialPojoResponse3.getResult());

        PresentationPolicyE presentationPolicyE = PresentationPolicyE.create("test-policy.json");
        if (presentationPolicyE == null) {
            return null;
        }
        presentationPolicyE.setPolicyPublisherWeId(createWeIdResultWithSetAttr.getWeId());
        Map<Integer, ClaimPolicy> policyMap = presentationPolicyE.getPolicy();
        ClaimPolicy cliamPolicy1000 = policyMap.get(1000);
        ClaimPolicy cliamPolicy1001 = policyMap.get(1001);

        policyMap.remove(1000);
        policyMap.remove(1001);
        policyMap.remove(1002);
        policyMap.put(createCredentialPojoArgs3.getCptId(), cliamPolicy1000);
        policyMap.put(createCredentialPojoArgs2.getCptId(), cliamPolicy1001);

        this.presentationPolicyE = presentationPolicyE;
        Challenge challenge = Challenge.create(
            createWeIdResultWithSetAttr.getWeId(),
            String.valueOf(System.currentTimeMillis())
        );
        this.challenge = challenge;
        com.webank.weid.protocol.response.ResponseData<PresentationE> response = credentialPojoService.createPresentation(
            credentialList,
            presentationPolicyE,
            challenge,
            TestBaseUtil.buildWeIdAuthentication(createWeIdResultWithSetAttr)
        );

        if (response.getErrorCode() != KitErrorCode.SUCCESS.getCode()) {
            return null;
        }
        return response.getResult();
    }

    protected PresentationE getPresentationE4SpecTplCpt() {
        com.webank.weid.protocol.response.ResponseData<CredentialPojo> credentialPojoResponse =
            credentialPojoService.createCredential(createCredentialPojoArgs4);
        if (credentialPojoResponse.getErrorCode() != KitErrorCode.SUCCESS.getCode()) {
            return null;
        }
        List<CredentialPojo> credentialList = new ArrayList<>();
        credentialList.add(credentialPojoResponse.getResult());

        PresentationPolicyE presentationPolicyE = PresentationPolicyE
            .create("test-spectpl-policy.json");
        if (presentationPolicyE == null) {
            return null;
        }
        presentationPolicyE.setPolicyPublisherWeId(createWeIdResultWithSetAttr.getWeId());
        Map<Integer, ClaimPolicy> policyMap = presentationPolicyE.getPolicy();
        ClaimPolicy cliamPolicy1005 = policyMap.get(1005);
        policyMap.remove(1005);
        policyMap.put(createCredentialPojoArgs4.getCptId(), cliamPolicy1005);

        this.presentationPolicyE = presentationPolicyE;
        Challenge challenge = Challenge.create(
            createWeIdResultWithSetAttr.getWeId(),
            String.valueOf(System.currentTimeMillis())
        );
        this.challenge = challenge;
        com.webank.weid.protocol.response.ResponseData<PresentationE> response = credentialPojoService.createPresentation(
            credentialList,
            presentationPolicyE,
            challenge,
            TestBaseUtil.buildWeIdAuthentication(createWeIdResultWithSetAttr)
        );

        if (response.getErrorCode() != KitErrorCode.SUCCESS.getCode()) {
            return null;
        }
        return response.getResult();
    }
    
    protected CredentialPojoList getCredentialPojoList(PresentationE presentation4MultiCpt) {
        CredentialPojoList list = new CredentialPojoList();
        for (CredentialPojo credentialPojo : presentation4MultiCpt.getVerifiableCredential()) {
            list.add(credentialPojo);
        }
        return list;
    }
}
