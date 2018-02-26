package com.travomate.tool

import com.travomate.Constants
import com.travomate.TripReview
import com.travomate.UserProfile
import com.travomate.dto.UserDTO
import com.travomate.dto.UserProfileDTO
/**
 * Created by mchopra on 1/12/2017.
 */
@Singleton(lazy = true)
class UserProfileDTOMapper {


    public UserProfileDTO[] mapUserProfileListToUserProfileDTOArray(List<UserProfile> userProfileList){
        if(userProfileList == null){
            return null
        }
        List<UserProfileDTO> userProfileDTOs = new ArrayList<UserProfileDTO>()
        for (UserProfile userProfile : userProfileList) {
            userProfileDTOs.add(mapUserProfileToUserProfileDTO(userProfile))
        }

        return userProfileDTOs.toArray()
    }

    public UserProfileDTO mapUserProfileToUserProfileDTO(UserProfile userProfile){
        if(userProfile == null){
            return null
        }

        UserDTO userDTO = new UserDTO()
        userDTO.id = userProfile.user.id
        userDTO.email = userProfile.user.email
        userDTO.contact = userProfile.user.contact
        userDTO.gender = userProfile.user.gender
        userDTO.dateOfBirth = userProfile.user.dateOfBirth
        userDTO.deviceId = userProfile.user.deviceId

        UserProfileDTO userProfileDTO = new UserProfileDTO()
        userProfileDTO.profileId = userProfile.id
        userProfileDTO.name = userProfile.name
        userProfileDTO.verificationStatus = userProfile.verificationStatus
        userProfileDTO.user = userDTO
        userProfileDTO.occupation = userProfile.occupation
        userProfileDTO.nationality = userProfile.nationality
        userProfileDTO.languages = userProfile.languages?.split(",")
        userProfileDTO.userIntro = userProfile.userIntro
        userProfileDTO.city = userProfile.city
        userProfileDTO.state = userProfile.state
        userProfileDTO.country = userProfile.country
        userProfileDTO.idProofLoc = userProfile.idProofLoc?.replace(Constants.IMAGE_ROOT_DIR,"/")
        userProfileDTO.coverImageLoc = userProfile.coverImageLoc?.replace(Constants.IMAGE_ROOT_DIR,"/")
        userProfileDTO.profileImageLoc = userProfile.profileImageLoc?.replace(Constants.IMAGE_ROOT_DIR,"/")
        userProfileDTO.tripReviewCount = TripReview.countByUser(userProfile.getUser())


        return userProfileDTO

    }
}
