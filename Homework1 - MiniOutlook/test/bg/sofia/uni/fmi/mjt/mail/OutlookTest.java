package bg.sofia.uni.fmi.mjt.mail;

import bg.sofia.uni.fmi.mjt.mail.exceptions.AccountAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.AccountNotFoundException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderNotFoundException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.InvalidEmailMetadataException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.InvalidPathException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.RuleAlreadyDefinedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class OutlookTest {
    Outlook mailClient;

    @BeforeEach
    void initializeOutlook() {
        mailClient = new Outlook();
    }

    @Test
    void testAddNewAccountNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> mailClient.addNewAccount(null, "email@abv.bg"),
                "AccountName cannot be null - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> mailClient.addNewAccount("Name", null),
                "Email cannot be null - expected IllegalArgumentException");
    }

    @Test
    void testAddNewAccountBlankArguments() {
        assertThrows(IllegalArgumentException.class, () -> mailClient.addNewAccount("", "email@abv.bg"),
                "AccountName cannot be empty or blank - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> mailClient.addNewAccount("Name", "  "),
                "Email cannot be empty or blank - expected IllegalArgumentException");
    }

    @Test
    void testAddNewAccountNameValidArguments() {
        Account actual = mailClient.addNewAccount("Name", "email@abv.bg");
        Account expected = new Account("email@abv.bg", "Name");

        assertEquals(expected, actual);
        assertTrue(mailClient.getExistingFolders("Name").contains("/inbox") &&
                mailClient.getExistingFolders("Name").contains("/sent"),
                "Default sent and inbox folders was not created");
    }

    @Test
    void testAddNewAccountExistingAccount() {
        mailClient.addNewAccount("Name", "email@abv.bg");

        assertThrows(AccountAlreadyExistsException.class, () -> mailClient.addNewAccount("Name",
                "otherEmail@abv.bg"), "Another account with same accountName cannot be added - " +
                "expected AccountAlreadyExistsException");
        assertThrows(AccountAlreadyExistsException.class, () -> mailClient.addNewAccount("otherName",
                "email@abv.bg"), "Another account with same email address cannot be added - " +
                "expected AccountAlreadyExistsException");
    }

    @Test
    void testCreateFolderNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> mailClient.createFolder(null, "/inbox/folder"),
                "AccountName cannot be null - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> mailClient.createFolder("name", null),
                "Path cannot be null - expected IllegalArgumentException");
    }

    @Test
    void testCreateFolderBlankArguments() {
        assertThrows(IllegalArgumentException.class, () -> mailClient.createFolder("", "/inbox/folder"),
                "AccountName cannot be empty or blank - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> mailClient.createFolder("name", "  "),
                "Path cannot be empty or blank - expected IllegalArgumentException");
    }

    @Test
    void testCreateFolderMissingAccount() {
        assertThrows(AccountNotFoundException.class, () -> mailClient.createFolder("name", "/inbox/path"),
                "This account must exist when creating new folder - Expected AccountNotFoundException");
    }

    @Test
    void testCreateFolderPathDoesNotStartWithInbox() {
        mailClient.addNewAccount("name", "email@abv.bg");

        assertThrows(InvalidPathException.class, () -> mailClient.createFolder("name",
                "/folder/newFolder"), "The new folder must be subdirectory of inbox folder");
    }

    @Test
    void testCreateFolderMissingIntermediateFolders() {
        mailClient.addNewAccount("name", "email@abv.bg");

        assertThrows(InvalidPathException.class, () -> mailClient.createFolder("name",
                "/inbox/important/newFolder"), "All intermediate folders must exist before creating new " +
                "folder - expected InvalidPathException");
    }

    @Test
    void testCreateFolderExistingIntermediateFolders() {
        mailClient.addNewAccount("name", "email@abv.bg");
        mailClient.createFolder("name", "/inbox/important");

        assertTrue(mailClient.getExistingFolders("name").contains("/inbox/important"),
                "The new folder not created");
    }

    @Test
    void testCreateFolderAlreadyCreatedFolder() {
        mailClient.addNewAccount("name", "email@abv.bg");
        mailClient.createFolder("name", "/inbox/important");

        assertThrows(FolderAlreadyExistsException.class, () -> mailClient.createFolder("name",
                "/inbox/important"), "The folder must not be created, because it already exists");
    }

    @Test
    void testAddRuleInvalidArguments() {
        // null strings
        assertThrows(IllegalArgumentException.class, () -> mailClient.addRule(null, "/inbox",
                "def", 3), "Account name cannot be null");
        assertThrows(IllegalArgumentException.class, () -> mailClient.addRule("name", null,
                "def", 3), "Folder path cannot be null");
        assertThrows(IllegalArgumentException.class, () -> mailClient.addRule("name", "/inbox",
                null, 3), "Rule definition cannot be null");

        // empty or blank strings
        assertThrows(IllegalArgumentException.class, () -> mailClient.addRule("", "/inbox",
                "def", 3), "Account name cannot be empty or blank");
        assertThrows(IllegalArgumentException.class, () -> mailClient.addRule("name", "  ",
                "def", 3), "Folder path cannot be empty or blank");
        assertThrows(IllegalArgumentException.class, () -> mailClient.addRule("name", "/inbox",
                "", 3), "Rule definition cannot be empty or blank");

        assertThrows(IllegalArgumentException.class, () -> mailClient.addRule("name", "/inbox",
                "def", 13), "Priority must be between 1 and 10");
        assertThrows(IllegalArgumentException.class, () -> mailClient.addRule("name", "/inbox",
                "def", 0), "Priority must be between 1 and 10");
    }

    @Test
    void testAddRuleMissingAccount() {
        assertThrows(AccountNotFoundException.class, () -> mailClient.addRule("name", "/inbox",
                "def", 3), "When account is missing the rule must not be added");
    }

    @Test
    void testAddRuleMissingFolder() {
        mailClient.addNewAccount("name", "email@abv.bg");

        assertThrows(FolderNotFoundException.class, () -> mailClient.addRule("name", "/inbox/important",
                "def", 3), "When the target folder is missing the rule must not be added");
    }

    @Test
    void testAddRuleConflictRule() {
        mailClient.addNewAccount("name", "email@abv.bg");
        mailClient.createFolder("name", "/inbox/important");

        final String ruleDefinition = """
                subject-includes: mjt, exam, 2022
                subject-or-body-includes: exam
                recipients-includes: email@abv.bg
                from: stoyo@fmi.bg
                """;

        mailClient.addRule("name", "/inbox", ruleDefinition, 3);

        assertThrows(RuleAlreadyDefinedException.class, () -> mailClient.addRule("name",
                "/inbox/important", ruleDefinition, 3));
    }

    @Test
    void testAddRuleWhenRuleConditionsRepeats() {
        mailClient.addNewAccount("name", "email@abv.bg");
        mailClient.createFolder("name", "/inbox/important");

        final String ruleDefinition = """
                subject-includes: mjt, exam, 2022
                subject-or-body-includes: exam
                recipients-includes: email@abv.bg
                from: stoyo@fmi.bg
                """;

        assertThrows(RuleAlreadyDefinedException.class, () -> mailClient.addRule("name",
                "/inbox/important", ruleDefinition + System.lineSeparator() +
                        "subject-includes : mjt", 3));
        assertThrows(RuleAlreadyDefinedException.class, () -> mailClient.addRule("name",
                "/inbox/important", ruleDefinition + System.lineSeparator() +
                        "subject-or-body-includes : mjt", 3));
        assertThrows(RuleAlreadyDefinedException.class, () -> mailClient.addRule("name",
                "/inbox/important", ruleDefinition + System.lineSeparator() +
                        "recipients-includes : gosho@gmail.com", 3));
        assertThrows(RuleAlreadyDefinedException.class, () -> mailClient.addRule("name",
                "/inbox/important", ruleDefinition + System.lineSeparator() +
                        "from: gosho@gmail.bg", 3));
    }

    @Test
    void testAddRuleMoveMailsFromInboxFolderToTargetFolder() {
        mailClient.addNewAccount("pesho", "pesho@gmail.com");
        mailClient.addNewAccount("gosho", "gosho@gmail.com");
        mailClient.addNewAccount("kiro", "kiro@gmail.com");

        mailClient.createFolder("pesho", "/inbox/important");

        final String mailMetadata1 = """
                sender: kiro@gmail.com
                subject: Test method!
                recipients: pesho@gmail.com, gosho@gmail.com,
                received: 2022-12-08 14:14
                """;

        final String mailMetadata2 = """
                sender: gosho@gmail.com
                subject: Important! Test method!
                recipients: pesho@gmail.com, kiro@gmail.com,
                received: 2022-12-08 14:25
                """;

        final String mailContent = """
                Boys, let's test this method! :D
                """;

        Set<String> recipients1 = new HashSet<>();
        Set<String> recipients2 = new HashSet<>();
        recipients1.add("pesho@gmail.com");
        recipients1.add("gosho@gmail.com");
        recipients2.add("pesho@gmail.com");
        recipients2.add("kiro@gmail.com");

        Set<Mail> inboxMails = new HashSet<>();
        inboxMails.add(new Mail(new Account("kiro@gmail.com", "kiro"), recipients1,
                "Test method!", mailContent, LocalDateTime.of(2022, 12, 8, 14, 14)));

        Set<Mail> importantMails = new HashSet<>();
        importantMails.add(new Mail(new Account("gosho@gmail.com", "gosho"), recipients2,
                "Important! Test method!", mailContent, LocalDateTime.of(2022, 12, 8, 14, 25)));

        mailClient.receiveMail("pesho", mailMetadata1, mailContent);
        mailClient.receiveMail("pesho", mailMetadata2, mailContent);

        final String ruleDefinition = """
                subject-includes: important
                subject-or-body-includes: method
                """;
        mailClient.addRule("pesho", "/inbox/important", ruleDefinition, 3);

        Collection<Mail> inbox = mailClient.getMailsFromFolder("pesho", "/inbox");
        Collection<Mail> important = mailClient.getMailsFromFolder("pesho", "/inbox/important");

        assertIterableEquals(inboxMails, inbox, "Mails not removed from inbox folder");
        assertIterableEquals(importantMails, important, "Mails not added to target folder");
    }

    @Test
    void testReceiveMailNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> mailClient.receiveMail(null, "metadata",
                "content"), "Account name cannot be null");
        assertThrows(IllegalArgumentException.class, () -> mailClient.receiveMail("name", null,
                "content"), "Mail metadata cannot be null");
        assertThrows(IllegalArgumentException.class, () -> mailClient.receiveMail("name", "metadata",
                null), "Mail content cannot be null");
    }

    @Test
    void testReceiveMailBlankArguments() {
        assertThrows(IllegalArgumentException.class, () -> mailClient.receiveMail("", "metadata",
                "content"), "Account name cannot be empty or blank");
        assertThrows(IllegalArgumentException.class, () -> mailClient.receiveMail("name", "  ",
                "content"), "Mail metadata cannot be empty or blank");
        assertThrows(IllegalArgumentException.class, () -> mailClient.receiveMail("name", "metadata",
                "    "), "Mail content cannot be empty or blank");
    }

    @Test
    void testReceiveMailMissingAccount() {
        assertThrows(AccountNotFoundException.class, () -> mailClient.receiveMail("name", "metadata",
                "content"), "Mail cannot be received when the account does not exist");
    }

    @Test
    void testReceiveMailInvalidMailMetadata() {
        mailClient.addNewAccount("kiro", "kiro@gmail.com");

        final String mailMetadata = """
                sender: kiro@gmail.com
                subject: Hello, MJT!
                received: 2022-12-08 14:14
                """;

        final String mailContent = "Mail Content";

        assertThrows(InvalidEmailMetadataException.class, () -> mailClient.receiveMail("kiro", mailMetadata,
                mailContent), "Invalid email metadata - InvalidEmailMetadataException must be thrown");
    }

    @Test
    void testReceiveMailValidArguments() {
        mailClient.addNewAccount("pesho", "pesho@gmail.com");
        mailClient.addNewAccount("gosho", "gosho@gmail.com");
        mailClient.addNewAccount("kiro", "kiro@gmail.com");

        final String mailMetadata = """
                sender: kiro@gmail.com
                subject: Hello, MJT!
                recipients: pesho@gmail.com, gosho@gmail.com,
                received: 2022-12-08 14:14
                """;

        final String mailContent = """
                hi, Pesho!
                hi, Gosho!
                
                from:
                    Kiro :)
                """;

        Set<String> recipients = new HashSet<>();
        recipients.add("pesho@gmail.com");
        recipients.add("gosho@gmail.com");

        Set<Mail> expected = new HashSet<>();
        expected.add(new Mail(new Account("kiro@gmail.com", "kiro"), recipients,
                "Hello, MJT!", mailContent, LocalDateTime.of(2022, 12, 8, 14, 14)));

        mailClient.receiveMail("pesho", mailMetadata, mailContent);

        Collection<Mail> received = mailClient.getMailsFromFolder("pesho", "/inbox");

        assertIterableEquals(expected, received, "Mail not received");
    }

    @Test
    void testReceiveMailExistingRuleApplied() {
        mailClient.addNewAccount("pesho", "pesho@gmail.com");
        mailClient.addNewAccount("gosho", "gosho@gmail.com");
        mailClient.addNewAccount("kiro", "kiro@gmail.com");

        mailClient.createFolder("pesho", "/inbox/important");

        final String ruleDefinition = """
                subject-includes: important
                subject-or-body-includes: method
                """;
        mailClient.addRule("pesho", "/inbox/important", ruleDefinition, 3);

        final String mailMetadata = """
                sender: kiro@gmail.com
                subject: Important! Test method!
                recipients: pesho@gmail.com, gosho@gmail.com,
                received: 2022-12-08 14:25
                """;

        final String mailContent = """
                Boys, let's test this method! :D
                """;

        Set<String> recipients = new HashSet<>();
        recipients.add("pesho@gmail.com");
        recipients.add("gosho@gmail.com");

        Set<Mail> expected = new HashSet<>();
        expected.add(new Mail(new Account("kiro@gmail.com", "kiro"), recipients,
                "Important! Test method!", mailContent, LocalDateTime.of(2022, 12, 8, 14, 25)));

        mailClient.receiveMail("pesho", mailMetadata, mailContent);
        mailClient.receiveMail("gosho", mailMetadata, mailContent);

        Collection<Mail> inboxPesho = mailClient.getMailsFromFolder("pesho", "/inbox");
        Collection<Mail> importantPesho = mailClient.getMailsFromFolder("pesho", "/inbox/important");
        Collection<Mail> inboxGosho = mailClient.getMailsFromFolder("gosho", "/inbox");

        assertIterableEquals(expected, importantPesho, "Mail not added to pesho's /inbox/important folder");
        assertTrue(inboxPesho.isEmpty(), "Mail not removed from pesho's /inbox folder");
        assertIterableEquals(expected, inboxGosho, "Mail not added to gosho's /inbox folder");
    }

    @Test
    void testGetMailsFromFolderNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> mailClient.getMailsFromFolder(null, "/inbox"),
                "Account name cannot be null");
        assertThrows(IllegalArgumentException.class, () -> mailClient.getMailsFromFolder("name", null),
                "Folder path cannot be null");
    }

    @Test
    void testGetMailsFromFolderBlankArguments() {
        assertThrows(IllegalArgumentException.class, () -> mailClient.getMailsFromFolder("", "/inbox"),
                "Account name cannot be empty or blank");
        assertThrows(IllegalArgumentException.class, () -> mailClient.getMailsFromFolder("name", "  "),
                "Folder path cannot be empty or blank");
    }

    @Test
    void testGetMailsFromFolderMissingAccount() {
        assertThrows(AccountNotFoundException.class, () -> mailClient.getMailsFromFolder("name", "/inbox"),
                "This account does not exist - expected AccountNotFoundException");
    }

    @Test
    void testGetMailsFromFolderMissingFolder() {
        mailClient.addNewAccount("pesho", "pesho02@gmail.com");

        assertThrows(FolderNotFoundException.class, () -> mailClient.getMailsFromFolder("pesho",
                        "/inbox/important"), "This folsder does not exist - expected AccountNotFoundException");
    }

    @Test
    void testGetMailsFromFolderExistingMails() {
        mailClient.addNewAccount("pesho", "pesho@gmail.com");
        mailClient.addNewAccount("gosho", "gosho@gmail.com");
        mailClient.addNewAccount("kiro", "kiro@gmail.com");

        final String mailMetadata1 = """
                sender: kiro@gmail.com
                subject: Hello, MJT!
                recipients: pesho@gmail.com, gosho@gmail.com,
                received: 2022-12-08 14:14
                """;

        final String mailMetadata2 = """
                sender: gosho@gmail.com
                subject: Hello, MJT!
                recipients: pesho@gmail.com, kiro@gmail.com,
                received: 2022-12-08 14:25
                """;

        final String mailContent = """
                Boys, let's test this method! :D
                """;

        Set<String> recipients1 = new HashSet<>();
        Set<String> recipients2 = new HashSet<>();
        recipients1.add("pesho@gmail.com");
        recipients1.add("gosho@gmail.com");
        recipients2.add("pesho@gmail.com");
        recipients2.add("kiro@gmail.com");

        Set<Mail> mails = new HashSet<>();
        mails.add(new Mail(new Account("kiro@gmail.com", "kiro"), recipients1,
                "Hello, MJT!", mailContent, LocalDateTime.of(2022, 12, 8, 14, 14)));
        mails.add(new Mail(new Account("gosho@gmail.com", "gosho"), recipients2,
                "Hello, MJT!", mailContent, LocalDateTime.of(2022, 12, 8, 14, 25)));

        mailClient.receiveMail("pesho", mailMetadata1, mailContent);
        mailClient.receiveMail("pesho", mailMetadata2, mailContent);

        Collection<Mail> received = mailClient.getMailsFromFolder("pesho", "/inbox");

        assertIterableEquals(mails, received, "Mails not received");
    }

    @Test
    void testSendMailNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> mailClient.sendMail(null, "metadata",
                "content"), "Account name cannot be null");
        assertThrows(IllegalArgumentException.class, () -> mailClient.sendMail("name", null,
                "content"), "Mail metadata cannot be null");
        assertThrows(IllegalArgumentException.class, () -> mailClient.sendMail("name", "metadata",
                null), "Mail content cannot be null");
    }

    @Test
    void testSendMailBlankArguments() {
        assertThrows(IllegalArgumentException.class, () -> mailClient.sendMail("", "metadata",
                "content"), "Account name cannot be empty or blank");
        assertThrows(IllegalArgumentException.class, () -> mailClient.sendMail("name", "  ",
                "content"), "Mail metadata cannot be empty or blank");
        assertThrows(IllegalArgumentException.class, () -> mailClient.sendMail("name", "metadata",
                "    "), "Mail content cannot be empty or blank");
    }

    @Test
    void testSendMailMissingAccount() {
        assertThrows(AccountNotFoundException.class, () -> mailClient.sendMail("name", "metadata",
                "content"), "The account is missing, so the mail must not be sent");
    }

    @Test
    void testSendMailInvalidMailMetadata() {
        mailClient.addNewAccount("kiro", "kiro@abv.bg");

        assertThrows(InvalidEmailMetadataException.class, () -> mailClient.sendMail("kiro", "metadata",
                "content"), "The metadata is invalid, so the mail must not be sent");
    }

    @Test
    void testSendMailIfMailIsAddedInSentFolder() {
        mailClient.addNewAccount("pesho", "pesho@gmail.com");
        mailClient.addNewAccount("gosho", "gosho@gmail.com");
        mailClient.addNewAccount("kiro", "kiro@gmail.com");

        final String mailMetadata = """
                sender: kiro@gmail.com
                subject: Hello, MJT!
                recipients: pesho@gmail.com, gosho@gmail.com,
                received: 2022-12-08 14:14
                """;

        final String mailContent = """
                hi, Pesho!
                hi, Gosho!
                
                from:
                    Kiro :)
                """;

        Set<String> recipients = new HashSet<>();
        recipients.add("pesho@gmail.com");
        recipients.add("gosho@gmail.com");

        Set<Mail> expectedSent = new HashSet<>();
        expectedSent.add(new Mail(new Account("kiro@gmail.com", "kiro"), recipients,
                "Hello, MJT!", mailContent, LocalDateTime.of(2022, 12, 8, 14, 14)));

        mailClient.sendMail("kiro", mailMetadata, mailContent);

        Collection<Mail> sent = mailClient.getMailsFromFolder("kiro", "/sent");

        assertIterableEquals(expectedSent, sent, "Mail not added in /sent folder");
    }

    @Test
    void testSendMailIfMailIsAddedInInboxFolders() {
        mailClient.addNewAccount("pesho", "pesho@gmail.com");
        mailClient.addNewAccount("gosho", "gosho@gmail.com");
        mailClient.addNewAccount("kiro", "kiro@gmail.com");

        final String mailMetadata = """
                sender: kiro@gmail.com
                subject: Hello, MJT!
                recipients: pesho@gmail.com, gosho@gmail.com, misho@gmail.com
                received: 2022-12-08 14:14
                """;

        final String mailContent = """
                hi, Pesho!
                hi, Gosho!
                
                from:
                    Kiro :)
                """;

        Set<String> recipients = new HashSet<>();
        recipients.add("pesho@gmail.com");
        recipients.add("gosho@gmail.com");
        recipients.add("misho@gmail.com");

        Set<Mail> expectedReceived = new HashSet<>();
        expectedReceived.add(new Mail(new Account("kiro@gmail.com", "kiro"), recipients,
                "Hello, MJT!", mailContent, LocalDateTime.of(2022, 12, 8, 14, 14)));

        mailClient.sendMail("kiro", mailMetadata, mailContent);

        Collection<Mail> receivedGosho = mailClient.getMailsFromFolder("gosho", "/inbox");
        Collection<Mail> receivedPesho = mailClient.getMailsFromFolder("pesho", "/inbox");

        assertIterableEquals(expectedReceived, receivedGosho, "Mail not added in /inbox folder");
        assertIterableEquals(expectedReceived, receivedPesho, "Mail not added in /inbox folder");
    }

    @Test
    void testSendMailExistingRuleApplied() {
        mailClient.addNewAccount("pesho", "pesho@gmail.com");
        mailClient.addNewAccount("gosho", "gosho@gmail.com");
        mailClient.addNewAccount("kiro", "kiro@gmail.com");

        mailClient.createFolder("pesho", "/inbox/important");

        final String ruleDefinition = """
                subject-includes: important
                subject-or-body-includes: method
                """;
        mailClient.addRule("pesho", "/inbox/important", ruleDefinition, 3);

        final String mailMetadata = """
                sender: kiro@gmail.com
                subject: Important! Test method!
                recipients: pesho@gmail.com, gosho@gmail.com,
                received: 2022-12-08 14:25
                """;

        final String mailContent = """
                Boys, let's test this method! :D
                """;

        Set<String> recipients = new HashSet<>();
        recipients.add("pesho@gmail.com");
        recipients.add("gosho@gmail.com");

        Set<Mail> expected = new HashSet<>();
        expected.add(new Mail(new Account("kiro@gmail.com", "kiro"), recipients,
                "Important! Test method!", mailContent, LocalDateTime.of(2022, 12, 8, 14, 25)));

        mailClient.sendMail("kiro", mailMetadata, mailContent);

        Collection<Mail> inboxPesho = mailClient.getMailsFromFolder("pesho", "/inbox");
        Collection<Mail> importantPesho = mailClient.getMailsFromFolder("pesho", "/inbox/important");
        Collection<Mail> inboxGosho = mailClient.getMailsFromFolder("gosho", "/inbox");

        assertIterableEquals(expected, importantPesho, "Mail not added to pesho's /inbox/important folder");
        assertTrue(inboxPesho.isEmpty(), "Mail not removed from pesho's /inbox folder");
        assertIterableEquals(expected, inboxGosho, "Mail not added to gosho's /inbox folder");
    }
}
