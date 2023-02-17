package bg.sofia.uni.fmi.mjt.mail;

import bg.sofia.uni.fmi.mjt.mail.exceptions.AccountAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.AccountNotFoundException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderNotFoundException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.InvalidEmailMetadataException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.InvalidPathException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.RuleAlreadyDefinedException;
import bg.sofia.uni.fmi.mjt.mail.rules.Rule;
import bg.sofia.uni.fmi.mjt.mail.rules.RuleConditions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Outlook implements MailClient {

    private static final String DEFAULT_INBOX_FOLDER_NAME = "/inbox";
    private static final String DEFAULT_SENT_FOLDER_NAME = "/sent";
    private static final String SEPARATOR = "/";
    private static final int MIN_PRIORITY = 1;
    private static final int MAX_PRIORITY = 10;
    private Set<Account> accounts;
    private Map<String, Set<Rule>> rules; // Key is accountName
    private Map<String, Map<String, Set<Mail>>> mails; // Key is accountName, Value is Map<Path to folder, Set<Mail>>

    public Outlook() {
        accounts = new HashSet<>();
        rules = new HashMap<>();
        mails = new HashMap<>();
    }

    /**
     * Creates a new account in the MailClient
     *
     * @param accountName short name of the account
     * @param email       email of the account
     * @return the created Account
     * @throws IllegalArgumentException      if any of the string parameters is null, empty or blank
     * @throws AccountAlreadyExistsException if account with the same name is already present in the client
     */
    @Override
    public Account addNewAccount(String accountName, String email) {
        validateString(accountName);
        validateString(email);

        if (checkIfAccountNameExists(accountName)) {
            throw new AccountAlreadyExistsException("Account with name \"" + accountName + "\" already exists");
        }
        if (checkIfEmailAlreadyExist(email)) {
            throw new AccountAlreadyExistsException("Account with email \"" + email + "\" already exists");
        }

        Account newAccount = new Account(email, accountName);
        accounts.add(newAccount);

        rules.putIfAbsent(accountName, new HashSet<>());
        mails.putIfAbsent(accountName, new HashMap<>());

        // Add the default folders to this account
        mails.get(accountName).put(DEFAULT_INBOX_FOLDER_NAME, new HashSet<>());
        mails.get(accountName).put(DEFAULT_SENT_FOLDER_NAME, new HashSet<>());

        return newAccount;
    }

    /**
     * @param accountName name of the account for which the folder is created
     * @param path        full path to the folder. The root folder and the path separator character
     *                    is forward slash ('/')
     * @throws IllegalArgumentException     if any of the string parameters is null, empty or blank
     * @throws AccountNotFoundException     if the account is not present
     * @throws InvalidPathException         if the folder path does not start from the root folder
     *                                      of received mails, or if some intermediate folders do not exist
     * @throws FolderAlreadyExistsException if folder with the same absolute path is already present
     *                                      for the provided account
     */
    @Override
    public void createFolder(String accountName, String path) {
        validateString(accountName);
        validateString(path);

        if (!checkIfAccountNameExists(accountName)) {
            throw new AccountNotFoundException("Account with name \"" + accountName + "\" doesn't exist");
        }

        if (!path.startsWith(DEFAULT_INBOX_FOLDER_NAME)) {
            throw new InvalidPathException("The path doesn't start with from the default inbox folder");
        }

        // Check for existing intermediate folders
        String[] folders = path.split(SEPARATOR);
        Set<String> existingFolders = mails.get(accountName).keySet(); // Paths
        int index = 0;
        String current = SEPARATOR + folders[index].trim();

        // When splitting "/inbox/folder" by "/" there is blank string at the beginning and `current` contains
        // separator only
        if (current.equals("/")) {
            current += folders[++index].trim();
        }

        while (index < folders.length - 1) {
            if (!existingFolders.contains(current)) {
                throw new InvalidPathException("Some of the folders in the path don't exist");
            }
            current += SEPARATOR + folders[++index].trim();
        }

        if (existingFolders.contains(path)) {
            throw new FolderAlreadyExistsException("Folder with path \"" + path + "\" already exists for this account");
        }

        mails.get(accountName).put(path, new HashSet<>());
    }

    /**
     * Creates a new Rule for the current mail client.
     * A Rule is defined via a string called Rule Definition. Each Rule Definition contains one or more Rule Conditions.
     * <p>
     * The following Rule Definition is the valid format for rules:
     * subject-includes: <list-of-keywords>
     * subject-or-body-includes: <list-of-keywords>
     * recipients-includes: <list-of-recipient-emails>
     * from: <sender-email>
     * <p>
     * The order is not determined, and the list might not be full. Example:
     * subject-includes: mjt, exam, 2022
     * subject-or-body-includes: exam
     * from: stoyo@fmi.bg
     * <p>
     * For subject-includes and subject-or-body-includes rule conditions, if more than one keyword is specified, all
     * must be contained for the rule to match, i.e. it is a conjunction condition. For recipients-includes,
     * it's enough for one listed recipient to match (disjunction condition). For from, it should be exact match.
     *
     * @param accountName    name of the account for which the rule is applied
     * @param folderPath     full path of the destination folder
     * @param ruleDefinition string definition of the rule
     * @param priority       priority of the rule - [1,10], 1 = highest priority
     * @throws IllegalArgumentException    if any of the string parameters is null, empty or blank,
     *                                     or the priority of the rule is not within the expected range
     * @throws AccountNotFoundException    if the account does not exist
     * @throws FolderNotFoundException     if the folder does not exist
     * @throws RuleAlreadyDefinedException if the rule definition contains a rule *condition* that already exists,
     *                                     e.g. a rule definition contains `subject-includes` twice, or any other
     *                                     condition more than once.
     */
    @Override
    public void addRule(String accountName, String folderPath, String ruleDefinition, int priority) {
        validateString(accountName);
        validateString(folderPath);
        validateString(ruleDefinition);

        if (priority < MIN_PRIORITY || priority > MAX_PRIORITY) {
            throw new IllegalArgumentException("Priority cannot be less than "
                                                + MIN_PRIORITY
                                                + " or greater than " +
                                                MAX_PRIORITY);
        }

        if (!checkIfAccountNameExists(accountName)) {
            throw new AccountNotFoundException("Account \"" + accountName + "\" doesn't exist");
        }

        if (!mails.get(accountName).containsKey(folderPath)) {
            throw new FolderNotFoundException("Folder \"" + folderPath + "\" doesn't exist");
        }

        Rule newRule = createRule(ruleDefinition, folderPath, priority);
        if (hasConflictRule(newRule, rules.get(accountName))) {
            throw new RuleAlreadyDefinedException("This rule is in conflict with some other rules");
        }

        rules.get(accountName).add(newRule);

        // Check inbox folder for this rule
        Set<Mail> inboxMails = mails.get(accountName).get(DEFAULT_INBOX_FOLDER_NAME);
        for (Mail current : inboxMails) {
            if (checkIfMailMatchesARule(current, newRule)) {
                mails.get(accountName).get(DEFAULT_INBOX_FOLDER_NAME).remove(current);
                mails.get(accountName).get(folderPath).add(current);
            }
        }
    }

    /**
     * The mail metadata has the following format (we always expect valid format of the mail metadata,
     * no validations are required):
     * sender: <sender-email>
     * subject: <subject>
     * recipients: <list-of-emails>
     * received: <LocalDateTime> - in format yyyy-MM-dd HH:mm
     * <p>
     * The order is not determined and the list might not be full. Example:
     * sender: testy@gmail.com
     * subject: Hello, MJT!
     * recipients: pesho@gmail.com, gosho@gmail.com,
     * received: 2022-12-08 14:14
     *
     * @param accountName  the recipient account
     * @param mailMetadata metadata, including the sender, all recipients, subject, and receiving time
     * @param mailContent  content of the mail
     * @throws IllegalArgumentException if any of the parameters is null, empty or blank
     * @throws AccountNotFoundException if the account does not exist
     */
    @Override
    public void receiveMail(String accountName, String mailMetadata, String mailContent) {
        validateString(accountName);
        validateString(mailMetadata);
        validateString(mailContent);

        if (!checkIfAccountNameExists(accountName)) {
            throw new AccountNotFoundException("Account with name \"" + accountName + "\" does not exist");
        }

        Mail newMail = createMail(mailMetadata, mailContent);

        addMail(accountName, newMail);
    }

    /**
     * Returns a collection of all mails contained directly in the provided folder.
     *
     * @param account    name of the selected account
     * @param folderPath full path of the folder
     * @return collections of mails available in the folder
     * @throws IllegalArgumentException if any of the parameters is null, empty or blank
     * @throws AccountNotFoundException if the account does not exist
     * @throws FolderNotFoundException  if the folder does not exist
     */
    @Override
    public Collection<Mail> getMailsFromFolder(String account, String folderPath) {
        validateString(account);
        validateString(folderPath);

        if (!checkIfAccountNameExists(account)) {
            throw new AccountNotFoundException("Account with name \"" + account + "\" does not exist");
        }

        if (!mails.get(account).containsKey(folderPath)) {
            throw new FolderNotFoundException("Folder \"" + folderPath + "\" does not exist for this account");
        }

        return mails.get(account).get(folderPath);
    }

    /**
     * Sends an email. This stores the mail into the sender's "/sent" folder.
     * For each recipient in the recipients email list in the metadata, if an account with this email exists,
     * a {@code receiveMail()} for this account, mail metadata and mail content is called.
     * If an account with the specified email does not exist, it is ignored.
     *
     * @param accountName  name of the sender
     * @param mailMetadata metadata of the mail. "sender" field should be included automatically
     *                     if missing or not correctly set
     * @param mailContent  content of the mail
     * @throws IllegalArgumentException if any of the parameters is null, empty or blank
     */
    @Override
    public void sendMail(String accountName, String mailMetadata, String mailContent) {
        validateString(accountName);
        validateString(mailMetadata);
        validateString(mailContent);

        if (!checkIfAccountNameExists(accountName)) {
            throw new AccountNotFoundException("Account with name \"" + accountName + "\" does not exist");
        }

        Mail newMail = createMail(mailMetadata, mailContent);

        mails.get(accountName).get(DEFAULT_SENT_FOLDER_NAME).add(newMail);

        Set<String> recipients = newMail.recipients();
        for (String email : recipients) {
            try {
                Account current = findAccountByEmail(email);
                addMail(current.name(), newMail);
            }
            catch (AccountNotFoundException e) {
                // do nothing, because "If an account with the specified email does not exist, it is ignored."
            }
        }

    }

    public Set<String> getExistingFolders(String accountName) {
        validateString(accountName);

        return Set.copyOf(mails.get(accountName).keySet());
    }

    boolean hasConflictRule(Rule newRule, Set<Rule> rules) {
        for (Rule rule : rules) {
            if (rule.priority() == newRule.priority() &&
                rule.from().equals(newRule.from()) &&
                rule.recipientsIncludesEmails().equals(newRule.recipientsIncludesEmails()) &&
                rule.subjectIncludesKeywords().equals(newRule.subjectIncludesKeywords()) &&
                rule.subjectOrBodyIncludesKeywords().equals(newRule.subjectOrBodyIncludesKeywords()) &&
                !rule.destinationFolder().equals(newRule.destinationFolder())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIfMailMatchesARule(Mail mail, Rule rule) {
        Set<String> subjectIncludesKeywords = rule.subjectIncludesKeywords();
        for (String keyword : subjectIncludesKeywords) {
            if (!mail.subject().toLowerCase().contains(keyword.toLowerCase())) {
                return false;
            }
        }

        Set<String> subjectOrBodyIncludesKeywords = rule.subjectOrBodyIncludesKeywords();
        for (String keyword : subjectOrBodyIncludesKeywords) {
            if (!mail.subject().toLowerCase().contains(keyword.toLowerCase()) &&
                    !mail.body().toLowerCase().contains(keyword.toLowerCase())) {
                return false;
            }
        }

        if (rule.from() != null && !rule.from().equals(mail.sender().emailAddress())) {
            return false;
        }

        Set<String> recipientsIncludesEmails = rule.recipientsIncludesEmails();
        if (recipientsIncludesEmails.isEmpty()) {
            return true;
        }
        for (String email : recipientsIncludesEmails) {
            if (mail.recipients().contains(email)) {
                return true;
            }
        }

        return false;
    }

    private void addMail(String accountName, Mail mail) {
        if (!checkIfAccountNameExists(accountName)) {
            throw new AccountNotFoundException("Account with name \"" + accountName + "\" does not exist");
        }

        Set<Rule> rulesSet = rules.get(accountName);
        Rule maxPriorityRule = null;
        int maxPriority = 0;
        for (Rule rule : rulesSet) {
            if (checkIfMailMatchesARule(mail, rule) && rule.priority() > maxPriority) {
                maxPriorityRule = rule;
                maxPriority = rule.priority();
            }
        }

        if (maxPriorityRule != null) {
            mails.get(accountName).get(maxPriorityRule.destinationFolder()).add(mail);
        }
        else {
            mails.get(accountName).get(DEFAULT_INBOX_FOLDER_NAME).add(mail);
        }
    }

    private Mail createMail(String metadata, String body) {
        Account sender = null;
        String subject = null;
        Set<String> recipients = new HashSet<>();
        LocalDateTime received = null;

        final String[] lines = metadata.trim().split("\\r?\\n");

        for (String line : lines) {
            String content = line.substring(line.indexOf(':') + 1).trim();

            if (line.trim().startsWith(MailMetadata.SENDER.prefix)) {
                try {
                    sender = findAccountByEmail(content);
                }
                catch (AccountNotFoundException e) {
                    throw new InvalidEmailMetadataException("Invalid sender email", e);
                }
            }
            else if (line.trim().startsWith(MailMetadata.RECIPIENTS.prefix)) {
                String[] emails = content.split(",");
                for (String email : emails) {
                    if (!email.isBlank()) {
                        recipients.add(email.trim());
                    }
                }
            }

            else if (line.trim().startsWith(MailMetadata.SUBJECT.prefix)) {
                subject = content;
            }
            else if (line.trim().startsWith(MailMetadata.RECEIVED.prefix)) {
                received = LocalDateTime.parse(content, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
        }

        if (sender == null || subject == null || received == null || recipients.isEmpty()) {
            throw new InvalidEmailMetadataException("Email metadata given does not contain required information");
        }

        return new Mail(sender, recipients, subject, body, received);
    }

    private Rule createRule(String ruleDefinition, String destinationFolder, int priority) {
        final String[] lines = ruleDefinition.trim().split("\\r?\\n");

        Set<String> subjectIncludesKeywords = new HashSet<>();
        Set<String> subjectOrBodyIncludesKeywords = new HashSet<>();
        Set<String> recipientsIncludesEmails = new HashSet<>();
        String from = null;

        for (String line : lines) {
            String content = line.substring(line.indexOf(':') + 1).trim();
            if (line.trim().startsWith(RuleConditions.SUBJECT_INCLUDES.prefix)) {
                if (!subjectIncludesKeywords.isEmpty()) {
                    throw new RuleAlreadyDefinedException("subject-includes condition met more than once");
                }
                String[] keywords = content.split(",");
                for (String keyword : keywords) {
                    if (!keyword.isBlank()) {
                        subjectIncludesKeywords.add(keyword.trim());
                    }
                }
            }
            else if (line.trim().startsWith(RuleConditions.SUBJECT_OR_BODY_INCLUDES.prefix)) {
                if (!subjectOrBodyIncludesKeywords.isEmpty()) {
                    throw new RuleAlreadyDefinedException("subject-or-body-includes condition met more than once");
                }
                String[] keywords = content.split(", ");
                for (String keyword : keywords) {
                    if (!keyword.isBlank()) {
                        subjectOrBodyIncludesKeywords.add(keyword.trim());
                    }
                }
            }
            else if (line.trim().startsWith(RuleConditions.RECIPIENTS_INCLUDES.prefix)) {
                if (!recipientsIncludesEmails.isEmpty()) {
                    throw new RuleAlreadyDefinedException("recipients-includes condition met more than once");
                }
                String[] emails = content.split(", ");
                for (String email : emails) {
                    if (!email.isBlank()) {
                        recipientsIncludesEmails.add(email.trim());
                    }
                }
            }
            else if (line.trim().startsWith(RuleConditions.FROM.prefix)) {
                if (from != null) {
                    throw new RuleAlreadyDefinedException("from condition met more than once");
                }
                from = content;
            }
        }

        return new Rule(subjectIncludesKeywords,
                subjectOrBodyIncludesKeywords,
                recipientsIncludesEmails,
                from,
                destinationFolder,
                priority
        );
    }

    private boolean checkIfAccountNameExists(String accountName) {
        for (Account current : accounts) {
            if (current.name().equalsIgnoreCase(accountName)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIfEmailAlreadyExist(String email) {
        for (Account current : accounts) {
            if (current.emailAddress().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    private Account findAccountByEmail(String email) {
        for (Account current : accounts) {
            if (current.emailAddress().equalsIgnoreCase(email)) {
                return current;
            }
        }
        throw new AccountNotFoundException("Account with email \"" + email + "\" not found");
    }

    private void validateString(String string) {
        if (string == null || string.isBlank()) {
            throw new IllegalArgumentException("String cannot be null, empty or blank");
        }
    }
}
