package com.king.gameserver;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.king.gameserver.Util.*;

public class HttpServerTest {

    private static final String CONTEXT = "/";
    private static final int PORT = 8081;
    private static String ADDRESS = "http://localhost";
    private final static int BACKLOG = 100;

    static int[] levels = {10,10,10,10,10,10,10,10,10,10};
    static int[] userIds=       {2,  1,  1, 3,  1,  4,  1,  7,  8,  9,  5,  6,  7,  8,   9,  10,11, 12,13,14, 15, 16};
    static Integer[] scores =   {100,200,50,400,500,350,700,800,900,850,711,853,999,1001,777,42,177,63,26,292,711,888};
    static int level = 10;

    public static void main(String[] args) {

        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(PORT, CONTEXT, new RootHandler(),BACKLOG);

        simpleHttpServer.start();
        System.out.println("King Server is started and listening on port "+ PORT + " context "+ CONTEXT);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        for(int i=0;i<100;i++) {
            executor.execute(new Task(i%22));
        }



        try {
            while (executor.getActiveCount()!=0) {

            }

            printHighScores();
            Arrays.stream(scores).sorted((f1, f2) -> Long.compare(f2, f1)).limit(15).forEach(s-> System.out.println("score: "+ s));
        } catch (Exception exception) {
            exception.printStackTrace();
        }


    }


    public static void printHighScores() {
        String hightScoreList = doHttpRequest(String.format("%s:%d/%d/%s", ADDRESS, PORT, 10, HIGHSCORE_PATH),
                "",
                "GET");

        //System.out.println("sessionKey: "+ sessionKey);
        System.out.printf("level %d hightScoreList: %s%n",10, hightScoreList);
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

            //retrieve response
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
                    String.format("%s:%d/%d/%s?%s=%s", ADDRESS, PORT, level, SCORE_PATH,SESSION_QUERY_KEY,sessionKey),
                    scores[index].toString(),
                    "POST");

        }
    }
}