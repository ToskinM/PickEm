package com.cse.osu.pickem;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    public static void removeMemberFromLeague(final String leagueID, final String userID){
        // Get database references
        DatabaseReference leagueMemberDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");
        // League Members listener
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
}
