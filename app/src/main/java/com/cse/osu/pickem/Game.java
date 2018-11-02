package com.cse.osu.pickem;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Game implements Parcelable {

    private String firstTeamName;
    private String secondTeamName;
    private String leagueID;
    private boolean isLocked;
    private Date mLockTime;

    public Game(String firstTeamName, String secondTeamName, String leagueID) {
        this.firstTeamName = firstTeamName;
        this.secondTeamName = secondTeamName;
        this.leagueID = leagueID;
    }

    public void setLockTime(Date lockTime) {
        mLockTime = lockTime;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void checkTime() {
        if (mLockTime.before(new Date())) {
            isLocked = true;
        }
    }

    public Game() {

    }

    public String getFirstTeamName() {
        return firstTeamName;
    }

    public void setFirstTeamName(String firstTeamName) {
        this.firstTeamName = firstTeamName;
    }

    public String getSecondTeamName() {
        return secondTeamName;
    }

    public void setSecondTeamName(String secondTeamName) {
        this.secondTeamName = secondTeamName;
    }

    //public boolean isLocked() {
    //    return isLocked;
    //}

    //public void setLocked(boolean locked) {
    //    isLocked = locked;
    //}

    public String getLeagueID() {
        return this.leagueID;
    }

    public void setLeagueID(String leagueID) {
        this.leagueID = leagueID;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstTeamName);
        dest.writeString(secondTeamName);
        dest.writeString(leagueID);
        dest.writeBooleanArray(new boolean[] {isLocked});
    }

    private Game(Parcel in) {
        this.firstTeamName = in.readString();
        this.secondTeamName = in.readString();
        this.leagueID = in.readString();
        boolean[] temp = new boolean[1];
        in.readBooleanArray(temp);
        this.isLocked = temp[0];
    }

    public void endGame() {
        isLocked = true;
    }

    public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>() {
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        public Game[] newArray(int size) {
            return new Game[size];
        }
    };
}
