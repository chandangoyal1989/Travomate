package com.travomate.tool

import com.travomate.User
import com.travomate.UserProfile
import com.travomate.dto.UserDTO
import com.travomate.dto.UserProfileDTO

/**
 * Created by mchopra on 1/12/2017.
 */
@Singleton(lazy = true)
class UserProfileDTOMapper {

    public UserProfileDTO mapUserProfileToUserProfileDTO(UserProfile userProfile){
        if(userProfile == null){
            return null
        }

        UserProfileDTO userProfileDTO = new UserProfileDTO()
        userProfileDTO.id = userProfile.id
        userProfileDTO.name = userProfile.name
        userProfileDTO.email = userProfile.user.email
        userProfileDTO.contact = userProfile.user.contact
        userProfileDTO.gender = userProfile.user.gender
        userProfileDTO.dateOfBirth = userProfile.user.dateOfBirth
        userProfileDTO.occupation = userProfile.occupation
        userProfileDTO.nationality = userProfile.nationality
        userProfileDTO.languages = userProfile.languages
        userProfileDTO.userIntro = userProfile.userIntro


        return userProfileDTO

    }
}
