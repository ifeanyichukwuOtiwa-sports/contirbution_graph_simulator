package iwo.wintech.pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ContributionGraph {
    private static final Logger LOG = LoggerFactory.getLogger(ContributionGraph.class);
    private static final String NAME = System.getenv("git_name");
    private static final String EMAIL = System.getenv("email");

    public static void processContributionGraphPattern(final int[][] pattern, final int year) {
        LOG.info("Processing contribution graph pattern for {} started", NAME);

        // Initialize Git repository
        final InitCommand init = Git.init();
        final Path pathUri = Path.of("../repo_with_pattern_graph");


        try (final Git git = init.setDirectory(pathUri.toFile()).call()) {

            final Path filePath = pathUri.resolve("contribution_file_" + year + ".txt");

            Files.createDirectories(filePath.getParent());
            Files.createFile(filePath);

            // Create a calendar instance to manipulate dates
            final Calendar cal = Calendar.getInstance();
            // Set the calendar year to the specified year (e.g., 2020) and month to January 1st
            cal.set(year, Calendar.JANUARY, 1);

            // Adjust the calendar to the first Sunday of the year
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                cal.add(Calendar.DATE, 1); // Increment day until Sunday is reached
            }

            // Iterate over the weeks and days in the year according to the pattern
            for (int week = 0; week < 50; week++) {
                for (int day = 0; day < 7; day++) { // Using 50 weeks to leave room for potential off-by-one error

                    // Determine the number of commits to make on this day
                    final int commitCount = pattern[day][week];
                    for (int i = 0; i < commitCount; i++) {
                        final Date commitDate = cal.getTime();


                        final String formattedCommitDate = new SimpleDateFormat("yyyy-MM-dd").format(commitDate);
                        final String commitMsg = "Contributions on " + formattedCommitDate;

                        final String content = "Commit #" + (i + 1) + " on " + formattedCommitDate + "\n";

                        Files.writeString(filePath, content, StandardOpenOption.APPEND);


                        // Stage the file for committing
                        git.add().addFilepattern(filePath.getFileName().toString()).call();

                        // Create a PersonIdent instance with the commit author information
                        final PersonIdent committer = new PersonIdent(NAME, EMAIL, commitDate, cal.getTimeZone());

                        // Commit the file with the specified message, author, and committer
                        git.commit()
                                .setMessage(commitMsg)
                                .setAuthor(committer)
                                .setCommitter(committer)
                                .call();

                    }

                    cal.add(Calendar.DATE, 1);
                }
            }


        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            LOG.info("Contribution graph pattern has been created successfully!");
        }
    }
}
