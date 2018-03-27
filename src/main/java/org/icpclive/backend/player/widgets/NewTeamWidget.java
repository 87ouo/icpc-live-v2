package org.icpclive.backend.player.widgets;

import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.PlayerInImage;
import org.icpclive.backend.player.urls.TeamUrls;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.backend.player.widgets.stylesheets.QueueStylesheet;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.ProblemInfo;
import org.icpclive.events.RunInfo;
import org.icpclive.events.TeamInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @author: pashka
 */
public class NewTeamWidget extends Widget {

    private static double standardAspect = 16. / 9;

    List<TeamStatusView> views = new ArrayList<>();
    int currentView;
    private int timeToSwitch;

    public NewTeamWidget(int sleepTime) {
        this.sleepTime = sleepTime;
        views.add(emptyView);
    }

    public void updateImpl(Data data) {
        if (!data.teamData.isVisible) {
            setVisible(false);
            if (views.get(currentView) != emptyView) {
                views.get(currentView).timeToLive = 0;
            }
        } else {
            setVisible(true);
            int teamId = data.teamData.getTeamId();
            boolean ok;
            if (teamId >= 0) {
                TeamInfo team = Preparation.eventsLoader.getContestData().getParticipant(data.teamData.getTeamId());
                ok = false;
                for (int i = currentView; i < views.size(); i++) {
                    TeamStatusView view = views.get(i);
                    if (view.team == team) {
                        ok = true;
                        break;
                    }
                }
            } else {
                ok = true;
            }
            if (!ok) {
                TeamInfo team = Preparation.eventsLoader.getContestData().getParticipant(data.teamData.getTeamId());
                String infoType = data.teamData.infoType;
                addView(team, infoType);
            }
            timeToSwitch = data.teamData.sleepTime;
        }
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        super.paintImpl(g, width, height);
        for (TeamStatusView view : views) {
            view.paintImpl(g, width, height);
        }
        if (views.get(currentView).timeToLive <= 0) {
            views.get(currentView).setVisible(false);
        }
        if (views.get(currentView).visibilityState <= 0) {
            System.err.println("Switch view");
            views.get(currentView).mainVideo.stop();
            currentView++;
            if (currentView == views.size()) {
                views.add(emptyView);
                emptyView.timeToLive = Integer.MAX_VALUE;
            }
            views.get(currentView).setVisible(true);
        }
    }

    public void addView(TeamInfo team, String infoType) {
        System.err.println("Add view " + team + " " + infoType);
        if (views.size() > 0) {
            views.get(currentView).timeToLive = timeToSwitch; // FIX!!!
        }
        views.add(new TeamStatusView(team, infoType, sleepTime));
    }

    public CachedData getCorrespondingData(Data data) {
        return data.teamData;
    }

    TeamStatusView emptyView = new TeamStatusView(null, null, sleepTime);

    class TeamStatusView extends Widget {

        private static final int BIG_HEIGHT = 1295 * 9 / 16;//780;
        private static final int BIG_X_RIGHT = 1883;//493;
        private static final int BIG_Y = 52;

        private static final int TEAM_PANE_X = 30;
        private static final int TEAM_PANE_Y = 52;
        private static final int TEAM_PANE_HEIGHT = 41;

        private final int nameWidth;
        private final int rankWidth;
        private final int solvedWidth;
        private final int problemWidth;
        private final int statusWidth;
        private final int timeWidth;

        private int width;
        private int height;

        private long timeToChange;

        private String infoType;

        private final PlayerInImage mainVideo;
        private final TeamInfo team;
        int timeToLive = Integer.MAX_VALUE;

        public TeamStatusView(TeamInfo team, String infoType, int sleepTime) {
            this.team = team;
            this.infoType = infoType;
            width = (int) (standardAspect * BIG_HEIGHT);
            height = BIG_HEIGHT;

            nameWidth = (int) Math.round(NAME_WIDTH * TEAM_PANE_HEIGHT);
            rankWidth = (int) Math.round(RANK_WIDTH * TEAM_PANE_HEIGHT);
            solvedWidth = (int) Math.round(PROBLEM_WIDTH * TEAM_PANE_HEIGHT);
            problemWidth = (int) Math.round(PROBLEM_WIDTH * TEAM_PANE_HEIGHT);
            statusWidth = (int) Math.round(STATUS_WIDTH * TEAM_PANE_HEIGHT);
            timeWidth = (int) Math.round(TIME_WIDTH * TEAM_PANE_HEIGHT);

            System.err.println("Load video: " + TeamUrls.getUrl(team, infoType));
            mainVideo = new PlayerInImage(width, height, null, TeamUrls.getUrl(team, infoType));
            setFont(Font.decode(MAIN_FONT + " " + (int) (TEAM_PANE_HEIGHT * 0.7)));
        }

