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
    private Boolean isAdmin;
    @Getter
    @SerializedName(value = "has_some_access")
    private Boolean hasSomeAccess;
    @Getter
    @SerializedName(value = "manage_all_tickets")
    private Boolean manageAllTickets;
    @Getter
    @SerializedName(value = "visit_form_tickets")
    private Boolean visitFormTickets;
    @Getter
    @SerializedName(value = "visit_views_tickets")
    private Boolean visitViewsTickets;
    @Getter
    @SerializedName(value = "visit_settings_tickets")
    private Boolean visitSettingsTickets;
    @Getter
    @SerializedName(value = "visit_statistics_tickets")
    private Boolean visitStatisticsTickets;
    @Getter
    @SerializedName(value = "visit_signatures_tickets")
    private Boolean visitSignaturesTickets;
    @Getter
    @SerializedName(value = "reply_tickets")
    private Boolean replyTickets;
    @Getter
    @SerializedName(value = "delete_tickets")
    private Boolean deleteTickets;
    @Getter
    @SerializedName(value = "status_tickets")
    private Boolean statusTickets;
    @Getter
    @SerializedName(value = "assignee_tickets")
    private Boolean assigneeTickets;
    @Getter
    @SerializedName(value = "priority_tickets")
    private Boolean priorityTickets;
    @Getter
    @SerializedName(value = "submit_tickets")
    private Boolean submitTickets;
    @Getter
    @SerializedName(value = "visit_index_tickets")
    private Boolean visitIndexTickets;
}
