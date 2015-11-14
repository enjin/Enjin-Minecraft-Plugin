package com.enjin.bukkit.statsigns;

import com.enjin.rpc.mappings.mappings.plugin.Stats;
import com.enjin.rpc.mappings.mappings.plugin.statistics.*;
import org.bukkit.block.Sign;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StatSignProcessor {
    private static DecimalFormat decimalFormat = new DecimalFormat("#.00");
    private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("h:mm a z");

    public static void setPurchaseSign(Sign sign, SignData data, Stats stats) {
        int index = data.getIndex();
        if (index < 1 || index > 10) {
            return;
        }

        if (data.getSubType() != null && data.getItemId() != null) {
            List<ItemPurchase> purchases = stats.getItemPurchases().get(data.getItemId());
            String[] lines = new String[4];
            lines[0] = "Donor " + index;

            if (purchases != null && purchases.size() >= index) {
                ItemPurchase purchase = purchases.get(index - 1);
                lines[1] = purchase.getName();
                lines[3] = "ID: " + data.getItemId();
            }

            for (int i = 0; i < 4; i++) {
                String line = lines[i];
                sign.setLine(i, line);
            }
        } else {
            List<RecentPurchase> purchases = stats.getRecentPurchases();
            String[] lines = new String[4];
            lines[0] = "Latest Donor " + index;

            if (purchases.size() >= index) {
                RecentPurchase purchase = purchases.get(index - 1);
                lines[1] = purchase.getName();
                lines[3] = decimalFormat.format(purchase.getPrice());
            }

            for (int i = 0; i < 4; i++) {
                String line = lines[i];
                sign.setLine(i, line);
            }
        }
    }

    public static void setTopVoterSign(Sign sign, SignData data, Stats stats) {
        int index = data.getIndex();
        if (index < 1 || index > 10) {
            return;
        }

        String period = "Monthly";
        List<TopVoter> voters = stats.getTopVotersMonth();

        if (data.getSubType() != null) {
            if (data.getSubType() == SignType.SubType.WEEK) {
                period = "Weekly";
                voters = stats.getTopVotersWeek();
            } else if (data.getSubType() == SignType.SubType.DAY) {
                period = "Daily";
                voters = stats.getTopVotersDay();
            }
        }

        String[] lines = new String[4];
        lines[0] = "Top " + period;
        lines[1] = "Voter " + index;

        if (voters.size() >= index) {
            TopVoter voter = voters.get(index - 1);
            lines[2] = voter.getName();
            lines[3] = voter.getCount() + " Votes";
        }

        for (int i = 0; i < 4; i++) {
            String line = lines[i];
            sign.setLine(i, line);
        }
    }

    public static void setVoterSign(Sign sign, SignData data, Stats stats) {
        int index = data.getIndex();
        if (index < 1 || index > 10) {
            return;
        }

        List<RecentVoter> voters = stats.getRecentVoters();
        String[] lines = new String[4];
        lines[0] = "Latest Voter " + index;

        if (voters.size() >= index) {
            RecentVoter voter = voters.get(index - 1);
            Date date = new Date(voter.getTime() * 1000);
            lines[1] = voter.getName();
            lines[2] = dateFormat.format(date);
            lines[3] = timeFormat.format(date);
        }

        for (int i = 0; i < 4; i++) {
            String line = lines[i];
            sign.setLine(i, line);
        }
    }

    public static void setTopPlayerSign(Sign sign, SignData data, Stats stats) {
        int index = data.getIndex();
        if (index < 1 || index > 10) {
            return;
        }

        List<TopPlayer> players = stats.getTopPlayers();
        String[] lines = new String[4];
        lines[0] = "Top Player " + index;

        if (players.size() >= index) {
            TopPlayer player = players.get(index - 1);
            lines[1] = player.getName();
            lines[3] = decimalFormat.format(player.getHours()) + " Hours";
        }

        for (int i = 0; i < 4; i++) {
            String line = lines[i];
            sign.setLine(i, line);
        }
    }

    public static void setTopPosterSign(Sign sign, SignData data, Stats stats) {
        int index = data.getIndex();
        if (index < 1 || index > 10) {
            return;
        }

        List<TopPoster> players = stats.getTopPosters();
        String[] lines = new String[4];
        lines[0] = "Top Poster " + index;

        if (players.size() >= index) {
            TopPoster player = players.get(index - 1);
            lines[1] = player.getName();
            lines[3] = player.getPosts() + " Posts";
        }

        for (int i = 0; i < 4; i++) {
            String line = lines[i];
            sign.setLine(i, line);
        }
    }

    public static void setTopLikesSign(Sign sign, SignData data, Stats stats) {
        int index = data.getIndex();
        if (index < 1 || index > 10) {
            return;
        }

        List<TopLiker> players = stats.getTopForumLikes();
        String[] lines = new String[4];
        lines[0] = "Top Likes " + index;

        if (players.size() >= index) {
            TopLiker player = players.get(index - 1);
            lines[1] = player.getName();
            lines[3] = player.getLikes() + " Likes";
        }

        for (int i = 0; i < 4; i++) {
            String line = lines[i];
            sign.setLine(i, line);
        }
    }

    public static void setNewMemberSign(Sign sign, SignData data, Stats stats) {
        int index = data.getIndex();
        if (index < 1 || index > 10) {
            return;
        }

        List<LatestMember> members = stats.getLatestMembers();
        String[] lines = new String[4];
        lines[0] = "New Member " + index;

        if (members.size() >= index) {
            LatestMember member = members.get(index - 1);
            Date date = new Date(member.getDateJoined() * 1000);
            lines[1] = member.getName();
            lines[2] = dateFormat.format(date);
            lines[3] = timeFormat.format(date);
        }

        for (int i = 0; i < 4; i++) {
            String line = lines[i];
            sign.setLine(i, line);
        }
    }

    public static void setTopPointsSign(Sign sign, SignData data, Stats stats) {
        int index = data.getIndex();
        if (index < 1 || index > 10) {
            return;
        }

        List<TopPoint> players = stats.getTopPoints();
        String[] lines = new String[4];
        lines[0] = "Top Points " + index;

        if (players.size() >= index) {
            TopPoint player = players.get(index - 1);
            lines[1] = player.getName();
            lines[3] = player.getPoints() + " Points";
        }

        for (int i = 0; i < 4; i++) {
            String line = lines[i];
            sign.setLine(i, line);
        }
    }

    public static void setPointsSpentSign(Sign sign, SignData data, Stats stats) {
        int index = data.getIndex();
        if (index < 1 || index > 10) {
            return;
        }

        String period = "Total";
        List<TopDonator> donors = stats.getTopDonatorsPoints();

        if (data.getSubType() != null) {
            if (data.getSubType() == SignType.SubType.MONTH) {
                period = "Monthly";
                donors = stats.getTopDonatorsPointsMonth();
            } else if (data.getSubType() == SignType.SubType.WEEK) {
                period = "Weekly";
                donors = stats.getTopDonatorsPointsWeek();
            } else if (data.getSubType() == SignType.SubType.DAY) {
                period = "Daily";
                donors = stats.getTopDonatorsPointsDay();
            }
        }

        String[] lines = new String[4];
        lines[0] = "Top " + period;
        lines[1] = "Points Donor " + index;

        if (donors.size() >= index) {
            TopDonator donor = donors.get(index - 1);
            lines[2] = donor.getName();
            lines[3] = donor.getPoints() + " Points";
        }

        for (int i = 0; i < 4; i++) {
            String line = lines[i];
            sign.setLine(i, line);
        }
    }

    public static void setMoneySpentSign(Sign sign, SignData data, Stats stats) {
        int index = data.getIndex();
        if (index < 1 || index > 10) {
            return;
        }

        String period = "Total";
        List<TopDonator> donors = stats.getTopDonatorsMoney();

        if (data.getSubType() != null) {
            if (data.getSubType() == SignType.SubType.MONTH) {
                period = "Monthly";
                donors = stats.getTopDonatorsMoneyMonth();
            } else if (data.getSubType() == SignType.SubType.WEEK) {
                period = "Weekly";
                donors = stats.getTopDonatorsMoneyWeek();
            } else if (data.getSubType() == SignType.SubType.DAY) {
                period = "Daily";
                donors = stats.getTopDonatorsMoneyDay();
            }
        }

        String[] lines = new String[4];
        lines[0] = "Top " + period;
        lines[1] = "Money Donor " + index;

        if (donors.size() >= index) {
            TopDonator donor = donors.get(index - 1);
            lines[2] = donor.getName();
            lines[3] = decimalFormat.format(donor.getPrice());
        }

        for (int i = 0; i < 4; i++) {
            String line = lines[i];
            sign.setLine(i, line);
        }
    }
}