        @Override
        protected void paintImpl(AbstractGraphics g, int width, int height) {
            super.paintImpl(g, width, height);
            timeToLive -= dt;
            if (team == null) {
                return;
            }
            if (visibilityState == 0) {
                return;
            }
            drawStatus();
            drawVideos();
        }

        private void drawVideos() {
            graphics.drawImage(mainVideo.getImage(), BIG_X_RIGHT - width, BIG_Y, width, height, visibilityState * .9);
        }

        private void drawStatus() {
            String name = team.getShortName();

            PlateStyle teamColor = QueueStylesheet.name;

            int x = TEAM_PANE_X;
            int y = TEAM_PANE_Y;

            PlateStyle color = getTeamRankColor(team);
            applyStyle(color);
            drawRectangleWithText("" + Math.max(team.getRank(), 1), x, y,
                    rankWidth, TEAM_PANE_HEIGHT, PlateStyle.Alignment.CENTER, false, false);

            x += rankWidth;

            applyStyle(teamColor);
            drawRectangleWithText(name, x, y,
                    nameWidth + solvedWidth, TEAM_PANE_HEIGHT, PlateStyle.Alignment.LEFT);

            x += nameWidth + solvedWidth;

            drawRectangleWithText("" + team.getSolvedProblemsNumber(), x, y,
                    problemWidth, TEAM_PANE_HEIGHT, PlateStyle.Alignment.CENTER);

            x += problemWidth;

            drawRectangleWithText("" + team.getPenalty(), x, y,
                    statusWidth, TEAM_PANE_HEIGHT, PlateStyle.Alignment.CENTER);


            List<RunInfo> lastRuns = new ArrayList<>();
            for (List<RunInfo> runs : team.getRuns()) {
                RunInfo lastRun = null;
                for (RunInfo run : runs) {
                    lastRun = run;
                    if (lastRun.isAccepted()) {
                        break;
                    }
                }
                if (lastRun != null) {
                    lastRuns.add(lastRun);
                }
            }

            Collections.sort(lastRuns, (o1, o2) -> Long.compare(o1.getTime(), o2.getTime()));


            for (RunInfo run : lastRuns) {
                y += TEAM_PANE_HEIGHT;
                x = TEAM_PANE_X + rankWidth + nameWidth + solvedWidth - timeWidth;
                applyStyle(teamColor);
                drawRectangleWithText("" + format(run.getTime()), x, y,
                        timeWidth, TEAM_PANE_HEIGHT, PlateStyle.Alignment.CENTER);
                x += timeWidth;
                drawProblemPane(run.getProblem(), x, y, problemWidth, TEAM_PANE_HEIGHT);
                x += problemWidth;

                PlateStyle resultColor = QueueStylesheet.udProblem;

                boolean inProgress = false;
                int progressWidth = 0;

                if (run.isJudged()) {
                    if (run.isAccepted()) {
                        resultColor = QueueStylesheet.acProblem;
                    } else {
                        resultColor = QueueStylesheet.waProblem;
                    }
                } else {
                    inProgress = true;
                    progressWidth = (int) Math.round(statusWidth * run.getPercentage());
                }

                String result = run.getResult();
                if (run.getTime() > ContestInfo.FREEZE_TIME) {
                    result = "?";
                    resultColor = QueueStylesheet.frozenProblem;
                    inProgress = false;
                }

                applyStyle(resultColor);
                drawRectangleWithText(result, x, y, statusWidth,
                        TEAM_PANE_HEIGHT, PlateStyle.Alignment.CENTER);

                if (inProgress) {
                    setBackgroundColor(QueueStylesheet.udTests);
                    drawRectangle(x, y, progressWidth, TEAM_PANE_HEIGHT);
                }
//                if (plate.runInfo == info.firstSolvedRun()[runInfo.getProblemId()]) {
//                    drawStar(x + statusWidth - STAR_SIZE, y + 2 * STAR_SIZE, STAR_SIZE);
//                }
            }


//        drawProblemPane(problem, x, y, problemWidth, plateHeight, blinking);
//
//        x += problemWidth + spaceX;

        }

        private String format(long time) {
            int s = (int) (time / 1000);
            int m = s / 60;
            s %= 60;
            int h = m / 60;
            m %= 60;
            return String.format("%d:%02d", h, m);
        }

        @Override
        protected CachedData getCorrespondingData(Data data) {
            return null;
        }
    }
}
