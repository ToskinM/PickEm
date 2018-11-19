package com.cse.osu.pickem;

import android.os.Parcel;
import android.os.Parcelable;

public class Pick implements Parcelable {

    private String UserID;
    private String GameID;

    public String getPickID() {
        return PickID;
    }

    public void setPickID(String pickID) {
        PickID = pickID;
    }

    private String PickID;
    private int TeamAScore;

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getGameID() {
        return GameID;
    }

    public void setGameID(String gameID) {
        GameID = gameID;
    }

    public int getTeamAScore() {
        return TeamAScore;
    }

    public void setTeamAScore(int teamAScore) {
        TeamAScore = teamAScore;
    }

    public int getTeamBScore() {
        return TeamBScore;
    }

    public void setTeamBScore(int teamBScore) {
        TeamBScore = teamBScore;
    }

    private int TeamBScore;

    public Pick(String userID, String gameID, int teamAScore, int teamBScore) {
        UserID = userID;
        GameID = gameID;
        TeamAScore = teamAScore;
        TeamBScore = teamBScore;
    }

    public Pick(Parcel in) {
        UserID = in.readString();
        GameID = in.readString();
        TeamAScore = in.readInt();
        TeamBScore = in.readInt();
    }

    public Pick() {

    }

    public void changePick(int teamAScore, int teamBScore) {
        TeamAScore = teamAScore;
        TeamBScore = teamBScore;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(UserID);
        dest.writeString(GameID);
        dest.writeInt(TeamAScore);
        dest.writeInt(TeamBScore);
    }

    public static final Parcelable.Creator<Pick> CREATOR = new Parcelable.Creator<Pick>() {
        public Pick createFromParcel(Parcel in) {
            return new Pick(in);
        }

        public Pick[] newArray(int size) {
            return new Pick[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Pick)) {
            return false;
        }

        Pick testPick = (Pick) o;

        if (UserID.equals(testPick.UserID) && GameID.equals(testPick.GameID) && TeamAScore == testPick.TeamAScore && TeamBScore == testPick.TeamBScore) {
            return true;
        }

        return false;
    }
}
