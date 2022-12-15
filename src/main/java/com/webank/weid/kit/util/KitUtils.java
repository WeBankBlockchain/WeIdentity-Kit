
package com.webank.weid.kit.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.webank.weid.exception.DataTypeCastException;
import com.webank.weid.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 数据工具类.
 *
 * @author tonychen 2019年4月23日
 */
public final class KitUtils {

    private static final Logger logger = LoggerFactory.getLogger(KitUtils.class);
    private static final String SEPARATOR_CHAR = "-";

    private static final int radix = 10;

    private static final String TO_JSON = "toJson";

    private static final String FROM_JSON = "fromJson";

    private static final String KEY_CREATED = "created";

    private static final String KEY_ISSUANCEDATE = "issuanceDate";

    private static final String KEY_EXPIRATIONDATE = "expirationDate";

    private static final String KEY_CLAIM = "claim";

    private static final String KEY_FROM_TOJSON = "$from";

    private static final List<String> CONVERT_UTC_LONG_KEYLIST = new ArrayList<>();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    //private static final ObjectWriter OBJECT_WRITER;
    //private static final ObjectReader OBJECT_READER;
    private static final ObjectWriter OBJECT_WRITER_UN_PRETTY_PRINTER;

    private static final com.networknt.schema.JsonSchemaFactory JSON_SCHEMA_FACTORY;
    /**
     * use this to create key pair of v2 or v3
     * WARN: create keyPair must use BigInteger of privateKey or decimal String of privateKey
     */
    static {
        // sort by letter
        OBJECT_MAPPER.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        // when map is serialization, sort by key
        OBJECT_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        // ignore mismatched fields
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        // use field for serialize and deSerialize
        OBJECT_MAPPER.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        OBJECT_WRITER_UN_PRETTY_PRINTER = OBJECT_MAPPER.writer();

        CONVERT_UTC_LONG_KEYLIST.add(KEY_CREATED);
        CONVERT_UTC_LONG_KEYLIST.add(KEY_ISSUANCEDATE);
        CONVERT_UTC_LONG_KEYLIST.add(KEY_EXPIRATIONDATE);

        //OBJECT_WRITER = OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
        //OBJECT_READER = OBJECT_MAPPER.reader();

        JSON_SCHEMA_FACTORY = com.networknt.schema.JsonSchemaFactory.getInstance(VersionFlag.V4);
    }

    /**
     * serialize a class instance to Json String.
     *
     * @param object the class instance to serialize
     * @param <T> the type of the element
     * @return JSON String
     */
    public static <T> String serialize(T object) {
        Writer write = new StringWriter();
        try {
            OBJECT_MAPPER.writeValue(write, object);
        } catch (JsonGenerationException e) {
            logger.error("JsonGenerationException when serialize object to json", e);
        } catch (JsonMappingException e) {
            logger.error("JsonMappingException when serialize object to json", e);
        } catch (IOException e) {
            logger.error("IOException when serialize object to json", e);
        }
        return write.toString();
    }

    /**
     * deserialize a JSON String to an class instance.
     *
     * @param json json string
     * @param clazz Class.class
     * @param <T> the type of the element
     * @return class instance
     */
    public static <T> T deserialize(String json, Class<T> clazz) {
        Object object = null;
        try {
            if (isValidFromToJson(json)) {
                logger.error("this jsonString is converted by toJson(), "
                    + "please use fromJson() to deserialize it");
                throw new DataTypeCastException("deserialize json to Object error");
            }
            object = OBJECT_MAPPER.readValue(json, TypeFactory.rawClass(clazz));
        } catch (JsonParseException e) {
            logger.error("JsonParseException when deserialize json to object", e);
            throw new DataTypeCastException(e);
        } catch (JsonMappingException e) {
            logger.error("JsonMappingException when deserialize json to object", e);
            throw new DataTypeCastException(e);
        } catch (IOException e) {
            logger.error("IOException when deserialize json to object", e);
            throw new DataTypeCastException(e);
        }
        return (T) object;
    }

