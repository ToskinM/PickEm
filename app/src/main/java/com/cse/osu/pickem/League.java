package com.cse.osu.pickem;

import android.os.Parcel;
import android.os.Parcelable;

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
}
