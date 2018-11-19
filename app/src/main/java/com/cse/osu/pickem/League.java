package com.cse.osu.pickem;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import java.util.Date;

public class League implements Parcelable {

    private String leagueName;
    private String leagueID;
    private String leagueOwnerUID;

    public League(String leagueName, String leagueID, String leagueOwnerUID) {
        this.leagueName = leagueName;
        this.leagueID = leagueID;
        this.leagueOwnerUID = leagueOwnerUID;
    }

    public League() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof League)) {
            return false;
        }
        League compareLeague = (League)o;
        if (this.leagueID != compareLeague.getLeagueID() || this.leagueName != compareLeague.getLeagueName() || this.leagueOwnerUID != compareLeague.getLeagueOwnerUID()) {
            return false;
        }
        return true;
    }

    // Setters
    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    public void setLeagueID(String leagueID) {
        this.leagueID = leagueID;
    }

    public void setLeagueOwnerUID(String leagueOwnerUID) {
        this.leagueOwnerUID = leagueOwnerUID;
    }

    // Getters
    public String getLeagueName() {
        return leagueName;
    }

    public String getLeagueID() {
        return leagueID;
    }

    public String getLeagueOwnerUID() {
        return leagueOwnerUID;
    }

    // Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(leagueName);
        dest.writeString(leagueID);
        dest.writeString(leagueOwnerUID);
    }

    // This is used to regenerate the League. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<League> CREATOR = new Parcelable.Creator<League>() {
        public League createFromParcel(Parcel in) {
            return new League(in);
        }

        public League[] newArray(int size) {
            return new League[size];
        }
    };

    // Constructor that takes a Parcel and gives you a League populated with it's values
    private League(Parcel in) {
        this.leagueName = in.readString();
        this.leagueID = in.readString();
        this.leagueOwnerUID = in.readString();
    }

    // Removes user with the indicated Firebase UID from the league
    public static void removeMember(final String leagueID, final String userID){
        // Get database references
        DatabaseReference leagueMemberDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");

        // Find and delete users pair to league
        leagueMemberDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LeagueMemberPair tempPair = snapshot.getValue(LeagueMemberPair.class);
                    if (tempPair.getUID().equals(userID) && tempPair.getLeagueID().equals(leagueID)) {
                        snapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void deleteLeagueGames(final String leagueID) {
        final DatabaseReference gamesReference = FirebaseDatabase.getInstance().getReference("games");
        gamesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Game> gamesToRemove = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Game tempGame = snapshot.getValue(Game.class);
                    if (tempGame.getLeagueID().equals(leagueID)) {
                        gamesToRemove.add(tempGame);
                    }
                }

                for (Game game : gamesToRemove) {
                    gamesReference.child(game.getGameID()).removeValue();
                }

                gamesToRemove.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void addMember(final String leagueID, final String userID){
        // Get database references
        DatabaseReference leagueMemberDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");
        LeagueFetcher leagueFetcher = LeagueFetcher.get();
        LeagueMembersFetcher leagueMembersFetcher = LeagueMembersFetcher.get();

        LeagueMemberPair pairToAdd = new LeagueMemberPair(userID, leagueID);
        boolean okToAdd = false;

        //Ensure league exists
        for (League league : leagueFetcher.getAllLeagues())
            if (league.getLeagueID().equals(leagueID))
                okToAdd = true;

        //Ensure user isn't already a member of the league
        for (LeagueMemberPair testPair : leagueMembersFetcher.getMembersOfLeague(leagueID))
            if (testPair.equals(pairToAdd))
                okToAdd = false;

        // Passes all tests, add new league pair
        if (okToAdd)
            leagueMemberDatabaseReference.push().setValue(pairToAdd);
    }

    public void addToDatabase(){
        // Add the league
        FirebaseDatabase.getInstance().getReference("leagues").child(leagueID).setValue(this);
        // Add owner to league
        FirebaseDatabase.getInstance().getReference("leagueMembers").push().setValue(new LeagueMemberPair(leagueOwnerUID, leagueID));
    }

    public void addGame(String teamAName, String teamBName, String daysText){
        // Create Game object
        Game newGame = new Game(teamAName, teamBName, leagueID);

        // Setup locktime
        Date now = new Date();
        Date lockTime = new Date();
        int days = Integer.parseInt(daysText);
        lockTime.setTime(now.getTime() + 86400000L * (long)days);
        newGame.setLockTime(lockTime);

        // Add game to database
        DatabaseReference tempReference = FirebaseDatabase.getInstance().getReference("games").push();
        newGame.setGameID(tempReference.getKey());
        tempReference.setValue(newGame);
    }
}
