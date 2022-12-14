

package com.webank.weid.full.transportation;

import com.webank.weid.common.LogUtil;
import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.crypto.CryptoServiceFactory;
import com.webank.weid.kit.crypto.inf.CryptoService;
import com.webank.weid.kit.crypto.params.CryptoType;
import com.webank.weid.kit.protocol.response.ResponseData;
import com.webank.weid.kit.transportation.TransportationFactory;
import com.webank.weid.kit.transportation.entity.EncodeType;
import com.webank.weid.kit.transportation.entity.ProtocolProperty;
import com.webank.weid.protocol.base.CredentialPojo;
import com.webank.weid.protocol.base.PresentationE;
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * 二维码协议序列化测试.
 *
 * @author v_wbgyang
 */
public class TestQrCodeSerialize extends TestBaseTransportation {

    private static final Logger logger = LoggerFactory.getLogger(TestQrCodeSerialize.class);

    private PresentationE presentation;

    @Override
    public synchronized void testInit() {
        super.testInit();
        presentation = getPresentationE();
    }

    /**
     * 使用原文方式构建协议数据.
     */
    @Test
    public void testSerialize_EncodeTypeOriginal() {
        ResponseData<String> response =
            TransportationFactory.newQrCodeTransportation().specify(verifier)
                .serialize(presentation, new ProtocolProperty(EncodeType.ORIGINAL));
        LogUtil.info(logger, "serialize", response);
        Assert.assertEquals(KitErrorCode.SUCCESS.getCode(), response.getErrorCode().intValue());
        Assert.assertNotNull(response.getResult());
    }

    /**
     * 使用密文方式构建协议数据.
     */
    @Test
    public void testSerialize_EncodeTypeCipher() {
        ResponseData<String> response =
            TransportationFactory.newQrCodeTransportation().specify(verifier)
                .serialize(presentation, new ProtocolProperty(EncodeType.CIPHER));
        LogUtil.info(logger, "serialize", response);
        Assert.assertEquals(KitErrorCode.SUCCESS.getCode(), response.getErrorCode().intValue());
        Assert.assertNotNull(response.getResult());
    }

    /**
     * 传入的协议配置为null.
     */
    @Test
    public void testSerialize_protocolNull() {
        ResponseData<String> response =
            TransportationFactory.newQrCodeTransportation()
                .serialize(presentation, null);
        LogUtil.info(logger, "serialize", response);
        Assert.assertEquals(
            KitErrorCode.TRANSPORTATION_PROTOCOL_PROPERTY_ERROR.getCode(),
            response.getErrorCode().intValue()
        );
        Assert.assertEquals(StringUtils.EMPTY, response.getResult());
    }

    /**
     * 传入协议配置的编解码为null.
     */
    @Test
    public void testSerialize_encodeNull() {
        ResponseData<String> response =
            TransportationFactory.newQrCodeTransportation()
                .serialize(presentation, new ProtocolProperty(null));
        LogUtil.info(logger, "serialize", response);
        Assert.assertEquals(
            KitErrorCode.TRANSPORTATION_PROTOCOL_ENCODE_ERROR.getCode(),
            response.getErrorCode().intValue()
        );
        Assert.assertEquals(StringUtils.EMPTY, response.getResult());
    }

    /**
     * 传入协议配置的编解码错误.
     */
    @Test
    public void testSerialize_encodeError() {
        ResponseData<String> response =
            TransportationFactory.newQrCodeTransportation()
                .serialize(presentation, new ProtocolProperty(null));
        LogUtil.info(logger, "serialize", response);
        Assert.assertEquals(
            KitErrorCode.TRANSPORTATION_PROTOCOL_ENCODE_ERROR.getCode(),
            response.getErrorCode().intValue()
        );
        Assert.assertEquals(StringUtils.EMPTY, response.getResult());
    }

    /**
     * 传入实体数据为null.
     */
    @Test
    public void testSerialize_transDataNull() {
        PresentationE presentation = null;
        ResponseData<String> response =
            TransportationFactory.newQrCodeTransportation()
                .serialize(presentation, new ProtocolProperty(EncodeType.ORIGINAL));
        LogUtil.info(logger, "serialize", response);
        Assert.assertEquals(
            KitErrorCode.TRANSPORTATION_PROTOCOL_DATA_INVALID.getCode(),
            response.getErrorCode().intValue()
        );
        Assert.assertEquals(StringUtils.EMPTY, response.getResult());
    }

    /**
     * 传入实体数据为credentialPojo.
     */
    @Test
    public void testSerialize_serializeCredentialPojo() {

        ResponseData<String> response =
            TransportationFactory.newQrCodeTransportation().specify(verifier)
                .serialize(credentialPojo, new ProtocolProperty(EncodeType.ORIGINAL));
        LogUtil.info(logger, "serialize", response);
        Assert.assertEquals(
            KitErrorCode.SUCCESS.getCode(),
            response.getErrorCode().intValue()
        );
        Assert.assertNotNull(response.getResult());
    }

    /**
     * 传入实体数据为credentialPojo.
     */
    @Test
    public void testSerialize_serializeEmptyCredentialPojo() {

        CredentialPojo credentialPojo1 = copyCredentialPojo(credentialPojo);
        ResponseData<String> response =
            TransportationFactory.newQrCodeTransportation().specify(verifier)
                .serialize(credentialPojo1, new ProtocolProperty(EncodeType.ORIGINAL));
        LogUtil.info(logger, "serialize", response);
        Assert.assertEquals(
            KitErrorCode.SUCCESS.getCode(),
            response.getErrorCode().intValue()
        );
        Assert.assertNotNull(response.getResult());
    }

    /**
     * 传入实体数据的Credential中的凭证ID存在分隔符.
     */
    @Test
    public void testSerializeCase6() {
        PresentationE presentation = this.getPresentationE();
        presentation.getContext().add("value|v");
        ResponseData<String> response =
            TransportationFactory.newQrCodeTransportation().specify(verifier)
                .serialize(presentation, new ProtocolProperty(EncodeType.ORIGINAL));
        LogUtil.info(logger, "serialize", response);
        Assert.assertEquals(
            KitErrorCode.TRANSPORTATION_PROTOCOL_FIELD_INVALID.getCode(),
            response.getErrorCode().intValue()
        );
        Assert.assertEquals(StringUtils.EMPTY, response.getResult());
    }

    /**
     * mock异常情况.
     */
    @Test
    public void testSerializeCase7() {

        new MockUp<CryptoServiceFactory>() {
            @Mock
            public CryptoService getCryptoService(CryptoType cryptType) {
                return new HashMap<String, CryptoService>().get("key");
            }
        };

        ResponseData<String> response =
            TransportationFactory.newQrCodeTransportation().specify(verifier)
                .serialize(presentation, new ProtocolProperty(EncodeType.CIPHER));

        LogUtil.info(logger, "serialize", response);
        Assert.assertEquals(
            KitErrorCode.TRANSPORTATION_ENCODE_BASE_ERROR.getCode(),
            response.getErrorCode().intValue()
        );
        Assert.assertEquals(StringUtils.EMPTY, response.getResult());
    }
}
