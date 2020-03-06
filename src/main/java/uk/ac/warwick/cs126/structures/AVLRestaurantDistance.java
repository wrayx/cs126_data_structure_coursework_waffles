package uk.ac.warwick.cs126.structures;

import uk.ac.warwick.cs126.models.RestaurantDistance;

public class AVLRestaurantDistance extends AVLTreeStore<RestaurantDistance> {
    /**
     * sorted in ascending order of distance If they have the same Distance, then it
     * is sorted in ascending order of their ID.
     */
    public int customCompare(RestaurantDistance r1, RestaurantDistance r2) {
        if (r1.getDistance() == r2.getDistance())
            return r1.getRestaurant().getID().compareTo(r2.getRestaurant().getID());
        else
            return r1.getDistance() < r2.getDistance() ? -1 : 1;
    }
}
