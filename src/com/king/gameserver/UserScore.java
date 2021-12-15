package com.king.gameserver;

import java.io.Serializable;

public class UserScore implements Comparable<UserScore>, Serializable {

    private Integer userId;
    private Integer score;

    public UserScore(int userId, int score) {
        this.userId = userId;
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserScore userScore = (UserScore) o;

        if (userId != userScore.userId) return false;
        return score == userScore.score;
    }

    @Override
    public int hashCode() {
        int result = userId;
        result = 31 * result + score;
        return result;
    }

    @Override
    public int compareTo(UserScore o) {
        if (userId == o.userId) {
            if(score >= o.score){
                System.out.println("same came userid: "+ userId + " score: "+ score);
                return 0;
            } else {
                System.out.println("bigger came userid: "+ userId + " old score: "+ score+ " new score: "+ o.score);
                return 1;
            }
        } else {
            return this.score.compareTo(o.score);
        }
    }

    @Override
    public String toString() {
        return userId + "=" + score;
    }
}
