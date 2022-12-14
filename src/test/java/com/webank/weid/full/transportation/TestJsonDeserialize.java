

package com.webank.weid.full.transportation;

import com.webank.weid.common.LogUtil;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.kit.crypto.CryptoServiceFactory;
import com.webank.weid.kit.crypto.inf.CryptoService;
import com.webank.weid.kit.crypto.params.CryptoType;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.kit.protocol.response.ResponseData;
import com.webank.weid.kit.transportation.TransportationFactory;
import com.webank.weid.kit.transportation.entity.EncodeType;
import com.webank.weid.kit.transportation.entity.ProtocolProperty;
import com.webank.weid.protocol.base.CredentialPojo;
import com.webank.weid.protocol.base.PresentationE;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

/**
 * 二维码协议反序列化测试.
 *
 * @author v_wbgyang
 */
public class TestJsonDeserialize extends TestBaseTransportation {

    private static final Logger logger = LoggerFactory.getLogger(TestJsonDeserialize.class);

    private static PresentationE presentation;

    private static String original_transString;

    @Override
    public synchronized void testInit() {
        if (presentation == null) {
            super.testInit();
            presentation = this.getPresentationE();
            original_transString =
                TransportationFactory.newJsonTransportation().serialize(
                    presentation,
                    new ProtocolProperty(EncodeType.ORIGINAL)
                ).getResult();
        }
    }

    /**
     * 使用原文方式构建协议数据并解析.
     */
    @Test
    public void testDeserialize_EncodeTypeOriginal() {
        ResponseData<String> response =
            TransportationFactory.newJsonTransportation().serialize(
                presentation,
                new ProtocolProperty(EncodeType.ORIGINAL)
            );
        ResponseData<PresentationE> wrapperRes = TransportationFactory.newJsonTransportation()
            .deserialize(response.getResult(), PresentationE.class);
        LogUtil.info(logger, "deserialize", wrapperRes);
        Assert.assertEquals(ErrorCode.SUCCESS.getCode(), wrapperRes.getErrorCode().intValue());
        Assert.assertEquals(presentation.toJson(), wrapperRes.getResult().toJson());
    }

    /**
     * 使用密文方式构建协议数据并解析.
     */
    @Test
    public void testDeserialize_EncodeTypeCipher() {
        ResponseData<String> response =
            TransportationFactory.newJsonTransportation().specify(verifier).serialize(
                presentation,
                new ProtocolProperty(EncodeType.CIPHER)
            );
        ResponseData<PresentationE> wrapperRes = TransportationFactory.newJsonTransportation()
            .deserialize(weIdAuthentication, response.getResult(), PresentationE.class);
        LogUtil.info(logger, "deserialize", wrapperRes);
        Assert.assertEquals(ErrorCode.SUCCESS.getCode(), wrapperRes.getErrorCode().intValue());
        Assert.assertEquals(presentation.toJson(), wrapperRes.getResult().toJson());
    }

    /**
     * 使用密文方式构建协议数据并解析.
     */
    @Test
    public void testDeserialize_credentialPojo() {
        List<CredentialPojo> credentialPojoList = presentation.getVerifiableCredential();
        CredentialPojo credentialPojo = new CredentialPojo();
        if (credentialPojoList.size() > 0) {
            credentialPojo = credentialPojoList.get(0);
        }

        ResponseData<String> response =
            TransportationFactory.newJsonTransportation().specify(verifier).serialize(
                credentialPojo,
                new ProtocolProperty(EncodeType.CIPHER)
            );
        ResponseData<CredentialPojo> wrapperRes = TransportationFactory.newJsonTransportation()
            .deserialize(response.getResult(), CredentialPojo.class);
        LogUtil.info(logger, "deserialize", wrapperRes);
        Assert.assertEquals(ErrorCode.ENCRYPT_KEY_NO_PERMISSION.getCode(),
            wrapperRes.getErrorCode().intValue());  
    }

