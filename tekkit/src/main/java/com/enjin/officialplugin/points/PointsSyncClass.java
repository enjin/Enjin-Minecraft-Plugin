package com.enjin.officialplugin.points;

import com.enjin.officialplugin.points.PointsAPI.Type;

class PointsSyncClass implements Runnable {

    String playername;
    int points;
    PointsAPI.Type type;

    public PointsSyncClass(String playername, int points, Type type) {
        this.playername = playername;
        this.points = points;
        this.type = type;
    }

    @Override
    public synchronized void run() {
        try {
            PointsAPI.modifyPointsToPlayer(playername, points, type);
        } catch (NumberFormatException e) {
        } catch (PlayerDoesNotExistException e) {
        } catch (ErrorConnectingToEnjinException e) {
        }
    }

}
