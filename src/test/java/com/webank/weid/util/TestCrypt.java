

package com.webank.weid.util;

import com.webank.weid.common.PasswordKey;
import com.webank.weid.full.TestBaseUtil;
import com.webank.weid.kit.crypto.CryptoServiceFactory;
import com.webank.weid.kit.crypto.params.Asymmetrickey;
import com.webank.weid.kit.crypto.params.CryptoType;
import com.webank.weid.kit.crypto.params.KeyGenerator;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCrypt {
    
    private static final Logger logger = LoggerFactory.getLogger(TestCrypt.class);
    
    private static String json = "{\"claim\":{\"a\":\"a\",\"b\":\"b\"},\"cptId\":2000099,"
            + "\"expirationDate\":1903510144,\"id\":\"4d4bb35e-6335-4e81-aae9-6a3b88ca04f3\","
            + "\"issuer\":\"did:weid:101:0x110f0ed41b33b1395b9060d274247c2f9e15b29a\","
            + "\"type\":\"lite1\"}";
    
    private static final String original = "{\"name\":\"zhangsan\",age:12}";

    private static final CryptoSuite cryptoSuite = com.webank.weid.blockchain.service.fisco.CryptoFisco.cryptoSuite;


    @Test
    public void testAes() {
        String key = KeyGenerator.getKey();
        logger.info("key: {}", key);
        logger.info("original: {}", original);
        String encrypt = CryptoServiceFactory.getCryptoService(CryptoType.AES)
            .encrypt(original, key);
        logger.info("encrypt: {}", encrypt);
        String decrypt = CryptoServiceFactory.getCryptoService(CryptoType.AES)
            .decrypt(encrypt, key);
        logger.info("decrypt: {}", decrypt);
        Assert.assertEquals(original, decrypt);
    }

    @Test
    public void testRsa() throws Exception {
        Asymmetrickey key = KeyGenerator.getKeyForRsa(2048);
        logger.info("pub key: {}", key.getPublicKey());
        logger.info("pri key: {}", key.getPrivavteKey());
        logger.info("original: {}", original);
        String encrypt = CryptoServiceFactory.getCryptoService(CryptoType.RSA)
            .encrypt(original, key.getPublicKey());
        logger.info("encrypt: {}", encrypt);
        String decrypt = CryptoServiceFactory.getCryptoService(CryptoType.RSA)
            .decrypt(encrypt, key.getPrivavteKey());
        logger.info("decrypt: {}", decrypt);
        Assert.assertEquals(original, decrypt);
    }
    
    @Test
    @Ignore
    public void testEcies_withPadding() throws Exception {
        // 外围有padding操作
        for (int i = 0; i < 1000; i++) {
            /*ECKeyPair keyPair = TestBaseUtil.createKeyPair();
            String publicKey = keyPair.getPublicKey().toString();
            String privateKey = keyPair.getPrivateKey().toString();*/
            PasswordKey createEcKeyPair = TestBaseUtil.createEcKeyPair();
            String publicKey = createEcKeyPair.getPublicKey();
            String privateKey = createEcKeyPair.getPrivateKey();
            String pubBase64 = KeyGenerator.decimalKeyToBase64(publicKey);
            String priBase64 = KeyGenerator.decimalKeyToBase64(privateKey);
            logger.info("pub key base64: {}", pubBase64);
            logger.info("pri key base64: {}", priBase64);
            String original = json;
            String encrypt = CryptoServiceFactory.getCryptoService(CryptoType.ECIES)
                .encrypt(original, pubBase64);
            logger.info("encrypt: {}", encrypt);
            String decrypt = CryptoServiceFactory.getCryptoService(CryptoType.ECIES)
                .decrypt(encrypt, priBase64);
            logger.info("decrypt: {}", decrypt);
            Assert.assertEquals(cryptoSuite.hash(original), cryptoSuite.hash(decrypt));
            Assert.assertEquals(original, decrypt);
        } 
    }
    
    @Test
    @Ignore
    public void testEcies_noPadding() throws Exception {
        // 外围没有padding操作
        for (int i = 0; i < 1000; i++) {
            /*ECKeyPair keyPair = TestBaseUtil.createKeyPair();
            String publicKey = keyPair.getPublicKey().toString();
            String privateKey = keyPair.getPrivateKey().toString();*/
            PasswordKey createEcKeyPair = TestBaseUtil.createEcKeyPair();
            String publicKey = createEcKeyPair.getPublicKey();
            String privateKey = createEcKeyPair.getPrivateKey();
            logger.info("pub key: {}", publicKey);
            logger.info("pri key: {}", privateKey);
            String original = json;
            String encrypt = CryptoServiceFactory.getCryptoService(CryptoType.ECIES)
                .encrypt(original, publicKey);
            logger.info("encrypt: {}", encrypt);
            String decrypt = CryptoServiceFactory.getCryptoService(CryptoType.ECIES)
                .decrypt(encrypt, privateKey);
            logger.info("decrypt: {}", decrypt);
            Assert.assertEquals(cryptoSuite.hash(original), cryptoSuite.hash(decrypt));
            Assert.assertEquals(original, decrypt);
        } 
    }
    
    @Test
    public void testDecimalKey() throws Exception {
        for (int i = 0; i < 1000; i++) {
            /*ECKeyPair keyPair = TestBaseUtil.createKeyPair();
            String publicKey = keyPair.getPublicKey().toString();
            String privateKey = keyPair.getPrivateKey().toString();*/
            PasswordKey createEcKeyPair = TestBaseUtil.createEcKeyPair();
            String publicKey = createEcKeyPair.getPublicKey();
            String privateKey = createEcKeyPair.getPrivateKey();
            String pubBase64 = KeyGenerator.decimalKeyToBase64(publicKey);
            String priBase64 = KeyGenerator.decimalKeyToBase64(privateKey);
            String decimalPubKey = KeyGenerator.base64KeyTodecimal(pubBase64);
            String decimalPriKey = KeyGenerator.base64KeyTodecimal(priBase64);
            Assert.assertEquals(cryptoSuite.hash(publicKey), cryptoSuite.hash(decimalPubKey));
            Assert.assertEquals(cryptoSuite.hash(privateKey), cryptoSuite.hash(decimalPriKey));
            Assert.assertEquals(publicKey, decimalPubKey);
            Assert.assertEquals(privateKey, decimalPriKey);
        } 
    }
}
