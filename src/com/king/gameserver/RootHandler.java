package com.king.gameserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import static com.king.gameserver.Util.*;

@SuppressWarnings("restriction")
public class RootHandler implements HttpHandler {

    //if userid is allowed to be saved in cookie, by set-cookie I wouldn't need to use sessionid map

    ConcurrentHashMap<String, Integer> sessionUserMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, String> userSessionMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, TreeSet<UserScore>> scoreMap = new ConcurrentHashMap<>();

    public void handle(HttpExchange httpExchange) throws IOException {

        URI uri = httpExchange.getRequestURI();

        String path = uri.getPath();
        String [] subPath = path.split(PATH_DELIMITER);

        String response = "";


        //<userid>/login
        //<levelid>/score?sessionkey=<sessionkey>
        //<levelid>/highscorelist

        if(subPath[2].equalsIgnoreCase(LOGIN_PATH)) {
            int userId = Integer.parseInt(subPath[1]);
            response = createSession(userId);
        } else if(subPath[2].equalsIgnoreCase(SCORE_PATH)){
            int levelId = Integer.parseInt(subPath[1]);
            int score = Util.getIntFromHttpExchange(httpExchange);
            postScore(levelId, Util.getQueryParam(uri),score);
        } else if(subPath[2].equalsIgnoreCase(HIGHSCORE_PATH)){
            int levelid = Integer.parseInt(subPath[1]);
            response = getHightScoreList(levelid);
        }

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes().length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }



    //expired sessionkeys management is getting hard
    private String createSession(int userId) {

        String sessionkey = Util.randomString(36, userId);

        //prevent sessionUserMap to be bigger by time
        //
        synchronized (userSessionMap) {
            synchronized (sessionUserMap) {
                if(userSessionMap.contains(userId)){
                    String oldSessionKey = userSessionMap.get(userId);
                    userSessionMap.put(userId,sessionkey);
                    sessionUserMap.remove(oldSessionKey);
                    sessionUserMap.put(sessionkey,userId);
                } else {
                    userSessionMap.put(userId,sessionkey);
                    sessionUserMap.put(sessionkey,userId);
                }
            }
        }

        return sessionkey;
    }


    //<levelid>/score?sessionkey=<sessionkey>
    private void postScore(int levelId,String sessionKey, int score) {

        Integer userId = sessionUserMap.get(sessionKey);

        TreeSet<UserScore> userScoreTreeSet = scoreMap.computeIfAbsent(levelId, k -> new TreeSet<>());
        synchronized (userScoreTreeSet){
            userScoreTreeSet.add(new UserScore(userId,score));
        }

        System.out.println(String.format("levelid:%d <-post userId:%d score:%d",levelId,userId,score));
    }

    //<levelid>/highscorelist, 15 results like userid=score,userid=score
    private String getHightScoreList(int levelId) {

        StringBuilder sb = new StringBuilder();

        if(scoreMap.containsKey(levelId)) {
            System.out.println(String.format("levelid:%d score count:%d",levelId,scoreMap.get(levelId).size()));
            scoreMap.get(levelId).stream().limit(15).forEach(userScore -> sb.append(userScore.toString()+","));

            sb.deleteCharAt(sb.length()-1);
        }

        return sb.toString();
    }







}