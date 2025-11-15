package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private final Invoice invoice;
    private final Map<String, Play> plays;

    /**
     * Creates a StatementPrinter for the given invoice and plays.
     *
     * @param invoice the invoice to print, must not be null
     * @param plays   the map of play id to play, must not be null
     */

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */

    public String statement() {

        StringBuilder result = new StringBuilder(
                "Statement for " + invoice.getCustomer() + System.lineSeparator());

        for (Performance p : invoice.getPerformances()) {
            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(p).getName(),
                    usd(getAmount(p)),
                    p.getAudience()));
        }

        result.append(String.format("Amount owed is %s%n", usd(getTotalAmount())));
        result.append(String.format("You earned %s credits%n", getTotalVolumeCredits()));
        return result.toString();
    }
    /**
     * Returns the total amount for all performances in the invoice.
     *
     * @return the total amount in cents
     */

    private int getTotalAmount() {
        int totalAmount = 0;

        for (Performance p : invoice.getPerformances()) {
            totalAmount += getAmount(p);
        }
        return totalAmount;
    }
    /**
     * Returns the total volume credits over all performances.
     *
     * @return the total volume credits
     */

    private int getTotalVolumeCredits() {
        int volumeCredits = 0;

        for (Performance p : invoice.getPerformances()) {
            volumeCredits += getVolumeCredits(p);
        }
        return volumeCredits;
    }

    /**
     * Formats the given amount as US dollars.
     *
     * @param amount the amount in cents
     * @return the formatted dollar string
     */

    private static String usd(int totalAmount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(totalAmount / 100);
    }

    /**
     * Returns the volume credits for a single performance.
     *
     * @param performance the performance
     * @return the volume credits earned for this performance
     */

    private int getVolumeCredits(Performance performance) {
        int result = 0;
        result += Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        if ("comedy".equals(getPlay(performance).getType())) result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        return result;
    }
    /**
     * Returns the play associated with the given performance.
     *
     * @param performance the performance
     * @return the play for that performance
     */

    private Play getPlay(Performance p) {
        return plays.get(p.getPlayID());
    }

    /**
     * Returns the base amount for a single performance.
     *
     * @param performance the performance
     * @return the base amount in cents
     */

    private int getAmount(Performance performance) {
        int result = 0;
        switch (getPlay(performance).getType()) {
            case "tragedy":
                result = 40000;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += 1000 * (performance.getAudience() - 30);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", getPlay(performance).getType()));
        }
        return result;
    }
}
