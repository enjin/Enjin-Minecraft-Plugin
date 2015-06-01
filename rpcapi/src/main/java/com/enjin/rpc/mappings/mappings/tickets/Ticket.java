package com.enjin.rpc.mappings.mappings.tickets;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class Ticket {
    @Getter
    private int id;
    @Getter
    private String code;
    @Getter
    @SerializedName(value = "site_id")
    private int siteId;
    @Getter
    @SerializedName(value = "preset_id")
    private int presetId;
    @Getter
    private String subject;
    @Getter
    private long created;
    @Getter
    private TicketStatus status;
    @Getter
    private String assignee;
    @Getter
    private long updated;
    @Getter
    private int requester;
    @Getter
    private int priority;
    @Getter
    @SerializedName(value = "extra_questions")
    private List<ExtraQuestion> extraQuestions;
    @Getter
    @SerializedName(value = "status_change")
    private long statusChange;
    @Getter
    private String email;
    @Getter
    private List<TicketViewer> viewers;
    @Getter
    @SerializedName(value = "site_name")
    private String siteName;
    @Getter
    @SerializedName(value = "no_reply_closed")
    private boolean noReplyClosed;
    @Getter
    @SerializedName(value = "reply_frequency")
    private int replyFrequency;
    @Getter
    @SerializedName(value = "disable_email_notifications")
    private boolean disableEmailNotifications;
    @Getter
    @SerializedName(value = "disable_pms")
    private boolean disablePms;
    @Getter
    @SerializedName(value = "email_user_create")
    private String emailUserCreate;
    @Getter
    @SerializedName(value = "email_user_reply")
    private String emailUserReply;
    @Getter
    @SerializedName(value = "email_user_assigned")
    private String emailUserAssigned;
    @Getter
    @SerializedName(value = "pm_user_create")
    private String pmUserCreate;
    @Getter
    @SerializedName(value = "pm_user_reply")
    private String pmUserReply;
    @Getter
    @SerializedName(value = "pm_agent_assigned")
    private String pmAgentAssigned;
    @Getter
    @SerializedName(value = "users_change_priority")
    private String usersChangePriority;
    @Getter
    @SerializedName(value = "feedback_surveys")
    private int feedbackSurveys;
    @Getter
    @SerializedName(value = "ticket_priority")
    private String ticketPriority;
}
