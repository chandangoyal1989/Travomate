package com.travomate

class TripReview {

    User user;
    String title;
    String tripCoverPicLoc;
    String timeToVisit;
    String routeToTake;
    String tripDescription;

    static constraints = {
        user(nullable: true)
        title(nullable: true)
        tripCoverPicLoc(nullable: true)
        timeToVisit(nullable: true)
        routeToTake(nullable: true)
        tripDescription(type: 'text',nullable: true)
    }
}
