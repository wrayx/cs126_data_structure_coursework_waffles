package uk.ac.warwick.cs126.stores;

import uk.ac.warwick.cs126.interfaces.IReviewStore;
import uk.ac.warwick.cs126.models.Review;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.*;

import uk.ac.warwick.cs126.util.DataChecker;
import uk.ac.warwick.cs126.util.KeywordChecker;
import uk.ac.warwick.cs126.util.StringFormatter;

public class ReviewStore implements IReviewStore {

    private MyArrayList<Review> reviewArray;
    private DataChecker dataChecker;
    private MyArrayList<Long> blackListedReviewID;

    public ReviewStore() {
        // Initialise variables here
        reviewArray = new MyArrayList<>();
        dataChecker = new DataChecker();
        blackListedReviewID = new MyArrayList<>();
    }

    public Review[] loadReviewDataToArray(InputStream resource) {
        Review[] reviewArray = new Review[0];

        try {
            byte[] inputStreamBytes = IOUtils.toByteArray(resource);
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int lineCount = 0;
            String line;
            while ((line = lineReader.readLine()) != null) {
                if (!("".equals(line))) {
                    lineCount++;
                }
            }
            lineReader.close();

            Review[] loadedReviews = new Review[lineCount - 1];

            BufferedReader tsvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int reviewCount = 0;
            String row;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            tsvReader.readLine();
            while ((row = tsvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split("\t");
                    Review review = new Review(
                            Long.parseLong(data[0]),
                            Long.parseLong(data[1]),
                            Long.parseLong(data[2]),
                            formatter.parse(data[3]),
                            data[4],
                            Integer.parseInt(data[5]));
                    loadedReviews[reviewCount++] = review;
                }
            }
            tsvReader.close();

            reviewArray = loadedReviews;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return reviewArray;
    }

    public boolean addReview(Review review) {
        if (blackListedReviewID.contains(review.getID()) || !dataChecker.isValid(review)) {
            return false;
        }
        for (int i = 0; i < reviewArray.size(); i++) {
            if (reviewArray.get(i).getID().equals(review.getID())) {
                blackListedReviewID.add(review.getID());
                reviewArray.remove(reviewArray.get(i));
                return false;
            } else if (reviewArray.get(i).getCustomerID().equals(review.getCustomerID())
                    && reviewArray.get(i).getRestaurantID().equals(review.getRestaurantID())
                    && reviewArray.get(i).getDateReviewed().compareTo(review.getDateReviewed()) < 0) {

                blackListedReviewID.add(reviewArray.get(i).getID());
                reviewArray.remove(reviewArray.get(i));
                reviewArray.add(review);
                return true;
            }
        }
        reviewArray.add(review);
        return true;
    }

    public boolean addReview(Review[] reviews) {
        boolean res = true;
        for (Review review : reviews) {
            if (!this.addReview(review))
                res = false;
        }
        return res;
    }

    public Review getReview(Long id) {
        if (dataChecker.isValid(id)){
            for (int i = 0; i < reviewArray.size(); i++) {
                if (reviewArray.get(i).getID().equals(id)) {
                    return reviewArray.get(i);
                }
            }
        }
        return null;
    }

    public Review[] getReviews() {
        Review[] res = new Review[reviewArray.size()];
        for (int i = 0; i < reviewArray.size(); i++) {
            res[i] = reviewArray.get(i);
        }
        this.reviewArrayQuickSortByID(res);
        return res;
    }

    public void reviewArrayQuickSortByID(Review[] reviews) {
        reviewArrayQuickSort(reviews, "id", 0, reviews.length - 1);
    }

    public void reviewArrayQuickSortByDateReviewed(Review[] reviews){
        reviewArrayQuickSort(reviews, "dateReviewed", 0, reviews.length - 1);
    }

    public void reviewArrayQuickSortByRating(Review[] reviews){
        reviewArrayQuickSort(reviews, "rating", 0, reviews.length - 1);
    }

    public int idCompare(Review r1, Review r2) {
        return r1.getID().compareTo(r2.getID());
    }

    public int ratingCompare(Review r1, Review r2) {
        int ratingCompare = r2.getRating() - r1.getRating();
        if (ratingCompare == 0)
            return dateCompare(r1, r2);
        else
            return ratingCompare;
    }

    public int dateCompare(Review r1, Review r2) {
        int dateCompare = r2.getDateReviewed().compareTo(r1.getDateReviewed());
        if (dateCompare == 0)
            return idCompare(r1, r2);
        else
            return dateCompare;
    }

    public void reviewArrayQuickSort(Review[] reviews, String sortBy, int begin, int end) {
        if (begin < end) {
            int partitionIndex;
            Review pivot = reviews[end];

            int i = (begin - 1);

            for (int j = begin; j < end; j++) {

                int c = 0;

                if (sortBy.equalsIgnoreCase("id"))
                    c = idCompare(reviews[j], pivot);
                else if (sortBy.equalsIgnoreCase("dateReviewed"))
                    c = dateCompare(reviews[j], pivot);
                else if (sortBy.equalsIgnoreCase("rating"))
                    c = ratingCompare(reviews[j], pivot);

                if (c < 0) {

                    i++;

                    Review tmp = reviews[i];
                    reviews[i] = reviews[j];
                    reviews[j] = tmp;
                }
            }

            Review tmp = reviews[i + 1];
            reviews[i + 1] = reviews[end];
            reviews[end] = tmp;

            partitionIndex = i + 1;

            reviewArrayQuickSort(reviews, sortBy, begin, partitionIndex - 1);
            reviewArrayQuickSort(reviews, sortBy, partitionIndex + 1, end);
        }
    }

    public Review[] getReviewsByDate() {
        Review[] res = this.getReviews();
        this.reviewArrayQuickSortByDateReviewed(res);
        return res;
    }

    public Review[] getReviewsByRating() {
        Review[] res = this.getReviews();
        this.reviewArrayQuickSortByRating(res);
        return res;
    }

    public Review[] getReviewsByCustomerID(Long id) {
        if (!dataChecker.isValid(id))
            return new Review[0];
        MyArrayList<Review> resList = new MyArrayList<>();
        for (int i = 0; i < reviewArray.size(); i++) {
            if (reviewArray.get(i).getCustomerID().equals(id)) {
                resList.add(reviewArray.get(i));
            }
        }
        Review[] res = new Review[resList.size()];
        res = resList.toArray(res);
        this.reviewArrayQuickSortByDateReviewed(res);
        return res;
    }

    public Review[] getReviewsByRestaurantID(Long id) {
        if (!dataChecker.isValid(id))
            return new Review[0];
        MyArrayList<Review> resList = new MyArrayList<>();
        for (int i = 0; i < reviewArray.size(); i++) {
            if (reviewArray.get(i).getRestaurantID().equals(id)) {
                resList.add(reviewArray.get(i));
            }
        }
        Review[] res = new Review[resList.size()];
        res = resList.toArray(res);
        this.reviewArrayQuickSortByDateReviewed(res);
        return res;
    }

    public float getAverageCustomerReviewRating(Long id) {
        if (!dataChecker.isValid(id))
            return -1;
        int sum = 0;
        int cnt = 0;
        for (int i = 0; i < reviewArray.size(); i++) {
            if (reviewArray.get(i).getCustomerID().equals(id)) {
                sum += reviewArray.get(i).getRating();
                cnt++;
            }
        }
        if (cnt == 0)
            return 0.0f;
        return ((float) sum) / cnt;
    }

    public float getAverageRestaurantReviewRating(Long id) {
        if (!dataChecker.isValid(id))
            return -1;
        int sum = 0;
        int cnt = 0;
        for (int i = 0; i < reviewArray.size(); i++) {
            if (reviewArray.get(i).getRestaurantID().equals(id)) {
                sum += reviewArray.get(i).getRating();
                cnt++;
            }
        }
        if (cnt == 0)
            return 0.0f;
        return ((float) sum) / cnt;
    }

    public int[] getCustomerReviewHistogramCount(Long id) {
        if (!dataChecker.isValid(id))
            return new int[0];
        int[] res = new int[5];
        for (int i = 0; i < reviewArray.size(); i++) {
            if (reviewArray.get(i).getCustomerID().equals(id)) {
                res[reviewArray.get(i).getRating() - 1]++;
            }
        }
        return res;
    }

    public int[] getRestaurantReviewHistogramCount(Long id) {
        int[] res = new int[5];
        if (!dataChecker.isValid(id))
            return res;
        for (int i = 0; i < reviewArray.size(); i++) {
            if (reviewArray.get(i).getRestaurantID().equals(id)) {
                res[reviewArray.get(i).getRating() - 1]++;
            }
        }
        return res;
    }

    public Long[] getTopCustomersByReviewCount() {
        Long[] topCustomers = new Long[20];

        MyComparableArrayList<IDCounter> topCustomerReviewCnt = new MyComparableArrayList<>();
        for (int i = 0, j; i < reviewArray.size(); i++) {
            for (j = 0; j < topCustomerReviewCnt.size(); j++) {
                if (topCustomerReviewCnt.get(j).getIdentifier().equals(reviewArray.get(i).getCustomerID())) {
                    topCustomerReviewCnt.get(j).addCount();
                    if (topCustomerReviewCnt.get(j).getLatestReviewDate().compareTo(reviewArray.get(i).getDateReviewed()) < 0)
                        topCustomerReviewCnt.get(j).setLatestReviewDate(reviewArray.get(i).getDateReviewed());

                    break;
                }
            }

            if (j == topCustomerReviewCnt.size()) {
                IDCounter customerFavourite = new IDCounter(reviewArray.get(i).getCustomerID(), reviewArray.get(i).getDateReviewed());
                topCustomerReviewCnt.add(customerFavourite);
            }
        }
        topCustomerReviewCnt.quicksort(0, topCustomerReviewCnt.size() - 1);
        for (int i = 0; i < topCustomers.length && i < topCustomerReviewCnt.size(); i++) {
            topCustomers[i] = topCustomerReviewCnt.get(i).getIdentifier();
            System.out.println(topCustomerReviewCnt.get(i).getIdentifier() + " - " + topCustomerReviewCnt.get(i).getCount());
        }

        return topCustomers;
    }

    public Long[] getTopRestaurantsByReviewCount() {
        Long[] topRestaurants = new Long[20];

        MyComparableArrayList<IDCounter> topRestaurantReviewCnt = new MyComparableArrayList<>();
        for (int i = 0, j; i < reviewArray.size(); i++) {
            for (j = 0; j < topRestaurantReviewCnt.size(); j++) {
                if (topRestaurantReviewCnt.get(j).getIdentifier().equals(reviewArray.get(i).getRestaurantID())) {
                    topRestaurantReviewCnt.get(j).addCount();
                    if (topRestaurantReviewCnt.get(j).getLatestReviewDate().compareTo(reviewArray.get(i).getDateReviewed()) < 0)
                        topRestaurantReviewCnt.get(j).setLatestReviewDate(reviewArray.get(i).getDateReviewed());

                    break;
                }
            }

            if (j == topRestaurantReviewCnt.size()) {
                IDCounter customerFavourite = new IDCounter(reviewArray.get(i).getRestaurantID(), reviewArray.get(i).getDateReviewed());
                topRestaurantReviewCnt.add(customerFavourite);
            }
        }
        topRestaurantReviewCnt.quicksort(0, topRestaurantReviewCnt.size() - 1);
        for (int i = 0; i < topRestaurants.length && i < topRestaurantReviewCnt.size(); i++) {
            topRestaurants[i] = topRestaurantReviewCnt.get(i).getIdentifier();
            System.out.println(topRestaurantReviewCnt.get(i).getIdentifier() + " - " + topRestaurantReviewCnt.get(i).getCount());
        }

        return topRestaurants;
    }

    public Long[] getTopRatedRestaurants() {
        MyComparableArrayList<Rating> ratings = new MyComparableArrayList<>();
        Long[] res = new Long[20];
        for (int i = 0, j; i < reviewArray.size(); i++) {
            for (j = 0; j < ratings.size(); j++) {
                if (ratings.get(j).getId().equals(reviewArray.get(i).getRestaurantID())) {
                    ratings.get(j).addCnt();
                    ratings.get(j).addSumRating(reviewArray.get(i).getRating());
//                     System.out.println(ratings.get(j).getId() + " - " + ratings.get(j).getAverageRating());
                    if (ratings.get(j).getLatestReviewDate().compareTo(reviewArray.get(i).getDateReviewed()) < 0)
                        ratings.get(j).setLatestReviewDate(reviewArray.get(i).getDateReviewed());

                    break;
                }
            }

            if (j == ratings.size()) {
                Rating rating = new Rating(reviewArray.get(i).getRestaurantID(), reviewArray.get(i).getDateReviewed(), reviewArray.get(i).getRating());
                ratings.add(rating);
            }
        }
        ratings.quicksort(0, ratings.size() - 1);
        for (int i = 0; i < res.length && i < ratings.size(); i++) {
            res[i] = ratings.get(i).getId();
        }

        return res;
    }

    public String[] getTopKeywordsForRestaurant(Long id) {
        String[] res = new String[5];
        Review[] restaurantReviews = this.getReviewsByRestaurantID(id);
        StringBuilder reviewString = new StringBuilder();
        for (Review r : restaurantReviews) {
            if (r.getRestaurantID().equals(id))
                reviewString.append(r.getReview());
        }
        String[] reviewWords = reviewString.toString().split("\\W+");
        MyComparableArrayList<Counter<String>> keywordList = new MyComparableArrayList<>();
        KeywordChecker keywordChecker = new KeywordChecker();
        for (int j = 0, i; j < reviewWords.length; j++) {
            if (keywordChecker.isAKeyword(reviewWords[j])) {
                for (i = 0; i < keywordList.size(); i++) {
                    if (keywordList.get(i).getIdentifier().equalsIgnoreCase(reviewWords[j])) {
                        keywordList.get(i).addCount();
                        break;
                    }
                }
                if (i == keywordList.size()) {
                    Counter<String> w = new Counter<String>(reviewWords[j]);
                    keywordList.add(w);
                }
            }
        }
        keywordList.quicksort(0, keywordList.size() - 1);
        for (int i = 0; i < res.length && i < keywordList.size(); i++) {
            res[i] = keywordList.get(i).getIdentifier();
            System.out.println("[cnt = " + keywordList.get(i).getCount() + "] Word: " + keywordList.get(i).getIdentifier());
        }
        return res;
    }

    public Review[] getReviewsContaining(String searchTerm) {
        // String searchTermConverted = stringFormatter.convertAccents(searchTerm);
        String searchTermConvertedFaster = StringFormatter.convertAccentsFaster(searchTerm.replaceAll("\\s+", " "));
        searchTermConvertedFaster = searchTermConvertedFaster.trim();
        if (searchTermConvertedFaster.length() == 0) {
            return new Review[0];
        }
        MyArrayList<Review> resList = new MyArrayList<>();
        for (int i = 0; i < reviewArray.size(); i++) {
            if (reviewArray.get(i).getReview().toLowerCase().contains(searchTermConvertedFaster.toLowerCase())) {
                resList.add(reviewArray.get(i));
            }
        }
        Review[] res = new Review[resList.size()];
        for (int i = 0; i < resList.size(); i++) {
            res[i] = resList.get(i);
        }
        this.reviewArrayQuickSortByDateReviewed(res);
        for (Review r : res){
            System.out.println(String.format("Date: %19s",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(r.getDateReviewed())) + ", review: " + r.getReview());
        }
        return res;
    }
}
