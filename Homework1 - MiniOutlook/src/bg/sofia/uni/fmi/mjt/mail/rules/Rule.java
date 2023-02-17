package bg.sofia.uni.fmi.mjt.mail.rules;
import java.util.Set;

public record Rule(Set<String> subjectIncludesKeywords,
                   Set<String> subjectOrBodyIncludesKeywords,
                   Set<String> recipientsIncludesEmails,
                   String from,
                   String destinationFolder,
                   int priority) {
}
