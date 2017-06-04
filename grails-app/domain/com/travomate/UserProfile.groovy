package com.travomate

class UserProfile {

    User user
    String name
    String nationality
    String occupation
    String idProofLoc
    String languages
    String userIntro
    String profileImageLoc
    String coverImageLoc
    String city
    String state
    String country


    static constraints = {
        name(nullable: true)
        idProofLoc(nullable: true)
        nationality(nullable: true)
        languages(nullable: true)
        userIntro(nullable: true)
        profileImageLoc(nullable: true)
        coverImageLoc(nullable: true)
        occupation(nullable: true)
        city(nullable: true)
        state(nullable: true)
        country(nullable: true)
    }

    static mapping = {
        id generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence:'USER_PROFILE_SEQ']
    }

}
