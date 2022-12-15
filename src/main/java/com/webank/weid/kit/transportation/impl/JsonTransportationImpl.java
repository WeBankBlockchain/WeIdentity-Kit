

package com.webank.weid.kit.transportation.impl;

import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.protocol.inf.JsonSerializer;
import com.webank.weid.kit.protocol.response.ResponseData;
import com.webank.weid.kit.transportation.encode.EncodeProcessorFactory;
import com.webank.weid.kit.transportation.entity.*;
import com.webank.weid.kit.transportation.inf.JsonTransportation;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.protocol.base.WeIdAuthentication;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * JSON协议的传输.
 *
 * @author v_wbgyang
 */
public class JsonTransportationImpl
    extends AbstractJsonTransportation
    implements JsonTransportation {

    private static final Logger logger =
        LoggerFactory.getLogger(JsonTransportationImpl.class);

    private static final JsonVersion version = JsonVersion.V1;

    @Override
    public <T extends JsonSerializer> ResponseData<String> serialize(
        T object,
        ProtocolProperty property
    ) {
        if (property != null && property.getTransMode() == TransMode.DOWNLOAD_MODE) {
            logger.error(
                "[serialize] should to call serialize(WeIdAuthentication weIdAuthentication, "
                + "T object, ProtocolProperty property).");
            return new ResponseData<String>(StringUtils.EMPTY, KitErrorCode.THIS_IS_UNSUPPORTED);
        }
        return serializeInner(object, property);
    }
    
    @Override
    protected <T extends JsonSerializer> ResponseData<String> serializeInner(
        T object,
        ProtocolProperty property
    ) {
        logger.info("[serialize] begin to execute JsonTransportation serialization, property:{}.",
            property);
        logger.info("[serialize] begin to execute JsonTransportation serialization, object:{}.",
            object);
        // 检查协议配置完整性
        KitErrorCode kitErrorCode = checkEncodeProperty(property);
        if (kitErrorCode != KitErrorCode.SUCCESS) {
            logger.error("[serialize] checkEncodeProperty fail, errorCode:{}.", kitErrorCode);
            return new ResponseData<String>(StringUtils.EMPTY, kitErrorCode);
        }
        // 检查presentation完整性
        kitErrorCode = checkProtocolData(object);
        if (kitErrorCode != KitErrorCode.SUCCESS) {
            logger.error("[serialize] checkProtocolData fail, errorCode:{}.", kitErrorCode);
            return new ResponseData<String>(StringUtils.EMPTY, kitErrorCode);
        }

        try {
            // 构建JSON协议数据
            JsonBaseData jsonBaseData = buildJsonData(property);
            logger.info("[serialize] encode by {}.", property.getEncodeType().name());
            // 如果是原文方式，则直接放对象,data为对象类型
            if (property.getEncodeType() == EncodeType.ORIGINAL) {
                jsonBaseData.setData(object.toJson());
            } else {
                // 非原文格式，根据data进行编解码，data为字符串类型
                // 创建编解码实体对象，对此实体中的data编码操作
                EncodeData encodeData =
                    new EncodeData(
                        jsonBaseData.getId(),
                        jsonBaseData.getAmopId(),
                        object.toJson(),
                        super.getVerifiers()
                    );

                String data =
                    EncodeProcessorFactory
                        .getEncodeProcessor(property.getEncodeType())
                        .encode(encodeData);
                jsonBaseData.setData(data);
            }
            // 将jsonBaseData转换成JSON字符串
            String jsonData = DataToolUtils.objToJsonStrWithNoPretty(jsonBaseData);
            logger.info("[serialize] JsonTransportation serialization finished.");
            return new ResponseData<String>(jsonData, KitErrorCode.SUCCESS);
        } catch (WeIdBaseException e) {
            logger.error("[serialize] JsonTransportation serialization due to base error.", e);
            return new ResponseData<String>(StringUtils.EMPTY, KitErrorCode.getTypeByErrorCode(e.getErrorCode().getCode()));
        } catch (Exception e) {
            logger.error("[serialize] JsonTransportation serialization due to unknown error.", e);
            return new ResponseData<String>(StringUtils.EMPTY, KitErrorCode.TRANSPORTATION_BASE_ERROR);
        }
    }
    
    @Override
    public <T extends JsonSerializer> ResponseData<T> deserialize(
        String transString,
        Class<T> clazz
    ) {
        return deserializeInner(null, transString, clazz);
    }

    @Override
    public <T extends JsonSerializer> ResponseData<T> deserialize(
        WeIdAuthentication weIdAuthentication,
        String transString, 
        Class<T> clazz
    ) {
        // 检查WeIdAuthentication合法性
        KitErrorCode kitErrorCode = checkWeIdAuthentication(weIdAuthentication);
        if (kitErrorCode != KitErrorCode.SUCCESS) {
            logger.error(
                "[deserialize] checkWeIdAuthentication fail, errorCode:{}.",
                    kitErrorCode
            );
            return new ResponseData<T>(null, kitErrorCode);
        }
        return deserializeInner(weIdAuthentication, transString, clazz);
    }
    
    private <T extends JsonSerializer> ResponseData<T> deserializeInner(
        WeIdAuthentication weIdAuthentication,
        String transString, 
        Class<T> clazz
    ) {
        try {
            logger.info("[deserialize] begin to execute JsonTransportation deserialize.");
            logger.info("[deserialize] the transString:{}.", transString);
            if (StringUtils.isBlank(transString)) {
                logger.error("[deserialize] the transString is blank.");
                return new ResponseData<T>(null, KitErrorCode.TRANSPORTATION_PROTOCOL_DATA_INVALID);
            }
           
            //将JSON字符串解析成JsonBaseData对象
            JsonBaseData jsonBaseData = DataToolUtils.deserialize(
                transString, 
                JsonBaseData.class);
            //检查JsonBaseData合法性
            KitErrorCode kitErrorCode = checkJsonBaseData(jsonBaseData);
            if (kitErrorCode != KitErrorCode.SUCCESS) {
                logger.error("[deserialize] checkJsonBaseData fail, errorCode:{}.", kitErrorCode);
                return new ResponseData<T>(null, kitErrorCode);
            }

            Object data = jsonBaseData.getData();
            // 如果解析出来的data为map类型，则说明 data存放的为对象，而非字符串
            if (data instanceof Map) {
                jsonBaseData
                    .setData(DataToolUtils.objToJsonStrWithNoPretty(jsonBaseData.getData()));
            }
            //创建编解码实体对象，对此实体中的data解码操作
            EncodeData encodeData =
                new EncodeData(
                    jsonBaseData.getId(),
                    jsonBaseData.getAmopId(),
                    jsonBaseData.getData().toString(),
                    weIdAuthentication
                );
            //根据编解码类型获取编解码枚举对象
            EncodeType encodeType = EncodeType.getEncodeType(jsonBaseData.getEncodeType());
            if (encodeType == null) {
                return new ResponseData<T>(null, KitErrorCode.TRANSPORTATION_PROTOCOL_ENCODE_ERROR);
            }
            logger.info("[deserialize] decode by {}.", encodeType.name());
            //进行解码操作
            String jsonString =
                EncodeProcessorFactory
                    .getEncodeProcessor(encodeType)
                    .decode(encodeData);
            return super.buildObject(jsonString, clazz);
        } catch (WeIdBaseException e) {
            logger.error("[deserialize] JsonTransportation deserialize due to base error.", e);
            return new ResponseData<T>(null, KitErrorCode.getTypeByErrorCode(e.getErrorCode().getCode()));
        } catch (Exception e) {
            logger.error("[deserialize] JsonTransportation deserialize due to unknown error.", e);
            return new ResponseData<T>(null, KitErrorCode.TRANSPORTATION_BASE_ERROR);
        }
    }
    
    /**
     * 构建协议实体数据.
     *
     * @param property 协议配置对象
     * @return 返回协议实体对象
     */
    private JsonBaseData buildJsonData(ProtocolProperty property) {
        JsonBaseData jsonBaseData = new JsonBaseData();
        jsonBaseData.setEncodeType(property.getEncodeType().getCode());
        jsonBaseData.setId(DataToolUtils.getUuId32());
        jsonBaseData.setAmopId(com.webank.weid.blockchain.service.fisco.BaseServiceFisco.fiscoConfig.getAmopId());
        jsonBaseData.setVersion(version.getCode());
        return jsonBaseData;
    }

    /**
     * 检查jsonBaseData合法性.
     *
     * @param jsonBaseData JSON协议实体数据
     * @return 返回错误码
     */
    private KitErrorCode checkJsonBaseData(JsonBaseData jsonBaseData) {
        if (jsonBaseData == null
            || StringUtils.isBlank(jsonBaseData.getId())
            || StringUtils.isBlank(jsonBaseData.getAmopId())
            || jsonBaseData.getData() == null
            || StringUtils.isBlank(jsonBaseData.getData().toString())) {
            return KitErrorCode.TRANSPORTATION_PROTOCOL_DATA_INVALID;
        }
        if (JsonVersion.getVersion(jsonBaseData.getVersion()) == null) {
            return KitErrorCode.TRANSPORTATION_PROTOCOL_VERSION_ERROR;
        }
        if (EncodeType.getEncodeType(jsonBaseData.getEncodeType()) == null) {
            return KitErrorCode.TRANSPORTATION_PROTOCOL_ENCODE_ERROR;
        }
        return KitErrorCode.SUCCESS;
    }
}
