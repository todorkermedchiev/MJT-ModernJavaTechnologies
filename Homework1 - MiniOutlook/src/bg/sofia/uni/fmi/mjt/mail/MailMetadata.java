package bg.sofia.uni.fmi.mjt.mail;

public enum MailMetadata {
    SENDER("sender"),
    SUBJECT("subject"),
    RECIPIENTS("recipients"),
    RECEIVED("received");

    public final String prefix;
    MailMetadata(String prefix) {
        this.prefix = prefix;
    }
}
