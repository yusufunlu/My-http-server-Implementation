package com.king.httpserver;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static com.king.httpserver.Util.*;

public class HttpServerTest {

    private static final String CONTEXT = "/";
    private static final int PORT = 8081;
    private static String ADDRESS = "http://localhost";

    static int[] userIds= {1,2,3,4,5,6,7,8,9};
    static int[] levels = {10,10,10,10,10,10,10,10,10};
    static Integer[] scores = {100,200,300,400,500,600,700,800,900};

    public static void main(String[] args) {

        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(PORT, CONTEXT, new RootHandler());

        simpleHttpServer.start();
        System.out.println("King Server is started and listening on port "+ PORT + " context "+ CONTEXT);


        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        for(int i=0;i<100;i++) {
            executor.submit(new Task(i%9));
        }

    }

    public static String doHttpRequest(String targetURL, String body, String method) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(body);
            osw.flush();
            osw.close();
            os.close();
            connection.connect();

            //Get Response
            String result;
            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result2 = bis.read();
            while(result2 != -1) {
                buf.write((byte) result2);
                result2 = bis.read();
            }
            result = buf.toString();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    static class Task implements Runnable {

        int index = 0;
        public Task(int index) {
            this.index = index;
        }

        public void run() {

            String sessionKey = doHttpRequest(String.format("%s:%d/%d/%s", ADDRESS, PORT, userIds[index], LOGIN_PATH),
                    "",
                    "GET");
            doHttpRequest(
                    String.format("%s:%d/%d/%s?%s=%s", ADDRESS, PORT, levels[index], SCORE_PATH,SESSION_QUERY_KEY,sessionKey),
                    scores[index].toString(),
                    "POST");

            String hightScoreList = doHttpRequest(String.format("%s:%d/%d/%s", ADDRESS, PORT, levels[index], HIGHSCORE_PATH),
                    "",
                    "GET");

            //System.out.println("sessionKey: "+ sessionKey);
            System.out.printf("level %d hightScoreList: %s%n",levels[index], hightScoreList);
        }
    }
}