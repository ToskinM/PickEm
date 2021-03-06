package com.cse.osu.pickem;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game implements Parcelable {

    private String firstTeamName;
    private String secondTeamName;
    private String leagueID;
    @Exclude
    private Date mLockTime;
    private String gameID;

    @Exclude
    private List<Pick> picksToRemove = new ArrayList<>();

    private DatabaseReference picksReference;
    private DatabaseReference leagueMemberReference;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public Game(String firstTeamName, String secondTeamName, String leagueID) {
        this.firstTeamName = firstTeamName;
        this.secondTeamName = secondTeamName;
        this.leagueID = leagueID;
    }

    public void fastEndGame(final int teamAFinalScore, final int teamBFinalScore) {
        DatabaseReference leagueReference = FirebaseDatabase.getInstance().getReference("leagues");
        leagueReference = leagueReference.child(leagueID).child("leagueOwnerUID");
        leagueReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String leagueOwnerID = dataSnapshot.getValue(String.class);

                if (!auth.getUid().equals(leagueOwnerID)) {
                    Log.d("PickEm", "Not the owner!!");
                } else {
                    picksReference = FirebaseDatabase.getInstance().getReference("picks");
                    picksReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Pick tempPick = snapshot.getValue(Pick.class);
                                if (tempPick.getGameID().equals(gameID)) {
                                    calculatePoints(tempPick, teamAFinalScore, teamBFinalScore);
                                    DatabaseReference gamesReference = FirebaseDatabase.getInstance().getReference("games");
                                    gamesReference = gamesReference.child(gameID);
                                    gamesReference.removeValue();
                                    removePicks();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Removes all picks for this game
    protected void removePicks() {
        picksReference = FirebaseDatabase.getInstance().getReference("picks");
        picksReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Pick> picksToRemove = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pick tempPick = snapshot.getValue(Pick.class);
                    if (tempPick.getGameID().equals(gameID)) {
                        picksToRemove.add(tempPick);
                    }
                }

                for (Pick pick : picksToRemove) {
                    picksReference.child(pick.getPickID()).removeValue();
                }

                picksToRemove.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    protected void calculatePoints(final Pick pick, int actualScoreA, int actualScoreB) {
        boolean teamAWon = actualScoreA > actualScoreB;
        int actualMargin = Math.abs(actualScoreA - actualScoreB);
        int guessMargin = Math.abs(pick.getTeamAScore() - pick.getTeamBScore());
        int pointsWon = Math.abs(actualMargin - guessMargin);

        if ((teamAWon && pick.getTeamAScore() > pick.getTeamBScore()) ||
                (!teamAWon && pick.getTeamAScore() > pick.getTeamBScore())) {
            pointsWon = 300 - pointsWon;
        } else {
            pointsWon = 0;
        }

        final int finalNewPoints = pointsWon;
        final String leagueIDFinal = leagueID;
        leagueMemberReference = FirebaseDatabase.getInstance().getReference("leagueMembers");
        leagueMemberReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LeagueMemberPair pair = snapshot.getValue(LeagueMemberPair.class);
                    if (pair.getUID().equals(pick.getUserID()) && pair.getLeagueID().equals(leagueIDFinal)) {
                        String key = snapshot.getKey();
                        int newPoints = pair.getPoints() + finalNewPoints;
                        Map<String, Object> newMap = new HashMap<>();
                        pair.setPoints(newPoints);
                        LeagueMemberPair newPair = pair;
                        newMap.put(key, newPair);
                        snapshot.getRef().getParent().updateChildren(newMap);
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

    public Date getLockTime() {
        return mLockTime;
    }

    public boolean isPastLockTime() {
        Date currentDate = new Date();
        if (mLockTime == null) {
            Log.d("PickEm", "mLockTime is null my dude!");
        }

        return currentDate.after(mLockTime);
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
        dest.writeLong(mLockTime.getTime());
    }

    private Game(Parcel in) {
        this.firstTeamName = in.readString();
        this.secondTeamName = in.readString();
        this.leagueID = in.readString();
        this.gameID = in.readString();
        Date date = new Date();
        date.setTime(in.readLong());
        this.mLockTime = date;
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