    /**
     * Load Json Object. Can be used to return both Json Data and Json Schema.
     *
     * @param jsonString the json string
     * @return JsonNode
     * @throws JsonProcessingException parse json fail
     */
    public static JsonNode loadJsonObject(String jsonString) throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(jsonString);

    }

    /**
     * load json from resource
     * @param path class path file path
     * @return json node
     * @throws IOException load error
     */
    public static JsonNode loadJsonObjectFromResource(String path) throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new DataTypeCastException("open path to inputStream get null!");
            }
            return OBJECT_MAPPER.readTree(inputStream);
        }
    }

    /**
     * encrypt the data. todo
     *
     * @param data the data to encrypt
     * @param publicKey public key
     * @return decrypt data
     * @throws Exception encrypt exception
     */
    public static byte[] encrypt(String data, String publicKey) throws Exception {
        /*
        cryptoSuite.ECCEncrypt encrypt = new ECCEncrypt(new BigInteger(publicKey));
            return encrypt.encrypt(data.getBytes());

         */
        return data.getBytes();
    }


    /**
     * decrypt the data. todo
     *
     * @param data the data to decrypt
     * @param privateKey private key
     * @return original data
     * @throws Exception decrypt exception
     */
    public static byte[] decrypt(byte[] data, String privateKey) throws Exception {

        /*ECCDecrypt decrypt = new ECCDecrypt(new BigInteger(privateKey));
        return decrypt.decrypt(data);*/
        return data;
    }

    /**
     * Compress JSON String.
     *
     * @param arg the compress string
     * @return return the value of compressed
     * @throws IOException IOException
     */
    public static String compress(String arg) throws IOException {
        if (null == arg || arg.length() <= 0) {
            return arg;
        }
        ByteArrayOutputStream out = null;
        GZIPOutputStream gzip = null;
        try {
            out = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(out);
            gzip.write(arg.getBytes(StandardCharsets.UTF_8.toString()));
            close(gzip);
            String value = out.toString(StandardCharsets.ISO_8859_1.toString());
            return value;
        } finally {
            close(out);
        }
    }

    /**
     * Decompression of String data.
     *
     * @param arg String data with decompression
     * @return return the value of decompression
     * @throws IOException IOException
     */
    public static String unCompress(String arg) throws IOException {
        if (null == arg || arg.length() <= 0) {
            return arg;
        }
        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        GZIPInputStream gzip = null;
        try {
            out = new ByteArrayOutputStream();
            in = new ByteArrayInputStream(arg.getBytes(StandardCharsets.ISO_8859_1.toString()));
            gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n = 0;
            while ((n = gzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            String value = out.toString(StandardCharsets.UTF_8.toString());
            return value;
        } finally {
            close(gzip);
            close(in);
            close(out);
        }
    }

    private static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                logger.error("close OutputStream error", e);
            }
        }
    }

    private static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                logger.error("close InputStream error", e);
            }
        }
    }

    /**
     * convert timestamp to UTC of json string.
     *
     * @param jsonString json string
     * @return timestampToUtcString
     */
    public static String convertTimestampToUtc(String jsonString) {
        String timestampToUtcString;
        try {
            timestampToUtcString = dealNodeOfConvertUtcAndLong(
                loadJsonObject(jsonString),
                CONVERT_UTC_LONG_KEYLIST,
                TO_JSON
            ).toString();
        } catch (IOException e) {
            logger.error("replaceJsonObj exception.", e);
            throw new DataTypeCastException(e);
        }
        return timestampToUtcString;
    }

    /**
     * convert UTC Date to timestamp of Json string.
     *
     * @param jsonString presentationJson
     * @return presentationJson after convert
     */
    public static String convertUtcToTimestamp(String jsonString) {
        String utcToTimestampString;
        try {
            utcToTimestampString = dealNodeOfConvertUtcAndLong(
                loadJsonObject(jsonString),
                CONVERT_UTC_LONG_KEYLIST,
                FROM_JSON
            ).toString();
        } catch (IOException e) {
            logger.error("replaceJsonObj exception.", e);
            throw new DataTypeCastException(e);
        }
        return utcToTimestampString;
    }

    private static JsonNode dealNodeOfConvertUtcAndLong(
        JsonNode jsonObj,
        List<String> list,
        String type) {
        if (jsonObj.isObject()) {
            return dealObjectOfConvertUtcAndLong((ObjectNode) jsonObj, list, type);
        } else if (jsonObj.isArray()) {
            return dealArrayOfConvertUtcAndLong((ArrayNode) jsonObj, list, type);
        } else {
            return jsonObj;
        }
    }

    private static JsonNode dealObjectOfConvertUtcAndLong(
        ObjectNode jsonObj,
        List<String> list,
        String type) {
        ObjectNode resJson = OBJECT_MAPPER.createObjectNode();
        jsonObj.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode obj = entry.getValue();
            if (obj.isObject()) {
                //JSONObject
                if (key.equals(KEY_CLAIM)) {
                    resJson.set(key, obj);
                } else {
                    resJson.set(key, dealObjectOfConvertUtcAndLong((ObjectNode) obj, list, type));
                }
            } else if (obj.isArray()) {
                //JSONArray 
                resJson.set(key, dealArrayOfConvertUtcAndLong((ArrayNode) obj, list, type));
            } else {
                if (list.contains(key)) {
                    if (TO_JSON.equals(type)) {
                        if (isValidLongString(obj.asText())) {
                            resJson.put(
                                key,
                                DateUtils.convertNoMillisecondTimestampToUtc(
                                    Long.parseLong(obj.asText())));
                        } else {
                            resJson.set(key, obj);
                        }
                    } else {
                        if (DateUtils.isValidDateString(obj.asText())) {
                            resJson.put(
                                key,
                                DateUtils.convertUtcDateToNoMillisecondTime(obj.asText()));
                        } else {
                            resJson.set(key, obj);
                        }
                    }
                } else {
                    resJson.set(key, obj);
                }
            }
        });
        return resJson;
    }

    private static JsonNode dealArrayOfConvertUtcAndLong(
        ArrayNode jsonArr,
        List<String> list,
        String type) {
        ArrayNode resJson = OBJECT_MAPPER.createArrayNode();
        for (int i = 0; i < jsonArr.size(); i++) {
            JsonNode jsonObj = jsonArr.get(i);
            if (jsonObj.isObject()) {
                resJson.add(dealObjectOfConvertUtcAndLong((ObjectNode) jsonObj, list, type));
            } else if (jsonObj.isArray()) {
                resJson.add(dealArrayOfConvertUtcAndLong((ArrayNode) jsonObj, list, type));
            } else {
                resJson.add(jsonObj);
            }
        }
        return resJson;
    }

    /**
     * valid string is a long type.
     *
     * @param str string
     * @return result
     */
    public static boolean isValidLongString(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }

        long result = 0;
        int i = 0;
        int len = str.length();
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        char firstChar = str.charAt(0);
        if (firstChar <= '0') {
            return false;
        }
        multmin = limit / radix;
        while (i < len) {
            digit = Character.digit(str.charAt(i++), radix);
            if (digit < 0) {
                return false;
            }
            if (result < multmin) {
                return false;
            }
            result *= radix;
            if (result < limit + digit) {
                return false;
            }
            result -= digit;
        }
        return true;
    }

    /**
     * valid the json string is converted by toJson().
     *
     * @param json jsonString
     * @return result
     */
    private static boolean isValidFromToJson(String json) {
        if (StringUtils.isBlank(json)) {
            logger.error("input json param is null.");
            return false;
        }
        JsonNode jsonObject = null;
        try {
            jsonObject = loadJsonObject(json);
        } catch (IOException e) {
            logger.error("convert jsonString to JSONObject failed." + e);
            return false;
        }
        return jsonObject.has(KEY_FROM_TOJSON);
    }

    /**
     * Check whether a URL String is a valid endpoint.
     *
     * @param url the endpoint url
     * @return true if yes, false otherwise
     */
    public static boolean isValidEndpointUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }
        String hostname;
        Integer port;
        String endpointName;
        try {
            URI uri = new URI(url);
            hostname = uri.getHost();
            port = uri.getPort();
            String path = uri.getPath();
            if (StringUtils.isEmpty(hostname) || StringUtils.isEmpty(path) || port < 0) {
                logger.error("Service URL illegal: {}", url);
                return false;
            }
            // Truncate the first slash
            endpointName = path.substring(1);
            if (StringUtils.isEmpty(endpointName)) {
                return false;
            }
        } catch (Exception e) {
            logger.error("Service URL format check failed: {}", url);
            return false;
        }
        return true;
    }

    /**
     * Check whether the address is local address.
     *
     * @param host host string
     * @return true if yes, false otherwise
     */
    public static boolean isLocalAddress(String host) {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            logger.error("Unkown host: " + host);
            return false;
        }
        // Check if the address is a valid special local or loop back
        if (addr.isSiteLocalAddress() || addr.isAnyLocalAddress() || addr.isLoopbackAddress()
            || addr.isLinkLocalAddress()) {
            return true;
        }
        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            return false;
        }
    }

    /**
     * Strictly check two lists' elements existence whether items in src exists in dst list or not.
     *
     * @param src source list
     * @param dst dest list
     * @return boolean list, each true / false indicating existing or not.
     */
    public static List<Boolean> strictCheckExistence(List<String> src, List<String> dst) {
        List<Boolean> result = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < src.size(); i++) {
            if (src.get(i).equalsIgnoreCase(dst.get(index))) {
                result.add(true);
                index++;
            } else {
                result.add(false);
            }
        }
        return result;
    }
}

