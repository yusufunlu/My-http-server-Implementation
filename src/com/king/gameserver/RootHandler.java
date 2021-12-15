package com.king.gameserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.king.gameserver.Util.*;

@SuppressWarnings("restriction")
public class RootHandler implements HttpHandler {

    //if userid is allowed to be saved in cookie, by set-cookie I wouldn't need to use sessionid map

    ConcurrentHashMap<String, Integer> sessionUserMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, String> userSessionMap = new ConcurrentHashMap<>();

    ConcurrentHashMap<Integer, ConcurrentSkipListMap <Integer, Integer>> scoreMapping = new ConcurrentHashMap<>();


    public void handle(HttpExchange httpExchange){

        URI uri = httpExchange.getRequestURI();

        String path = uri.getPath();
        String[] subPath = path.split(PATH_DELIMITER);

        String response = "";

        try {
            //<userid>/login
            //<levelid>/score?sessionkey=<sessionkey>
            //<levelid>/highscorelist

            if (subPath[2].equalsIgnoreCase(LOGIN_PATH)) {
                int userId = Integer.parseInt(subPath[1]);
                response = createSession(userId);
            } else if (subPath[2].equalsIgnoreCase(SCORE_PATH)) {
                int levelId = Integer.parseInt(subPath[1]);
                int score = Util.getIntFromHttpExchange(httpExchange);
                postScore(levelId, Util.getQueryParam(uri), score);
            } else if (subPath[2].equalsIgnoreCase(HIGHSCORE_PATH)) {
                int levelid = Integer.parseInt(subPath[1]);
                response = getHightScoreList(levelid);
            }

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes().length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }


    //expired sessionkeys management is getting hard
    private String createSession(int userId) {

        String sessionkey = Util.randomString(36, userId);

        //prevent sessionUserMap to be bigger by time
        //
        synchronized (userSessionMap) {
            synchronized (sessionUserMap) {
                if (userSessionMap.contains(userId)) {
                    String oldSessionKey = userSessionMap.get(userId);
                    userSessionMap.put(userId, sessionkey);
                    sessionUserMap.remove(oldSessionKey);
                    sessionUserMap.put(sessionkey, userId);
                } else {
                    userSessionMap.put(userId, sessionkey);
                    sessionUserMap.put(sessionkey, userId);
                }
            }
        }

        return sessionkey;
    }


    //<levelid>/score?sessionkey=<sessionkey>
    private void postScore(int levelId, String sessionKey, int score) {

        Integer userId = sessionUserMap.get(sessionKey);


        try {
            scoreMapping.computeIfAbsent(levelId, k-> new ConcurrentSkipListMap <>());
            scoreMapping.computeIfPresent(levelId, (k, v) -> {
                if(v.containsKey(userId)) {
                    if(v.get(userId)<score){
                        v.put(userId, score);
                    }
                } else {
                    v.put(userId, score);
                }
                return v;
            });

            System.out.println("userid: " + userId + " score: " + scoreMapping.get(levelId).get(userId));
            System.out.println(String.format("levelid:%d <-post userId:%d score:%d",levelId,userId,score));

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    //<levelid>/highscorelist, 15 results like userid=score,userid=score
    private String getHightScoreList(int levelId) {

        StringBuilder sb = new StringBuilder();

        try {
            if(scoreMapping.containsKey(levelId)) {
                scoreMapping.get(levelId).entrySet().stream().limit(15).forEach(x -> {

                    sb.append(x.getKey()+"="+x.getValue()+",");
                    //System.out.println(String.format("levelid:%d score count:%d",levelId,scoreMapping.get(levelId).size()));
                });

                if(!sb.toString().isEmpty() && !sb.toString().isBlank()) {
                    sb.deleteCharAt(sb.length()-1);
                }

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }


        return sb.toString();
    }








}