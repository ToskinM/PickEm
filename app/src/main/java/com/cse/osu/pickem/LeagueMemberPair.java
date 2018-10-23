package com.cse.osu.pickem;

public class LeagueMemberPair {
    private String UID;
    private String LeagueID;

    public LeagueMemberPair(String UIDIn, String leagueIdIn) {
        UID = UIDIn;
        LeagueID = leagueIdIn;
    }

    public LeagueMemberPair() {

    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof LeagueMemberPair)) {
            return false;
        }

        LeagueMemberPair compareLeagueMemberPair = (LeagueMemberPair)o;

        if (!compareLeagueMemberPair.LeagueID.equals(this.LeagueID) || !compareLeagueMemberPair.UID.equals(this.UID)) {
            return false;
        }

        return true;
    }

    public String getUID() {
        return UID;
    }

    public String getLeagueID() {
        return LeagueID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setLeagueID(String leagueID) {
        LeagueID = leagueID;
    }
}
