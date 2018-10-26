package com.cse.osu.pickem;

public class Game {

    private String firstTeamName;
    private String secondTeamName;
    private boolean isLocked;

    public Game(String firstTeamName, String secondTeamName, boolean isLocked) {
        this.firstTeamName = firstTeamName;
        this.secondTeamName = secondTeamName;
        this.isLocked = isLocked;
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

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }


}
