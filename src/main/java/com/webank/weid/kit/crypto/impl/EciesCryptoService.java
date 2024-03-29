

package com.webank.weid.kit.crypto.impl;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.webank.weid.blockchain.constant.ErrorCode;
import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.exception.EncodeSuiteException;
import com.webank.wedpr.ecies.EciesResult;
import com.webank.wedpr.ecies.NativeInterface;

import com.webank.weid.kit.crypto.params.KeyGenerator;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.kit.crypto.inf.CryptoService;
import com.webank.weid.util.DataToolUtils;

public class EciesCryptoService implements CryptoService {

    private static final Logger logger = LoggerFactory.getLogger(EciesCryptoService.class);

    @Override
    public String encrypt(String content, String key) throws EncodeSuiteException {
        logger.info("begin encrypt by ecies.");
        checkForEncrypt(content, key);
        String data = Numeric.toHexStringNoPrefix(content.getBytes(StandardCharsets.UTF_8));
        String pubValue = null;
        if (StringUtils.isNumeric(key)) {
            //如果为10进制数字
            BigInteger bigInt = new BigInteger(key);
            //pubValue = Numeric.toHexStringNoPrefixZeroPadded(bigInt, Keys.PUBLIC_KEY_LENGTH_IN_HEX);
            pubValue = Numeric.toHexStringNoPrefixZeroPadded(
                    bigInt, KeyGenerator.PUBLIC_KEY_LENGTH_IN_HEX);
        } else {
            if (!DataToolUtils.isValidBase64String(key)) {
                String errorMessage = "input publicKey is not a valid Base64 string.";
                throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
            }
            pubValue = Numeric.toHexStringNoPrefix(Base64.decodeBase64(key));
        }
        EciesResult result =  NativeInterface.eciesEncrypt(pubValue, data); // 加密
        if (result != null) {
            if (StringUtils.isBlank(result.wedprErrorMessage)) {
                logger.info("encrypt by ecies successfully.");
                return Base64.encodeBase64String(
                    Numeric.hexStringToByteArray(result.encryptMessage));
            }
            logger.error("encrypt by ecies fail, message = {}.", result.wedprErrorMessage);
            throw new EncodeSuiteException(result.wedprErrorMessage);
        }
        throw new EncodeSuiteException(ErrorCode.UNKNOW_ERROR);
    }

    private void checkForEncrypt(String content, String key) {
        check(content, key);
        // 检查content是否为utf-8
        boolean isUtf8 = Charset.forName(StandardCharsets.UTF_8.toString())
            .newEncoder().canEncode(content);
        if (!isUtf8) {
            String errorMessage = "input content is not utf-8.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        }
    }

    @Override
    public String decrypt(String content, String key) throws EncodeSuiteException {
        logger.info("begin decrypt by ecies.");
        checkForDecrypt(content, key);
        String data = Numeric.toHexStringNoPrefix(Base64.decodeBase64(content));
        String priValue = null;
        if (StringUtils.isNumeric(key)) {
            //如果为10进制数字
            BigInteger bigInt = new BigInteger(key);
            priValue = Numeric.toHexStringNoPrefixZeroPadded(
                    bigInt, KeyGenerator.PRIVATE_KEY_LENGTH_IN_HEX);
                //bigInt, Keys.PRIVATE_KEY_LENGTH_IN_HEX);
        } else {
            if (!DataToolUtils.isValidBase64String(key)) {
                String errorMessage = "input privateKey is not a valid Base64 string.";
                throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
            }
            priValue = Numeric.toHexStringNoPrefix(Base64.decodeBase64(key));
        }
        EciesResult deResult =  NativeInterface.eciesDecrypt(priValue, data);
        if (deResult != null) {
            if (StringUtils.isBlank(deResult.wedprErrorMessage)) {
                logger.info("decrypt by ecies successfully.");
                byte[] buffer = Numeric.hexStringToByteArray(deResult.decryptMessage);
                return new String(buffer, StandardCharsets.UTF_8);
            }
            logger.error("decrypt by ecies fail, message = {}.", deResult.wedprErrorMessage);
            throw new EncodeSuiteException(deResult.wedprErrorMessage);
        }
        throw new EncodeSuiteException(ErrorCode.UNKNOW_ERROR);
    }

    private void checkForDecrypt(String content, String key) {
        check(content, key);
        // 检查content是否为标准base64格式
        if (!DataToolUtils.isValidBase64String(content)) {
            String errorMessage = "input content is not a valid Base64 string.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        }
    }

    private static void check(String content, String key) {
        // 入参非空检查
        String errorMessage = null;
        if (StringUtils.isEmpty(content)) {
            errorMessage = "input content is null.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        }
        // 入参非空检查
        if (StringUtils.isEmpty(key)) {
            errorMessage = "input key is null.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        }
    }
}
