

package com.webank.weid.kit.transportation.impl;

import com.webank.weid.blockchain.constant.ErrorCode;
import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.exception.ProtocolSuiteException;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.protocol.inf.JsonSerializer;
import com.webank.weid.kit.protocol.response.ResponseData;
import com.webank.weid.kit.transmission.TransmissionFactory;
import com.webank.weid.kit.transmission.entity.TransType;
import com.webank.weid.kit.transportation.encode.EncodeProcessorFactory;
import com.webank.weid.kit.transportation.entity.*;
import com.webank.weid.kit.transportation.inf.QrCodeTransportation;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.protocol.base.WeIdAuthentication;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 二维码传输协议业务处理类.
 * @author v_wbgyang
 *
 */
public class QrCodeTransportationImpl 
    extends AbstractCodeTransportation
    implements QrCodeTransportation {

    private static final Logger logger = 
        LoggerFactory.getLogger(QrCodeTransportationImpl.class);
    
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
    
    protected <T extends JsonSerializer> ResponseData<String> serializeInner(
        T object, 
        ProtocolProperty property
    ) {
        logger.info(
            "[serialize] begin to execute QrCodeTransportation serialization, property:{}.",
            property
        );
        logger.info(
            "[serialize] begin to execute QrCodeTransportation serialization, object:{}.", object);

        // 验证协议配置
        KitErrorCode kitErrorCode = checkEncodeProperty(property);
        if (kitErrorCode != KitErrorCode.SUCCESS) {
            logger.error("[serialize] checkEncodeProperty fail, errorCode:{}.", kitErrorCode);
            return new ResponseData<String>(StringUtils.EMPTY, kitErrorCode);
        }
        // 验证presentation数据
        kitErrorCode = checkProtocolData(object);
        if (kitErrorCode != KitErrorCode.SUCCESS) {
            logger.error("[serialize] checkProtocolData fail, errorCode:{}.", kitErrorCode);
            return new ResponseData<String>(StringUtils.EMPTY, kitErrorCode);
        }
        try {
            // 根据协议版本生成协议实体对象
            QrCodeVersion version = QrCodeVersion.V1;
            if (property.getTransMode() == TransMode.DOWNLOAD_MODE) {
                // 下载模式
                version = QrCodeVersion.V2;
            }
            TransCodeBaseData codeData = TransCodeBaseData.newInstance(version.getClz());
            // 构建协议基础数据
            String uuId = DataToolUtils.getUuId32();
            codeData.buildCodeData(property, com.webank.weid.blockchain.service.fisco.BaseServiceFisco.fiscoConfig.getAmopId(), uuId);
            
            // 创建编解码实体对象，对此实体中的data编码操作
            EncodeData encodeData = 
                new EncodeData(
                    codeData.getId(),
                    codeData.getAmopId(),
                    object.toJson(),
                    super.getVerifiers()
                );
            logger.info("[serialize] encode by {}.", property.getEncodeType().name());
            // 进行编码处理
            String data = 
                EncodeProcessorFactory
                    .getEncodeProcessor(property.getEncodeType())
                    .encode(encodeData);
            codeData.setData(data);
            if (!codeData.check()) {
                throw new ProtocolSuiteException(ErrorCode.TRANSPORTATION_PROTOCOL_FIELD_INVALID);
            }
            
            logger.info("[serialize] the transMode is {}", property.getTransMode());
            if (version == QrCodeVersion.V2) {
                // 下载模式
                //save CodeData
                saveTransData(codeData.getId(), codeData);
            }
            logger.info("[serialize] QrCodeTransportation serialization finished.");
            return new ResponseData<String>(codeData.buildCodeString(), KitErrorCode.SUCCESS);
        } catch (WeIdBaseException e) {
            logger.error("[serialize] QrCodeTransportation serialization due to base error.", e);
            return new ResponseData<String>(StringUtils.EMPTY, KitErrorCode.getTypeByErrorCode(e.getErrorCode().getCode()));
        } catch (Exception e) {
            logger.error("[serialize] QrCodeTransportation serialization due to unknown error.", e);
            return new ResponseData<String>(StringUtils.EMPTY, KitErrorCode.UNKNOW_ERROR);
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
        TransMode transMode = super.getTransMode(transString);
        if (transMode == TransMode.DATA_MODE) {
            return deserializeInner(weIdAuthentication, transString, clazz);
        }
        return deserializeInnerForDown(weIdAuthentication, transString, clazz);
    }
    
    private <T extends JsonSerializer> ResponseData<T> deserializeInner(
        WeIdAuthentication weIdAuthentication,
        String transString, 
        Class<T> clazz
    ) {
        try {
            logger.info("[deserialize] begin to execute QrCodeTransportation deserialize.");
            logger.info("[deserialize] the transString:{}.", transString);
            //解析协议版本
            int versionCode = TransCodeBaseData.getVersion(transString);
            QrCodeVersion version = QrCodeVersion.getVersion(versionCode);
            //根据协议版本生成协议实体对象
            TransCodeBaseData codeData = TransCodeBaseData.newInstance(version.getClz());
            //将协议字符串构建成协议对象
            codeData.buildCodeData(transString);
            EncodeData encodeData = 
                new EncodeData(
                    codeData.getId(),
                    codeData.getAmopId(),
                    String.valueOf(codeData.getData()),
                    weIdAuthentication
                );
            EncodeType enCodeType = EncodeType.getEncodeType(codeData.getEncodeType());
            logger.info("[deserialize] encode by {}.", enCodeType.name());
            //进行解码处理
            String jsonString = 
                EncodeProcessorFactory
                    .getEncodeProcessor(enCodeType)
                    .decode(encodeData);
            logger.info("[deserialize] QrCodeTransportation deserialize finished.");
            return super.buildObject(jsonString, clazz);
        } catch (WeIdBaseException e) {
            logger.error("[deserialize] QrCodeTransportation deserialize due to base error.", e);
            return new ResponseData<T>(null, KitErrorCode.getTypeByErrorCode(e.getErrorCode().getCode()));
        } catch (Exception e) {
            logger.error("[deserialize] QrCodeTransportation deserialize due to unknown error.", e);
            return new ResponseData<T>(null, KitErrorCode.UNKNOW_ERROR);
        }
    }
    
    private <T extends JsonSerializer> ResponseData<T> deserializeInnerForDown(
        WeIdAuthentication weIdAuthentication,
        String transString, 
        Class<T> clazz
    ) {
        try {
            logger.info("[deserialize] begin to execute JsonTransportation deserialize.");
            logger.info("[deserialize] the transString:{}.", transString);
            // 解析协议版本
            int versionCode = TransCodeBaseData.getVersion(transString);
            QrCodeVersion version = QrCodeVersion.getVersion(versionCode);
            //根据协议版本生成协议实体对象
            TransCodeBaseData codeData = TransCodeBaseData.newInstance(version.getClz());
            //将协议字符串构建成协议对象
            codeData.buildCodeData(transString);
            // 获取协议请求下载通过类型
            TransType type = TransType.getTransmissionByCode(codeData.getTransTypeCode());
            // 获取原始数据，两种协议获取方式，目前只实现一种amop，支持https扩展
            ResponseData<String> result = TransmissionFactory.getTransmisson(type)
                .send(buildRequest(type, codeData, weIdAuthentication));
            if (result.getErrorCode() != KitErrorCode.SUCCESS.getCode()) {
                logger.error(
                    "[deserialize] channel request fail:{}-{}.",
                    result.getErrorCode(),
                    result.getErrorMessage()
                );
                return new ResponseData<T>(null, 
                    KitErrorCode.getTypeByErrorCode(result.getErrorCode().intValue()));
            }
            codeData = (TransCodeBaseData) DataToolUtils.deserialize(
                result.getResult(), 
                version.getClz()
            );
            return super.buildObject(String.valueOf(codeData.getData()), clazz);
        } catch (WeIdBaseException e) {
            logger.error("[deserialize] QrCodeTransportation deserialize due to base error.", e);
            return new ResponseData<T>(null, KitErrorCode.getTypeByErrorCode(e.getErrorCode().getCode()));
        } catch (Exception e) {
            logger.error(
                "[deserialize] QrCodeTransportation deserialize due to unknown error.", e);
            return new ResponseData<T>(null, KitErrorCode.TRANSPORTATION_BASE_ERROR);
        }
    }
}
