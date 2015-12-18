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
    private Integer id;
    @Getter
    private String code;
    @Getter
    @SerializedName(value = "site_id")
    private Integer siteId;
    @Getter
    @SerializedName(value = "preset_id")
    private Integer presetId;
    @Getter
    private String subject;
    @Getter
    private Long created;
    @Getter
    private TicketStatus status;
    @Getter
    private String assignee;
    @Getter
    private Long updated;
    @Getter
    private Integer requester;
    @Getter
    private Integer priority;
    @Getter
    @SerializedName(value = "extra_questions")
    private List<ExtraQuestion> extraQuestions;
    @Getter
    @SerializedName(value = "status_change")
    private Long statusChange;
    @Getter
    private String email;
    @Getter
    private Boolean viewers;
    @Getter
    @SerializedName(value = "site_name")
    private String siteName;
    @Getter
    @SerializedName(value = "no_reply_closed")
    private Boolean noReplyClosed;
    @Getter
    @SerializedName(value = "reply_frequency")
    private Integer replyFrequency;
    @Getter
    @SerializedName(value = "disable_email_notifications")
    private Boolean disableEmailNotifications;
    @Getter
    @SerializedName(value = "disable_pms")
    private Boolean disablePms;
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
    private Integer feedbackSurveys;
    @Getter
    @SerializedName(value = "ticket_priority")
    private String ticketPriority;
    @Getter
    @SerializedName(value = "reply_count")
    private Integer replyCount;
}
