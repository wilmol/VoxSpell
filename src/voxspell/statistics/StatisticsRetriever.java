package voxspell.statistics;

import net.sourceforge.calendardate.CalendarDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieves various statistics from the hidden statistics file.
 *
 * @author Will Molloy
 */
public class StatisticsRetriever extends StatisticsFileHandler {

    /**
     * Returns the statistics for the given category.
     *
     * @return an integer array with 2 elements: [ correctCount, incorrectCount ] for the given category
     */
    public int[] getStatsForCategory(String category) {
        scanner = getScannerForStatFile();
        int correct = 0;
        int incorrect = 0;

        while ((line = scannerReadLine()) != null) {
            String[] tokens = line.split("\\t");
            // Tokens: word (tab) correctCount (tab) incorrectCount (tab) category
            if (tokens.length == 4 && tokens[3].equals(category)) {
                correct += parseInt(tokens[1]);
                incorrect += parseInt(tokens[2]);
            }
        }
        return new int[]{correct, incorrect};
    }

    /**
     * Retrieves all of the statistics -
     * a 2d array of [ totalCorrect, totalIncorrect ] for all words/days/categories stored.
     */
    public int[] getLifeTimeStats() {
        int correct = 0;
        int incorrect = 0;
        scanner = getScannerForStatFile();
        while ((line = scannerReadLine()) != null) {
            String[] tokens = line.split("\\t");

            if (tokens.length == 4) {
                correct += parseInt(tokens[1]);
                incorrect += parseInt(tokens[2]);
            }
        }
        return new int[]{correct, incorrect};
    }

    /**
     * Returns the previous days of statistics (or all statistics if less than the given days..)
     *
     * @return a List of 2 element integer arrays: [ correctCount, incorrectCount] for each day of statistics
     * the List is ordered with the earliest day in index 0.
     * <p>
     */
    public List<String[]> getPrevDayStats(int days) {
        List<String[]> stats = new ArrayList<>();
        scanner = getScannerForStatFile();
        int correct = 0;
        int incorrect = 0;
        String date;
        String[] tokens = scannerReadLine().split("\\t");
        if (tokens.length > 0) {
            date = tokens[1]; // get first date
            String todaysDate = StatisticsFileHandler.todaysDate; // cache todays date

            // add in empty statistics to sync todays date with the last recorded date
            while (!todaysDate.equals(date) && stats.size() < days) {
                stats.add(new String[]{0 + "", 0 + "", todaysDate});
                todaysDate = getPrevDate(todaysDate); // adding in one 'empty' day at a time
            }

            while ((line = scannerReadLine()) != null) {
                if (stats.size() == days) {
                    break; // requested number of stats found
                }

                tokens = line.split("\\t");
                if (tokens.length == 4) {
                    correct += parseInt(tokens[1]);
                    incorrect += parseInt(tokens[2]);
                } else if (tokens.length == 2) {
                    stats.add(new String[]{correct + "", incorrect + "", date});    // record stat
                    correct = 0;
                    incorrect = 0;
                    String prevDate = getPrevDate(date);
                    date = tokens[1]; // get next date

                    // sync the next day with its previous date by adding in empty statistics
                    while (!prevDate.equals(date) && stats.size() < days) {
                        stats.add(new String[]{0 + "", 0 + "", prevDate});
                        prevDate = getPrevDate(prevDate);
                    }

                }
                if (!scanner.hasNextLine()) {
                    stats.add(new String[]{correct + "", incorrect + "", date});    // record stat
                    break; // EOF
                }
            }
        }

        // Ensure returned stats matches number of days by adding in empty statistics
        if (stats.size() != days) {
            String lastRecDate = stats.get(stats.size() - 1)[2];
            stats.addAll(addEmptyStats(lastRecDate, days - stats.size()));
        }

        return stats;
    }

    /**
     * Adds the specified amount of empty statistics (0 correct, 0 incorrect) for the number of days.
     */
    private List<String[]> addEmptyStats(String lastRecDate, int daysOfStatsToAdd) {
        List<String[]> emptyStats = new ArrayList<>();
        String date = lastRecDate;
        for (int i = 0; i < daysOfStatsToAdd; i++) {
            date = getPrevDate(date);
            emptyStats.add(new String[]{0 + "", 0 + "", date});
        }
        return emptyStats;
    }

    /**
     * Given a date in the format yyyy-MM-dd returns the previous date by one day
     */
    private String getPrevDate(String date) {
        String[] tokens = date.split("-");
        int year = parseInt(tokens[0]);
        int month = parseInt(tokens[1]);
        int day = parseInt(tokens[2]);

        CalendarDate calendarDate = new CalendarDate(year, month, day);
        calendarDate = calendarDate.addDays(-1);
        return formatCalendarDate(calendarDate);
    }

    private String formatCalendarDate(CalendarDate calendarDate) {
        String newYear = calendarDate.getYear() + "";
        String newMonth = calendarDate.getMonth() + "";
        String newDay = calendarDate.getDayOfMonth() < 10 ? "0" + calendarDate.getDayOfMonth() : calendarDate.getDayOfMonth() + "";
        return newYear + "-" + newMonth + "-" + newDay;
    }
}
