

package com.webank.weid.kit.crypto.impl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.exception.EncodeSuiteException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.kit.crypto.inf.CryptoService;
import com.webank.weid.kit.crypto.params.CryptoType;
import com.webank.weid.util.DataToolUtils;

public class RsaCryptoService implements CryptoService {

    private static final Logger logger = LoggerFactory.getLogger(RsaCryptoService.class);

    private static final String KEY_ALGORITHM = CryptoType.RSA.name();
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    @Override
    public String encrypt(String content, String key) throws EncodeSuiteException {
        logger.info("begin encrypt by RSA");
        checkForEncrypt(content, key);
        try {
            byte[] pubByte = Base64.decodeBase64(key);
            PublicKey pub = KeyFactory.getInstance(KEY_ALGORITHM)
                .generatePublic(new X509EncodedKeySpec(pubByte));
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, pub);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            logger.error("RSA encrypt error, please check the log.", e);
            throw new EncodeSuiteException();
        }
    }
    
    private void checkForEncrypt(String content, String key) {
        // 入参非空检查
        String errorMessage = null;
        if (StringUtils.isEmpty(content)) {
            errorMessage = "input content is null.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        }
        // 检查content是否为utf-8
        boolean isUtf8 = Charset.forName(StandardCharsets.UTF_8.toString())
            .newEncoder().canEncode(content);
        if (!isUtf8) {
            errorMessage = "input content is not utf-8.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        }
        // 入参非空检查
        if (StringUtils.isEmpty(key)) {
            errorMessage = "input publicKey is null.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        }
        // 检查publicKey是否为标准base64格式
        if (!DataToolUtils.isValidBase64String(key)) {
            errorMessage = "input publicKey is not a valid Base64 string.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        }
    }
    
    @Override
    public String decrypt(String content, String key) throws EncodeSuiteException {
        logger.info("begin decrypt by RSA");
        checkForDecrypt(content, key);
        try {
            // 64位解码加密后的字符串
            byte[] inputByte = Base64.decodeBase64(content);
            byte[] priByte = Base64.decodeBase64(key);
            PrivateKey priKey = KeyFactory.getInstance(KEY_ALGORITHM)
                .generatePrivate(new PKCS8EncodedKeySpec(priByte));
            // RSA解密
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            return new String(cipher.doFinal(inputByte), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("RAS decrypt error, please check the log.", e);
            throw new EncodeSuiteException();
        }
    }
    
    private void checkForDecrypt(String content, String key) {
        // 入参非空检查
        String errorMessage = null;
        if (StringUtils.isEmpty(content)) {
            errorMessage = "input content is null.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        }
        // 检查content是否为标准base64格式
        if (!DataToolUtils.isValidBase64String(content)) {
            errorMessage = "input content is not a valid Base64 string.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        }
        // 入参非空检查
        if (StringUtils.isEmpty(key)) {
            errorMessage = "input privateKey is null.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        }
        // 检查privateKey是否为标准base64格式
        if (!DataToolUtils.isValidBase64String(key)) {
            errorMessage = "input privateKey is not a valid Base64 string.";
            throw new EncodeSuiteException(KitErrorCode.ILLEGAL_INPUT, errorMessage);
        } 
    }
}
