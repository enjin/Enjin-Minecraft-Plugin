package com.enjin.rpc.mappings.mappings.plugin;

import com.enjin.rpc.mappings.mappings.plugin.statistics.*;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class Stats {
    @Getter
    @SerializedName(value = "recent_purchases")
    private List<Object> recentPurchases;
    @Getter
    @SerializedName(value = "item_purchases")
    private List<Object> itemPurchases;
    @Getter
    @SerializedName(value = "top_voters_day")
    private List<TopVoter> topVotersDay;
    @Getter
    @SerializedName(value = "top_voters_week")
    private List<TopVoter> topVotersWeek;
    @Getter
    @SerializedName(value = "top_voters_month")
    private List<TopVoter> topVotersMonth;
    @Getter
    @SerializedName(value = "recent_voters")
    private List<RecentVoter> recentVoters;
    @Getter
    @SerializedName(value = "top_players")
    private List<TopPlayer> topPlayers;
    @Getter
    @SerializedName(value = "top_posters")
    private List<TopPoster> topPosters;
    @Getter
    @SerializedName(value = "top_forum_likes")
    private List<TopLiker> topForumLikes;
    @Getter
    @SerializedName(value = "latest_members")
    private List<LatestMember> latestMembers;
    @Getter
    @SerializedName(value = "top_points")
    private List<TopPoint> topPoints;
    @Getter
    @SerializedName(value = "top_points_day")
    private List<TopPoint> topPointsDay;
    @Getter
    @SerializedName(value = "top_points_week")
    private List<TopPoint> topPointsWeek;
    @Getter
    @SerializedName(value = "top_points_month")
    private List<TopPoint> topPointsMonth;
    @Getter
    @SerializedName(value = "top_donators_points")
    private List<Object> topDonatorsPoints;
    @Getter
    @SerializedName(value = "top_donators_points_day")
    private List<Object> topDonatorsPointsDay;
    @Getter
    @SerializedName(value = "top_donators_points_week")
    private List<Object> topDonatorsPointsWeek;
    @Getter
    @SerializedName(value = "top_donators_points_month")
    private List<Object> topDonatorsPointsMonth;
    @Getter
    @SerializedName(value = "top_donators_money")
    private List<Object> topDonatorsMoney;
    @Getter
    @SerializedName(value = "top_donators_money_day")
    private List<Object> topDonatorsMoneyDay;
    @Getter
    @SerializedName(value = "top_donators_money_week")
    private List<Object> topDonatorsMoneyWeek;
    @Getter
    @SerializedName(value = "top_donators_money_month")
    private List<Object> topDonatorsMoneyMonth;
}