    /**
     * 解析的数据和解析类型不匹配.
     */
    @Test
    public void testDeserialize_transNotMath() {
        List<CredentialPojo> credentialPojoList = presentation.getVerifiableCredential();
        CredentialPojo credentialPojo = new CredentialPojo();
        if (credentialPojoList.size() > 0) {
            credentialPojo = credentialPojoList.get(0);
        }

        ResponseData<String> response =
            TransportationFactory.newJsonTransportation().specify(verifier).serialize(
                credentialPojo,
                new ProtocolProperty(EncodeType.CIPHER)
            );
        ResponseData<PresentationE> wrapperRes = TransportationFactory.newJsonTransportation()
            .deserialize(response.getResult(), PresentationE.class);
        LogUtil.info(logger, "deserialize", wrapperRes);
        Assert.assertEquals(ErrorCode.ENCRYPT_KEY_NO_PERMISSION.getCode(),
                wrapperRes.getErrorCode().intValue());        
    }

    /**
     * 协议字符串输入为空.
     */
    @Test
    public void testDeserialize_dataNull() {
        String transString = null;
        ResponseData<PresentationE> wrapperRes = TransportationFactory.newJsonTransportation()
            .deserialize(transString, PresentationE.class);
        LogUtil.info(logger, "deserialize", wrapperRes);
        Assert.assertEquals(
            ErrorCode.TRANSPORTATION_PROTOCOL_DATA_INVALID.getCode(),
            wrapperRes.getErrorCode().intValue()
        );
        Assert.assertNull(wrapperRes.getResult());
    }

    /**
     * 协议字符串输入非法.
     */
    @Test
    public void testDeserialize_transStrig() {
        String transString = "abcd";
        ResponseData<PresentationE> wrapperRes = TransportationFactory.newJsonTransportation()
            .deserialize(transString, PresentationE.class);
        LogUtil.info(logger, "deserialize", wrapperRes);
        Assert.assertEquals(
            ErrorCode.DATA_TYPE_CASE_ERROR.getCode(),
            wrapperRes.getErrorCode().intValue()
        );
        Assert.assertNull(wrapperRes.getResult());
    }

    /**
     * mock异常情况.
     */
    @Test
    public void testDeserializeCase5() {
        ResponseData<String> response =
            TransportationFactory.newJsonTransportation().specify(verifier).serialize(
                presentation,
                new ProtocolProperty(EncodeType.CIPHER)
            );

        new MockUp<CryptoServiceFactory>() {
            @Mock
            public CryptoService getCryptoService(CryptoType cryptType) {
                return new HashMap<String, CryptoService>().get("key");
            }
        };

        ResponseData<PresentationE> wrapperRes =
            TransportationFactory.newJsonTransportation()
                .deserialize(weIdAuthentication, response.getResult(), PresentationE.class);

        LogUtil.info(logger, "deserialize", wrapperRes);
        Assert.assertEquals(
            ErrorCode.TRANSPORTATION_ENCODE_BASE_ERROR.getCode(),
            wrapperRes.getErrorCode().intValue()
        );
        Assert.assertNull(wrapperRes.getResult());
    }

    /**
     * mock异常情况.
     */
    @Test
    public void testDeserializeCase6() {
        new MockUp<EncodeType>() {
            @Mock
            public EncodeType getEncodeType(int code) {
                throw new WeIdBaseException(ErrorCode.TRANSPORTATION_PROTOCOL_ENCODE_ERROR);
            }
        };
        ResponseData<PresentationE> wrapperRes = TransportationFactory.newJsonTransportation()
            .deserialize(original_transString, PresentationE.class);

        LogUtil.info(logger, "deserialize", wrapperRes);
        Assert.assertEquals(
            ErrorCode.TRANSPORTATION_PROTOCOL_ENCODE_ERROR.getCode(),
            wrapperRes.getErrorCode().intValue()
        );
        Assert.assertNull(wrapperRes.getResult());
    }

    /**
     * mock异常情况.
     */
    @Test
    public void testDeserializeCase7() {
        new MockUp<EncodeType>() {
            @Mock
            public EncodeType getEncodeType(int code) {
                throw new WeIdBaseException(ErrorCode.TRANSPORTATION_PROTOCOL_ENCODE_ERROR);
            }
        };

        ResponseData<PresentationE> response =
            TransportationFactory.newJsonTransportation().deserialize(
                original_transString,
                PresentationE.class
            );

        LogUtil.info(logger, "deserialize", response);
        Assert.assertEquals(
            ErrorCode.TRANSPORTATION_PROTOCOL_ENCODE_ERROR.getCode(),
            response.getErrorCode().intValue()
        );
        Assert.assertEquals(null, response.getResult());
    }
}
