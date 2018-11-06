package com.cse.osu.pickem;

public class LeagueMemberPair {
    private String UID;
    private String LeagueID;
    private int Points;

    public LeagueMemberPair(String UIDIn, String leagueIdIn, int points) {
        UID = UIDIn;
        LeagueID = leagueIdIn;
        Points = points;
    }

    public LeagueMemberPair(String UIDIn, String leagueIDin) {
        UID = UIDIn;
        LeagueID = leagueIDin;
        Points = 0;
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

    public int getPoints() { return Points; }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setLeagueID(String leagueID) {
        LeagueID = leagueID;
    }

    public void setPoints(int newPoints) { Points = newPoints; }


}
