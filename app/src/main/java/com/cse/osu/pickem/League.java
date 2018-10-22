package com.cse.osu.pickem;

public class League {

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

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    public void setLeagueID(String leagueID) {
        this.leagueID = leagueID;
    }

    public void setLeagueOwnerUID(String leagueOwnerUID) {
        this.leagueOwnerUID = leagueOwnerUID;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public String getLeagueID() {
        return leagueID;
    }

    public String getLeagueOwnerUID() {
        return leagueOwnerUID;
    }
}
