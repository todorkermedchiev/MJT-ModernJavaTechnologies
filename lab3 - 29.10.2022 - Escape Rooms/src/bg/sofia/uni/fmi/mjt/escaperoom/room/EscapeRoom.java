package bg.sofia.uni.fmi.mjt.escaperoom.room;

import bg.sofia.uni.fmi.mjt.escaperoom.rating.Ratable;

public class EscapeRoom implements Ratable {
    private String name;
    private Theme theme;
    private Difficulty difficulty;
    private final int maxTimeToEscape;
    private final double priceToPlay;
    private final int maxReviewsCount;
    private Review[] reviews;
    private int reviewsCount;
    private double rating;

    public EscapeRoom(String name, Theme theme, Difficulty difficulty, int maxTimeToEscape, double priceToPlay,
                      int maxReviewsCount)
    {
        this.name = name;
        this.theme = theme;
        this.difficulty = difficulty;
        this.maxTimeToEscape = maxTimeToEscape;
        this.priceToPlay = priceToPlay;
        this.maxReviewsCount = maxReviewsCount;
        reviews = new Review[maxReviewsCount];
        reviewsCount = 0;
        rating = 0.0;
    }

    /**
     * Returns the name of the escape room.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the difficulty of the escape room.
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Returns the maximum time to escape the room.
     */
    public int getMaxTimeToEscape() {
        return maxTimeToEscape;
    }

    /**
     * Returns all user reviews stored for this escape room, in the order they have been added.
     */
    public Review[] getReviews() {
        Review[] newReviews = new Review[reviewsCount];
        System.arraycopy(reviews, 0, newReviews, 0, reviewsCount);
        return newReviews;
    }

    /**
     * Adds a user review for this escape room.
     * The platform keeps just the latest up to {@code maxReviewsCount} reviews and in case the capacity is full,
     * a newly added review would overwrite the oldest added one, so the platform contains
     * {@code maxReviewsCount} at maximum, at any given time. Note that, despite older reviews may have been
     * overwritten, the rating of the room averages all submitted review ratings, regardless of whether all reviews
     * themselves are still stored in the platform.
     *
     * @param review the user review to add.
     */
    public void addReview(Review review) {
        if (reviewsCount == maxReviewsCount) {
            for (int i = 0; i < reviewsCount - 1; ++i) {
                reviews[i] = reviews[i + 1];
            }
            reviews[reviewsCount - 1] = review;
        }
        else {
            reviews[reviewsCount] = review;
            ++reviewsCount;
        }

        rating = (rating * (reviewsCount - 1) + review.rating()) / reviewsCount;
    }

    /**
     * Returns the rating
     */
    @Override
    public double getRating() {
        return rating;
    }
}
