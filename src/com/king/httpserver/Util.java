package com.king.httpserver;

import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;


public class Util {
    public static final String SESSION_QUERY_KEY = "sessionkey";
    public static final String LOGIN_PATH = "login";
    public static final String SCORE_PATH = "score";
    public static final String HIGHSCORE_PATH = "highscorelist";

    public static final int PARAM_NAME_IDX = 0;
    public static final int PARAM_VALUE_IDX = 1;

    public static final int HTTP_OK_STATUS = 200;

    public static final String PATH_DELIMITER = "/";
    public static final String AND_DELIMITER = "&";
    public static final String EQUAL_DELIMITER = "=";

    static final String sessionAlphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();



    protected static int getIntFromHttpExchange(HttpExchange httpExchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), "UTF8"));

        String number = reader.readLine();
        int n = Integer.parseInt(number);
        return n;
    }


    private static String getStringFromHttpExchange(HttpExchange httpExchange) throws IOException {

        InputStreamReader isr =  new InputStreamReader(httpExchange.getRequestBody(),"utf-8");
        BufferedReader br = new BufferedReader(isr);

        int b;
        StringBuilder buf = new StringBuilder(512);
        while ((b = br.read()) != -1) {
            buf.append((char) b);
        }

        br.close();
        isr.close();

        String requestBody = buf.toString();
        return requestBody;
    }

    protected static String getQueryParam(URI uri) {

        String value = "";

        //Get the request query
        String query = uri.getQuery();
        if (query != null) {
            String[] queryParams = query.split(AND_DELIMITER);
            if (queryParams.length > 0) {
                for (String qParam : queryParams) {
                    String[] param = qParam.split(EQUAL_DELIMITER);
                    if (param.length > 0) {
                        for (int i = 0; i < param.length; i++) {
                            if (SESSION_QUERY_KEY.equalsIgnoreCase(param[PARAM_NAME_IDX])) {
                                value = param[PARAM_VALUE_IDX];
                            }
                        }
                    }
                }
            }
        }

        return value;
    }

    //no need to use saved for later on using
    @Deprecated
    protected static String createHashForUser(int userId) throws NoSuchAlgorithmException {

        long timestamp = createSessionLifeLong();
        String data = String.valueOf(timestamp + userId);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
        for (int i = 0; i < encodedhash.length; i++) {
            String hex = Integer.toHexString(0xff & encodedhash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();

    }

    protected static long createSessionLifeLong(){
        int tenMinInMiliSec = 10*60*1000;
        long timestamp = System.currentTimeMillis() / tenMinInMiliSec;
        return timestamp;
    }

    //no need to use saved for later on using
    @Deprecated
    protected String encodeBase64(String data) throws UnsupportedEncodingException {
        byte[] encryptArray = Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8));

        String encstr = new String(encryptArray,"UTF-8");
        return encstr;

    }



    //problem: same for different users
    static String randomString(int len, int userId){

        Random generator = new Random(createSessionLifeLong() + userId);

        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(sessionAlphabet.charAt(generator.nextInt(sessionAlphabet.length())));
        return sb.toString();
    }


}
