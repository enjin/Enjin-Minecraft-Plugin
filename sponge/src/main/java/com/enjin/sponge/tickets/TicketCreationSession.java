package com.enjin.sponge.tickets;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.tickets.*;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.api.conversation.*;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TicketCreationSession {
    private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private static InteractiveFactory factory;
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
    private InteractiveConversation conversation;

    static {
        dateFormat.setLenient(false);
    }

    public TicketCreationSession(Player player, int moduleId, Module module) {
        this.uuid = player.getUniqueId();
        this.moduleId = moduleId;
        this.idMap = module.getIdMappedQuestions();
        this.questions = new ArrayList<>(module.getQuestions());
        Collections.sort(this.questions, (q1, q2) -> {
			if (q1.getOrder() == q2.getOrder()) {
				return Integer.compare(q1.getId(), q2.getId());
			} else {
				return Integer.compare(q1.getOrder(), q2.getOrder());
			}
		});

        for (Question question : new ArrayList<>(questions)) {
            plugin.debug("Processing question: " + question.getId() + " of type " + question.getType());

            if (question.getType() == QuestionType.file) {
                plugin.debug("File question type detected. Required: " + question.getRequired().booleanValue());
                if (question.getRequired().booleanValue()) {
                    player.sendMessage(Text.of(TextColors.GOLD, "This support ticket requires a file upload and must be submitted on the website."));
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
            factory = new InteractiveFactory(Enjin.getPlugin());
            factory.withAbandonListeners(event -> {
				if (event.getContext().getReceiver() instanceof Player) {
					Player p = (Player) event.getContext().getReceiver();
					sessions.remove(p.getUniqueId());
				}
			});
			factory.withCompletedListeners(event -> {
				if (event.getContext().getReceiver() instanceof Player) {
					Player p = (Player) event.getContext().getReceiver();
					sessions.remove(p.getUniqueId());
				}
			});
            factory.withCancellers((InteractiveCanceller) (context, input) -> input.toPlain().equalsIgnoreCase("abandon-ticket"));
            factory.withFirstPrompt(new StartPrompt());
        }

        if (player == null) {
            return;
        }

        this.conversation = factory.buildConversation(player);;
        sessions.put(player.getUniqueId(), this);
        conversation.begin();
    }

    public InteractivePrompt getNextPrompt() {
		InteractivePrompt prompt = null;

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
                                if (result) {
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
            final Optional<Player> player = Sponge.getServer().getPlayer(uuid);

            if (player.isPresent()) {
				plugin.getAsync().execute(() -> {
					TicketSubmission.submit(player.get(), moduleId, new ArrayList<>(responses.values()));
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
                    return (condition.getStatus() == Condition.Status.is) == option.equalsIgnoreCase(answer);
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

    public InteractivePrompt createPrompt(Question question) {
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

    public class StartPrompt extends InteractiveMessagePrompt {
        @Override
        protected InteractivePrompt getNextPrompt(InteractiveContext context) {
            TicketCreationSession session = sessions.get(((Player) context.getReceiver()).getUniqueId());
            return session != null ? session.getNextPrompt() : null;
        }

        @Override
        public Text getPromptText(InteractiveContext context) {
            return Text.of(TextColors.GOLD, "Type \"abandon-ticket\" to cancel at any time.");
        }
    }

    public class ErrorPrompt extends InteractiveMessagePrompt {
        @Override
        protected InteractivePrompt getNextPrompt(InteractiveContext context) {
            return null;
        }

        @Override
        public Text getPromptText(InteractiveContext context) {
            return Text.of(Text.NEW_LINE, TextColors.GOLD, "There was an error processing your ticket submission. We apologize for the inconvenience.");
        }
    }

    public class TextPrompt extends InteractiveTextPrompt {
        private Question question;

        public TextPrompt(Question question) {
            this.question = question;
        }

        @Override
        public Text getPromptText(InteractiveContext context) {
            Text text = Text.of(TextColors.GOLD, question.getLabel());

            if (question.getHelpText() != null && !question.getHelpText().isEmpty()) {
                text = text.concat(Text.NEW_LINE).concat(Text.of(question.getHelpText()));
            }

            return Text.NEW_LINE.concat(text);
        }

		@Override
		public InteractivePrompt acceptInput (InteractiveContext context, Text input) {
			responses.put(question.getId(), new QuestionResponse(question, input.toPlain()));
			TicketCreationSession session = sessions.get(((Player) context.getReceiver()).getUniqueId());
			return session != null ? session.getNextPrompt() : null;
		}
	}

    public class NumberPrompt extends InteractiveNumericPrompt {
        private Question question;

        public NumberPrompt(Question question) {
            this.question = question;
        }

		@Override
		protected InteractivePrompt acceptValidatedInput (InteractiveContext context, Number number) {
			responses.put(question.getId(), new QuestionResponse(question, number.toString()));
			TicketCreationSession session = sessions.get(((Player) context.getReceiver()).getUniqueId());
			return session != null ? session.getNextPrompt() : null;
		}

		@Override
        public Text getPromptText(InteractiveContext context) {
            Text text = Text.of(TextColors.GOLD, question.getLabel());

            if (question.getHelpText() != null && !question.getHelpText().isEmpty()) {
                text = text.concat(Text.of(Text.NEW_LINE, question.getHelpText()));
            }

            return Text.NEW_LINE.concat(text);
        }
	}

    public class DatePrompt extends InteractiveTextPrompt {
        private Question question;

        public DatePrompt(Question question) {
            this.question = question;
        }

        @Override
        public Text getPromptText(InteractiveContext context) {
            Text text = Text.of(TextColors.GOLD, question.getLabel());

            if (question.getHelpText() != null && !question.getHelpText().isEmpty()) {
                text = text.concat(Text.NEW_LINE).concat(Text.of(question.getHelpText()));
            }

            text = text.concat(Text.of(TextColors.GRAY, Text.NEW_LINE, "[Please type a date in the format DD-MM-YYYY]", TextColors.RESET));

            return Text.NEW_LINE.concat(text);
        }

        @Override
		public InteractivePrompt acceptInput (InteractiveContext context, Text input) {
            String text = input.toPlain().replace("/", "-");
            Date answer;

            try {
                answer = dateFormat.parse(text);
            } catch (ParseException e) {
                plugin.debug("User inputted a value that does not use format DD/MM/YYYY. Resending prompt.");
                return this;
            }

            responses.put(question.getId(), new QuestionResponse(question, dateFormat.format(answer)));
            TicketCreationSession session = sessions.get(((Player) context.getReceiver()).getUniqueId());
            return session != null ? session.getNextPrompt() : null;
        }
    }

    public class RadioSelectPrompt extends InteractiveTextPrompt {
        private Question question;

        public RadioSelectPrompt(Question question) {
            this.question = question;
        }

        @Override
        public Text getPromptText(InteractiveContext context) {
            Text text = Text.of(TextColors.GOLD, question.getLabel());

            if (question.getHelpText() != null && !question.getHelpText().isEmpty()) {
                text = text.concat(Text.NEW_LINE).concat(Text.of(question.getHelpText()));
            }

            text = text.concat(Text.of(TextColors.GRAY, Text.NEW_LINE, "[Please type a number]", TextColors.RESET));

            int i = 1;
            for (String option : question.getOptions()) {
                text = text.concat(Text.of(TextColors.GREEN, Text.NEW_LINE, i, TextColors.GRAY, ") ", TextColors.GOLD + option));
                i++;
            }

            return Text.NEW_LINE.concat(text);
        }

        @Override
		public InteractivePrompt acceptInput (InteractiveContext context, Text input) {
            String answer;
            try {
                int index = Integer.parseInt(input.toPlain()) - 1;
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
            TicketCreationSession session = sessions.get(((Player) context.getReceiver()).getUniqueId());
            return session != null ? session.getNextPrompt() : null;
        }
    }

    public class CheckboxPrompt extends InteractiveTextPrompt {
        private Question question;

        public CheckboxPrompt(Question question) {
            assert question.getType() == QuestionType.checkbox;
            this.question = question;
        }

        @Override
        public Text getPromptText(InteractiveContext context) {
            Text text = Text.of(TextColors.GOLD, question.getLabel());

            if (question.getHelpText() != null && !question.getHelpText().isEmpty()) {
                text = text.concat(Text.NEW_LINE).concat(Text.of(question.getHelpText()));
            }

            text = text.concat(Text.of(TextColors.GRAY, Text.NEW_LINE, "[Please type one or more numbers separated by commas]", TextColors.RESET));

            int i = 1;
            for (String option : question.getOptions()) {
                text = text.concat(Text.of(TextColors.GREEN, Text.NEW_LINE, i, TextColors.GRAY, ") ", TextColors.GOLD, option));
                i++;
            }

            return Text.NEW_LINE.concat(text);
        }

        @Override
		public InteractivePrompt acceptInput (InteractiveContext context, Text input) {
            String[] selections = input.toPlain().replace(" ", "").split(",");

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

            responses.put(question.getId(), new QuestionResponse(question, answers.toArray(new String[answers.size()])));
            TicketCreationSession session = sessions.get(((Player) context.getReceiver()).getUniqueId());
            return session != null ? session.getNextPrompt() : null;
        }
    }
}
