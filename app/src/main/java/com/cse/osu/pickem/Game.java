package com.cse.osu.pickem;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Game implements Parcelable {

    private String firstTeamName;
    private String secondTeamName;
    private String leagueID;
    private boolean isLocked;
    private Date mLockTime;
    private String gameID;

    private DatabaseReference picksReference;
    private DatabaseReference leagueMemberReference;

    private List<LeagueMemberPair> loadedMembers;
    private List<Pick> loadedPicks;

    public Game(String firstTeamName, String secondTeamName, String leagueID) {
        this.firstTeamName = firstTeamName;
        this.secondTeamName = secondTeamName;
        this.leagueID = leagueID;

        loadedMembers = new ArrayList<>();
        loadedPicks = new ArrayList<>();
    }


    public void endGame(final String gameID, final int teamAFinalScore, final int teamBFinalScore) {
        picksReference = FirebaseDatabase.getInstance().getReference("picks");
        leagueMemberReference = FirebaseDatabase.getInstance().getReference("leagueMembers");

        picksReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pick tempPick = snapshot.getValue(Pick.class);
                    if (tempPick.getGameID().equals(gameID)) {
                        loadedPicks.add(tempPick);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
        dest.writeString(gameID);
        //dest.writeBooleanArray(new boolean[] {isLocked});
    }

    private Game(Parcel in) {
        this.firstTeamName = in.readString();
        this.secondTeamName = in.readString();
        this.leagueID = in.readString();
        this.gameID = in.readString();
        //boolean[] tempArray = new boolean[1];
        //in.readBooleanArray(tempArray);
        //this.isLocked = tempArray[0];
    }

    public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>() {
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }
}
