

package com.webank.weid.kit.transportation.encode;

import com.webank.weid.constant.DataDriverConstant;
import com.webank.weid.blockchain.constant.ErrorCode;
import com.webank.weid.constant.ParamKeyConstant;
import com.webank.weid.exception.DataTypeCastException;
import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.exception.EncodeSuiteException;
import com.webank.weid.kit.amop.request.GetEncryptKeyArgs;
import com.webank.weid.kit.protocol.response.GetEncryptKeyResponse;
import com.webank.weid.kit.protocol.response.ResponseData;
import com.webank.weid.kit.amop.AmopServiceImpl;
import com.webank.weid.kit.amop.AmopService;
import com.webank.weid.kit.crypto.CryptoServiceFactory;
import com.webank.weid.kit.crypto.params.CryptoType;
import com.webank.weid.kit.crypto.params.KeyGenerator;
import com.webank.weid.kit.transportation.entity.EncodeData;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.util.PropertyUtils;
import com.webank.weid.suite.persistence.Persistence;
import com.webank.weid.suite.persistence.PersistenceFactory;
import com.webank.weid.suite.persistence.PersistenceType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 密文编解码处理器.
 *
 * @author v_wbgyang
 */
public class CipherEncodeProcessor implements EncodeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CipherEncodeProcessor.class);

    private Persistence dataDriver;

    private PersistenceType persistenceType;

    protected AmopService amopService = new AmopServiceImpl();

    private Persistence getDataDriver() {
        String type = PropertyUtils.getProperty("persistence_type");
        if (type.equals("mysql")) {
            persistenceType = PersistenceType.Mysql;
        } else if (type.equals("redis")) {
            persistenceType = PersistenceType.Redis;
        }
        if (dataDriver == null) {
            dataDriver = PersistenceFactory.build(persistenceType);
        }
        return dataDriver;
    }

    /**
     * 密文编码处理：先进行压缩，然后进行AES加密.
     */
    @Override
    public String encode(EncodeData encodeData) throws EncodeSuiteException {
        logger.info("[encode] cipher encode process, encryption with AES.");
        try {
            String key = KeyGenerator.getKey();
            Map<String, Object> keyMap = new HashMap<String, Object>();
            keyMap.put(ParamKeyConstant.KEY_DATA, key);
            keyMap.put(ParamKeyConstant.KEY_VERIFIERS, encodeData.getVerifiers());
            String saveData = DataToolUtils.serialize(keyMap);

            //将数据进行AES加密处理
            String value =
                CryptoServiceFactory
                    .getCryptoService(CryptoType.AES)
                    .encrypt(encodeData.getData(), key);

            //保存秘钥
            com.webank.weid.blockchain.protocol.response.ResponseData<Integer> response = this.getDataDriver().add(
                DataDriverConstant.DOMAIN_ENCRYPTKEY, encodeData.getId(), saveData);
            if (response.getErrorCode() != ErrorCode.SUCCESS.getCode()) {
                throw new EncodeSuiteException(
                    ErrorCode.getTypeByErrorCode(response.getErrorCode())
                );
            }
            logger.info("[encode] cipher encode process finished.");
            return value;
        } catch (EncodeSuiteException e) {
            logger.error("[encode] encode processor has some error.", e);
            throw e;
        } catch (Exception e) {
            logger.error("[encode] encode processor has unknow error.", e);
            throw new EncodeSuiteException(e);
        }
    }

    /**
     * 密文解码处理：先进行AES解密， 然后进行解压.
     */
    @Override
    public String decode(EncodeData encodeData) throws EncodeSuiteException {
        logger.info("[decode] cipher decode process, decryption with AES.");
        try {
            String key = this.getEntryptKey(encodeData);
            //将数据进行AES解密
            String value =
                CryptoServiceFactory
                    .getCryptoService(CryptoType.AES)
                    .decrypt(encodeData.getData(), key);
            //数据进行解压
            logger.info("[decode] cipher decode process finished.");
            return value;
        } catch (EncodeSuiteException e) {
            logger.error("[decode] decode processor has some error.", e);
            throw e;
        } catch (Exception e) {
            logger.error("[decode] decode processor has unknow error.", e);
            throw new EncodeSuiteException(e);
        }
    }

    /**
     * 获取秘钥.
     *
     * @param encodeData 编解码实体
     * @return return the key
     */
    private String getEntryptKey(EncodeData encodeData) {
        //说明是当前机构，这个时候不适用于AMOP获取key，而是从本地数据库中获取key
        if (com.webank.weid.blockchain.service.fisco.BaseServiceFisco.fiscoConfig.getAmopId().equals(encodeData.getAmopId())) {
            logger.info("get Encrypt Key from DB.");
            //保存秘钥
            com.webank.weid.blockchain.protocol.response.ResponseData<String> response =
                this.getDataDriver().get(DataDriverConstant.DOMAIN_ENCRYPTKEY, encodeData.getId());
            if (response.getErrorCode().intValue() != KitErrorCode.SUCCESS.getCode()) {
                throw new EncodeSuiteException(
                    ErrorCode.getTypeByErrorCode(response.getErrorCode().intValue())
                );
            }
            return this.getEncryptKey(encodeData, response.getResult());
        } else {
            logger.info("get Encrypt Key By AMOP.");
            //获取秘钥，
            return this.requestEncryptKeyByAmop(encodeData);
        }
    }

    /**
     * 本机构取秘钥(需要判断权限控制).
     *
     * @param encodeData 编解码实体
     * @param value 数据库中存储的秘钥结构数据
     * @return 返回秘钥
     */
    private String getEncryptKey(EncodeData encodeData, String value) {
        if (encodeData.getWeIdAuthentication() == null) {
            logger.info("[getEncryptKey] the weid Authentication is null.");
            throw new EncodeSuiteException(ErrorCode.ENCRYPT_KEY_NO_PERMISSION);
        }
        try {
            Map<String, Object> keyMap = DataToolUtils.deserialize(
                value,
                new HashMap<String, Object>().getClass()
            );
            String weId = encodeData.getWeIdAuthentication().getWeId();
            List<String> verifiers = (ArrayList<String>) keyMap.get(ParamKeyConstant.KEY_VERIFIERS);
            // 如果verifiers为empty,或者传入的weId为空，或者weId不在指定列表中，则无权限获取秘钥数据
            if (CollectionUtils.isEmpty(verifiers)
                || StringUtils.isBlank(weId)
                || !verifiers.contains(weId)) {
                logger.error("[getEncryptKey] no access to get the data, this weid is {}.", weId);
                throw new EncodeSuiteException(ErrorCode.ENCRYPT_KEY_NO_PERMISSION);
            }
            return (String) keyMap.get(ParamKeyConstant.KEY_DATA);
        } catch (DataTypeCastException e) {
            logger.error("[getEncryptKey] deserialize the data error, you should upgrade SDK.", e);
            throw new EncodeSuiteException(ErrorCode.ENCRYPT_KEY_INVALID);
        }
    }

    /**
     * 通过AMOP获取秘钥.
     *
     * @param encodeData 编解码实体
     * @return 返回秘钥
     */
    private String requestEncryptKeyByAmop(EncodeData encodeData) {
        GetEncryptKeyArgs args = new GetEncryptKeyArgs();
        args.setKeyId(encodeData.getId());
        args.setMessageId(DataToolUtils.getUuId32());
        //args.setToAmopId(encodeData.getAmopId());
        //args.setFromAmopId(fiscoConfig.getAmopId());
        if (encodeData.getWeIdAuthentication() != null) {
            /*String signValue = DataToolUtils.secp256k1Sign(
                encodeData.getId(),
                new BigInteger(
                    encodeData.getWeIdAuthentication().getWeIdPrivateKey().getPrivateKey())
            );*/
            String signature = DataToolUtils.SigBase64Serialization(
                    DataToolUtils.signToRsvSignature(encodeData.getId(), encodeData.getWeIdAuthentication().getWeIdPrivateKey().getPrivateKey())
            );
            args.setSignValue(signature);
            args.setWeId(encodeData.getWeIdAuthentication().getWeId());
        }
        ResponseData<GetEncryptKeyResponse> resResponse =
            amopService.getEncryptKey(encodeData.getAmopId(), args);
        if (resResponse.getErrorCode().intValue() != KitErrorCode.SUCCESS.getCode()) {
            logger.error(
                "[requestEncryptKeyByAmop] AMOP response fail, dataId={}, "
                    + "errorCode={}, errorMessage={}",
                encodeData.getId(),
                resResponse.getErrorCode(),
                resResponse.getErrorMessage()
            );
            throw new EncodeSuiteException(
                ErrorCode.getTypeByErrorCode(resResponse.getErrorCode().intValue())
            );
        }
        GetEncryptKeyResponse keyResponse = resResponse.getResult();
        KitErrorCode kitErrorCode =
            KitErrorCode.getTypeByErrorCode(keyResponse.getErrorCode().intValue());
        if (kitErrorCode.getCode() != KitErrorCode.SUCCESS.getCode()) {
            logger.error(
                "[requestEncryptKeyByAmop] requestEncryptKey error, dataId={},"
                    + " errorCode={}, errorMessage={}",
                encodeData.getId(),
                keyResponse.getErrorCode(),
                keyResponse.getErrorMessage()
            );
            throw new EncodeSuiteException(ErrorCode.getTypeByErrorCode(kitErrorCode.getCode()));
        }
        return keyResponse.getEncryptKey();
    }
}
