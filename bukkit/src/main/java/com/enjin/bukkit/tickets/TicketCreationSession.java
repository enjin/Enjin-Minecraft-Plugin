package com.enjin.bukkit.tickets;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.tickets.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TicketCreationSession {
    private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private static ConversationFactory factory;
    @Getter
    private static Map<UUID, TicketCreationSession> sessions = new HashMap<>();

    private int moduleId;
    private Map<Integer, Question> idMap;
    private List<Question> questions;
    private List<Question> conditional = new ArrayList<>();
    private Map<Integer, QuestionResponse> responses = new HashMap<>();

    private EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();
    private UUID uuid;
    @Getter
    private Conversation conversation;

    static {
        dateFormat.setLenient(false);
    }

    public TicketCreationSession(Player player, int moduleId, Module module) {
        this.uuid = player.getUniqueId();
        this.moduleId = moduleId;
        this.idMap = module.getIdMappedQuestions();
        this.questions = new ArrayList<>(module.getQuestions());
        Collections.sort(this.questions, new Comparator<Question>() {
            @Override
            public int compare(Question q1, Question q2) {
                if (q1.getOrder() == q2.getOrder()) {
                    return Integer.compare(q1.getId(), q2.getId());
                } else {
                    return Integer.compare(q1.getOrder(), q2.getOrder());
                }
            }
        });

        for (Question question : new ArrayList<>(questions)) {
            plugin.debug("Processing question: " + question.getId() + " of type " + question.getType());

            if (question.getType() == QuestionType.file) {
                plugin.debug("File question type detected. Required: " + question.getRequired().booleanValue());
                if (question.getRequired().booleanValue()) {
                    player.sendMessage(ChatColor.GOLD + "This support ticket requires a file upload and must be submitted on the website.");
                    return;
                } else {
                    this.questions.remove(question);
                }
            } else if (question.getType() == QuestionType.section) {
                this.questions.remove(question);
            }

            if (question.getConditions() != null && this.questions.size() > 0) {
                this.questions.remove(question);
                this.conditional.add(question);
            }
        }

        if (factory == null) {
            factory = new ConversationFactory(EnjinMinecraftPlugin.getInstance());
            factory.addConversationAbandonedListener(new ConversationAbandonedListener() {
                @Override
                public void conversationAbandoned(ConversationAbandonedEvent event) {
                    if (event.getContext().getForWhom() instanceof Player) {
                        Player p = (Player) event.getContext().getForWhom();
                        sessions.remove(p.getUniqueId());
                    }
                }
            });
            factory.withEscapeSequence("abandon-ticket");
            factory.withTimeout(60);
            factory.withModality(false);
            factory.withFirstPrompt(new StartPrompt());
        }

        if (player == null) {
            return;
        }

        final Conversation conversation = factory.buildConversation(player);
        this.conversation = conversation;
        sessions.put(player.getUniqueId(), this);
        conversation.begin();
    }

    public Prompt getNextPrompt() {
        Prompt prompt = null;

        plugin.debug("Getting next prompt.");
        if (questions != null) {
            if (!questions.isEmpty()) {
                Question question = questions.remove(0);
                plugin.debug("Creating prompt for question: " + question.getId() + "|" + question.getLabel() + " in module: " + question.getPresetId());
                prompt = createPrompt(question);
            } else {
                plugin.debug("Checking conditionals for next question to prompt.");
                if (!conditional.isEmpty()) {
                    for (Question question : new ArrayList<>(conditional)) {
                        boolean conditionsMet = false;
                        if (question.getConditionQualify() == ConditionQualify.one_true) {
                            plugin.debug("Question: " + question.getId() + "|" + question.getLabel() + " requires that condition be met. Checking conditions.");
                            for (Condition condition : question.getConditions()) {
                                boolean result = conditionMet(condition);
                                if (result == true) {
                                    conditionsMet = true;
                                    break;
                                }
                            }
                        } else {
                            for (Condition condition : question.getConditions()) {
                                if (!conditionMet(condition)) {
                                    break;
                                }
                                conditionsMet = true;
                            }
                        }

                        if (conditionsMet) {
                            plugin.debug("Question: " + question.getId() + "|" + question.getLabel() + " meets conditions.");
                            conditional.remove(question);
                            prompt = createPrompt(question);
                        } else {
                            plugin.debug("Question: " + question.getId() + "|" + question.getLabel() + " does not meet conditions.");
                            plugin.debug("Checking if condition relies on another conditional.");
                            boolean conditionalRequiresConditional = false;
                            for (Condition condition : question.getConditions()) {
                                for (Question q : conditional) {
                                    if (condition.getQuestion() == q.getId()) {
                                        plugin.debug("Question: " + question.getId() + "|" + question.getLabel() + " relies on conditional: " + q.getId() + "|" + q.getLabel());
                                        conditionalRequiresConditional = true;
                                    }
                                }
                            }

                            if (!conditionalRequiresConditional) {
                                plugin.debug("Does not rely on conditional. Removing impossible question.");
                                conditional.remove(question);
                            }
                        }
                    }
                }
            }
        }

        if (prompt == null && questions.isEmpty() && conditional.isEmpty()) {
            plugin.debug("No possible conditionals remaining. Submitting ticket.");
            final Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        TicketSubmission.submit(player, moduleId, new ArrayList<>(responses.values()));
                    }
                });
            }

            return null;
        }

        return prompt == null ? new ErrorPrompt() : prompt;
    }

    public boolean conditionMet(Condition condition) {
        if (responses.containsKey(condition.getQuestion())) {
            Question question = idMap.get(condition.getQuestion());
            QuestionResponse response = responses.get(question.getId());
            String option = question.getOptions().get(condition.getAnswer());

            if (question.getType() == QuestionType.radio || question.getType() == QuestionType.select) {
                if (response.getAnswer() instanceof String) {
                    String answer = (String) response.getAnswer();
                    return condition.getStatus() == Condition.Status.is ? option.equalsIgnoreCase(answer) : !option.equalsIgnoreCase(answer);
                }
            } else if (question.getType() == QuestionType.checkbox) {
                if (response.getAnswer() instanceof List) {
                    List<String> answers = (List<String>) response.getAnswer();
                    for (String answer : answers) {
                        if (condition.getStatus() == Condition.Status.is) {
                            if (answer.equalsIgnoreCase(answer)) {
                                return true;
                            }
                        } else {
                            if (option.equalsIgnoreCase(answer)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public Prompt createPrompt(Question question) {
        if (question == null) {
            return null;
        }

        switch (question.getType()) {
            case text:
            case multiline:
                return new TextPrompt(question);
            case number:
                return new NumberPrompt(question);
            case date:
                return new DatePrompt(question);
            case select:
            case radio:
                return new RadioSelectPrompt(question);
            case checkbox:
                return new CheckboxPrompt(question);
            default:
                return null;
        }
    }

    public class StartPrompt extends MessagePrompt {
        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            TicketCreationSession session = sessions.get(((Player) context.getForWhom()).getUniqueId());
            return session != null ? session.getNextPrompt() : null;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.GOLD + "Type \"abandon-ticket\" to cancel at any time.";
        }
    }

    public class ErrorPrompt extends MessagePrompt {
        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return null;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return "\n" + ChatColor.GOLD + "There was an error processing your ticket submission. We apologize for the inconvenience.";
        }
    }

    public class TextPrompt extends StringPrompt {
        private Question question;

        public TextPrompt(Question question) {
            this.question = question;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + question.getLabel();

            if (question.getHelpText() != null && !question.getHelpText().isEmpty()) {
                text += "\n" + question.getHelpText();
            }

            return "\n" + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            responses.put(question.getId(), new QuestionResponse(question, input));
            TicketCreationSession session = sessions.get(((Player) context.getForWhom()).getUniqueId());
            return session != null ? session.getNextPrompt() : null;
        }
    }

    public class NumberPrompt extends NumericPrompt {
        private Question question;

        public NumberPrompt(Question question) {
            this.question = question;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number number) {
            responses.put(question.getId(), new QuestionResponse(question, number.toString()));
            TicketCreationSession session = sessions.get(((Player) context.getForWhom()).getUniqueId());
            return session != null ? session.getNextPrompt() : null;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + question.getLabel();

            if (question.getHelpText() != null && !question.getHelpText().isEmpty()) {
                text += "\n" + question.getHelpText();
            }

            return "\n" + text;
        }
    }

    public class DatePrompt extends StringPrompt {
        private Question question;

        public DatePrompt(Question question) {
            this.question = question;
        }

        @Override
        public String getPromptText(ConversationContext conversationContext) {
            String text = ChatColor.GOLD + question.getLabel();

            if (question.getHelpText() != null && !question.getHelpText().isEmpty()) {
                text += "\n" + question.getHelpText();
            }

            text += ChatColor.GRAY + "\n[Please type a date in the format DD-MM-YYYY]" + ChatColor.RESET;

            return "\n" + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            input = input.replace("/", "-");
            Date answer;

            try {
                answer = dateFormat.parse(input);
            } catch (ParseException e) {
                plugin.debug("User inputted a value that does not use format DD/MM/YYYY. Resending prompt.");
                return this;
            }

            responses.put(question.getId(), new QuestionResponse(question, input));
            TicketCreationSession session = sessions.get(((Player) context.getForWhom()).getUniqueId());
            return session != null ? session.getNextPrompt() : null;
        }
    }

    public class RadioSelectPrompt extends StringPrompt {
        private Question question;

        public RadioSelectPrompt(Question question) {
            this.question = question;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + question.getLabel();

            if (question.getHelpText() != null && !question.getHelpText().isEmpty()) {
                text += "\n" + question.getHelpText();
            }

            text += ChatColor.GRAY + "\n[Please type a number]" + ChatColor.RESET;

            int i = 1;
            for (String option : question.getOptions()) {
                text += ChatColor.GREEN + "\n" + i + ChatColor.GRAY + ") " + ChatColor.GOLD + option;
                i++;
            }

            return "\n" + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            String answer;
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= question.getOptions().size()) {
                    plugin.debug("User selection is out of bounds. Resending prompt");
                    return this;
                }

                answer = question.getOptions().get(index);
            } catch (NumberFormatException e) {
                plugin.debug("User inputted a value that is NaN. Resending prompt.");
                return this;
            }

            responses.put(question.getId(), new QuestionResponse(question, answer));
            TicketCreationSession session = sessions.get(((Player) context.getForWhom()).getUniqueId());
            return session != null ? session.getNextPrompt() : null;
        }
    }

    public class CheckboxPrompt extends StringPrompt {
        private Question question;

        public CheckboxPrompt(Question question) {
            assert question.getType() == QuestionType.checkbox;
            this.question = question;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + question.getLabel();

            if (question.getHelpText() != null && !question.getHelpText().isEmpty()) {
                text += "\n" + question.getHelpText();
            }

            text += ChatColor.GRAY + "\n[Please type one or more numbers separated by commas]" + ChatColor.RESET;

            int i = 1;
            for (String option : question.getOptions()) {
                text += ChatColor.GREEN + "\n" + i + ChatColor.GRAY + ") " + ChatColor.GOLD + option;
                i++;
            }

            return "\n" + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            String[] selections = input.replace(" ", "").split(",");

            List<String> answers = new ArrayList<>();
            for (String selection : selections) {
                try {
                    int index = Integer.parseInt(selection) - 1;
                    if (index >= question.getOptions().size()) {
                        return this;
                    }

                    answers.add(question.getOptions().get(index));
                } catch (NumberFormatException e) {
                    return this;
                }
            }

            responses.put(question.getId(), new QuestionResponse(question, answers.toArray(new String[]{})));
            TicketCreationSession session = sessions.get(((Player) context.getForWhom()).getUniqueId());
            return session != null ? session.getNextPrompt() : null;
        }
    }
}
