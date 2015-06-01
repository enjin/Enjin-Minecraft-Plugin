package com.enjin.rpc.mappings.mappings.tickets;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class PlayerAccess {
    @Getter
    @SerializedName(value = "is_admin")
    private boolean isAdmin;
    @Getter
    @SerializedName(value = "has_some_access")
    private boolean hasSomeAccess;
    @Getter
    @SerializedName(value = "manage_all_tickets")
    private boolean manageAllTickets;
    @Getter
    @SerializedName(value = "visit_form_tickets")
    private boolean visitFormTickets;
    @Getter
    @SerializedName(value = "visit_views_tickets")
    private boolean visitViewsTickets;
    @Getter
    @SerializedName(value = "visit_settings_tickets")
    private boolean visitSettingsTickets;
    @Getter
    @SerializedName(value = "visit_statistics_tickets")
    private boolean visitStatisticsTickets;
    @Getter
    @SerializedName(value = "visit_signatures_tickets")
    private boolean visitSignaturesTickets;
    @Getter
    @SerializedName(value = "reply_tickets")
    private boolean replyTickets;
    @Getter
    @SerializedName(value = "delete_tickets")
    private boolean deleteTickets;
    @Getter
    @SerializedName(value = "status_tickets")
    private boolean statusTickets;
    @Getter
    @SerializedName(value = "assignee_tickets")
    private boolean assigneeTickets;
    @Getter
    @SerializedName(value = "priority_tickets")
    private boolean priorityTickets;
    @Getter
    @SerializedName(value = "submit_tickets")
    private boolean submitTickets;
    @Getter
    @SerializedName(value = "visit_index_tickets")
    private boolean visitIndexTickets;
}
