package bg.sofia.uni.fmi.mjt.mail.rules;

public enum RuleConditions {
    SUBJECT_INCLUDES("subject-includes"),
    SUBJECT_OR_BODY_INCLUDES("subject-or-body-includes"),
    RECIPIENTS_INCLUDES("recipients-include"),
    FROM("from");

    public final String prefix;
    RuleConditions(String prefix) {
        this.prefix = prefix;
    }
}
