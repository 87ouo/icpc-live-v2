package org.icpclive.datapassing;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.events.TeamInfo;
import org.icpclive.webadmin.mainscreen.MainScreenData;
import org.icpclive.events.EventsLoader;

import java.lang.reflect.Type;
import java.util.Arrays;

public class TeamData extends CachedData {
    private static final Logger log = LogManager.getLogger(TeamData.class);

    public TeamData() {
    }

    @Override
    public TeamData initialize() {
        TeamData data = MainScreenData.getMainScreenData().teamData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.infoType = data.infoType;
        this.teamId = data.teamId;
        this.delay = data.delay;
        this.sleepTime = data.sleepTime;
        this.withStats = data.withStats;

        return this;
    }

    public void recache() {
        Data.cache.refresh(TeamData.class);
    }

    String currentStatus = "0\nfalse";
    String lastStatus = "0\nfalse";

    public void switchOverlaysOff() {
        if (MainScreenData.getMainScreenData().standingsData.isVisible) {
            MainScreenData.getMainScreenData().standingsData.hide();
        }
//        MainScreenData.getMainScreenData().advertisementData.hide();
//        MainScreenData.getMainScreenData().personData.hide();
        if (MainScreenData.getMainScreenData().statisticsData.isVisible()) {
            MainScreenData.getMainScreenData().statisticsData.hide();
        }
        if (MainScreenData.getMainScreenData().wordStatisticsData.isVisible) {
            MainScreenData.getMainScreenData().wordStatisticsData.hide();
        }
        if (MainScreenData.getMainScreenData().pollData.isVisible) {
            MainScreenData.getMainScreenData().pollData.hide();
        }
    }

    public String getOverlayError() {
        return "You should close team view first!";
    }

    public synchronized boolean automaticStart(int number,
                                               String type,
                                               boolean stats) {
        if (timestamp + sleepTime > System.currentTimeMillis() && isVisible) {
            return false;
        }
        TeamInfo[] currentStandings = EventsLoader.getInstance().getContestData().getStandings();
        teamsToShow = Arrays.copyOf(currentStandings, number);
        currentPosition = 0;
        isVisible = true;
        automaticType = type;
        automaticStats = stats;
        setInfo(automaticType, teamsToShow[0], automaticStats);
//        timestamp = System.currentTimeMillis() + MainScreenData.getProperties().sleepTime;
        isAutomatic = true;
        return true;
    }

    public synchronized void automaticStop() {
//        log.info("STOOOOOOOOOOOP IT!");
        hideInfo();
        isAutomatic = false;
    }

    private synchronized void setInfo(String type, TeamInfo teamInfo, boolean stats) {
        timestamp = System.currentTimeMillis();
        isVisible = true;
        infoType = type;
        currentTeam = teamInfo;
        teamId = teamInfo.getId();
        withStats = stats;

        lastStatus = currentStatus;
        currentStatus = timestamp + "\n" + isVisible + "\n" + infoType + "\n" + currentTeam.getName();

        log.debug(teamInfo.getName() + " " + teamId + " " + type);

        switchOverlaysOff();
        recache();
    }

    private synchronized void hideInfo() {
        timestamp = System.currentTimeMillis();
        isVisible = false;

        lastStatus = currentStatus;
        currentStatus = timestamp + "\n" + false;
        recache();
    }

    public synchronized String setInfoManual(boolean visible,
                                             String type,
                                             TeamInfo teamInfo,
                                             boolean withStats) {
        if (inAutomaticShow())
            return "Please, stop the automatic mode";
        if (teamInfo == null && visible) {
            return "Team is null";
        }
        if (visible) {
            if (isVisible) {
                if (teamInfo.getId() == teamId && infoType.equals(type)) {
                    return "This team and this type is currently shown";
                }
                if (timestamp + sleepTime > System.currentTimeMillis()) {
                    return "Please wait " + sleepTime / 1000 + " seconds first";
                }
            }
            setInfo(type, teamInfo, withStats);
        } else {
            hideInfo();
        }

        return null;
    }

    public void setSleepTime(int sleepTime) {
        MainScreenData.getProperties().sleepTime = sleepTime;
        this.sleepTime = sleepTime;
    }

    public synchronized boolean isVisible() {
        return isVisible;
    }

    public synchronized TeamInfo getTeam() {
        return currentTeam;
    }

    public synchronized String infoStatus() {
        return currentStatus + "\0" + lastStatus;
    }

    public synchronized int getTeamId() {
        return teamId;
    }

    private final String[] automaticStatuses = {"Now: ", "Next: ", "After: "};

    public synchronized String automaticStatus() {
        if (teamsToShow == null || !isAutomatic) {
            return "";
        }
        String result = "";
        for (int i = currentPosition; i < currentPosition + 3; i++) {
            result += (i != currentPosition ? "<br>" : "") +
                    (i < teamsToShow.length ? automaticStatuses[i - currentPosition] + teamsToShow[i].getName() : "");
        }
        return result;
    }

    public synchronized boolean inAutomaticShow() {
        return isAutomatic;
    }

    public synchronized void update() {
        if (!inAutomaticShow()) {
            return;
        }
        if (timestamp + MainScreenData.getProperties().automatedShowTime < System.currentTimeMillis()) {
            if (currentPosition + 1 < teamsToShow.length) {
                setInfo(automaticType, teamsToShow[++currentPosition], automaticStats);
            } else {
                if (timestamp + MainScreenData.getProperties().automatedShowTime +
                        sleepTime > System.currentTimeMillis()) {
                    hideInfo();
                    isAutomatic = false;
                }
            }
//            recache();
        }
    }

    ;
    public boolean isVisible;
    public String infoType = "";
    private TeamInfo currentTeam;
    private int teamId = -1;
    private boolean withStats;

    private boolean isAutomatic;
    private TeamInfo[] teamsToShow;
    private int currentPosition;
    private String automaticType;
    private boolean automaticStats;

    public int sleepTime;

    public static class TeamDataSerializer implements JsonSerializer<TeamData> {

        @Override
        public JsonElement serialize(TeamData teamData, Type type, JsonSerializationContext jsonSerializationContext) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("timestamp", teamData.timestamp);
            jsonObject.addProperty("isVisible", teamData.isVisible);
            jsonObject.addProperty("infoType", teamData.infoType);
            jsonObject.addProperty("teamId", teamData.teamId);
            jsonObject.addProperty("sleepTime", teamData.sleepTime);
            jsonObject.addProperty("withStats", teamData.withStats);

            return jsonObject;
        }
    }

    public static class TeamDataDeserializer implements JsonDeserializer<TeamData> {

        @Override
        public TeamData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            TeamData teamData = new TeamData();

            final JsonObject jsonObject = jsonElement.getAsJsonObject();

            teamData.timestamp = jsonObject.get("timestamp").getAsLong();
            teamData.isVisible = jsonObject.get("isVisible").getAsBoolean();
            teamData.infoType = jsonObject.get("infoType").getAsString();
            teamData.teamId = jsonObject.get("teamId").getAsInt();
            teamData.sleepTime = jsonObject.get("sleepTime").getAsInt();
            teamData.withStats = jsonObject.get("withStats").getAsBoolean();

            //log.info("Hello from TeamDataDeserializer!");

            return teamData;
        }
    }
}
