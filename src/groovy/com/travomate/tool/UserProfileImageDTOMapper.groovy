package com.travomate.tool

import com.travomate.UserProfileImage
import com.travomate.dto.UserDTO
import com.travomate.dto.UserProfileImageDTO

/**
 * Created by mchopra on 5/21/2017.
 */

@Singleton(lazy=true)
class UserProfileImageDTOMapper {

    public UserProfileImageDTO[] mapUserProfileImageListToDTOArray(List<UserProfileImage> userProfileImageList){
        if(userProfileImageList == null){
            return
        }

        List<UserProfileImageDTO> userProfileImageDTOs = new ArrayList<UserProfileImageDTO>()
        for (UserProfileImage userProfileImage : userProfileImageList) {
            userProfileImageDTOs.add(mapUserProfileImageToUserProfileImageDTO(userProfileImage))
        }

        return userProfileImageDTOs.toArray()
    }


    public UserProfileImageDTO mapUserProfileImageToUserProfileImageDTO(UserProfileImage userProfileImage){
        if(userProfileImage == null){
            return
        }

        UserDTO userDTO = new UserDTO()
        userDTO.id = userProfileImage.user.id
        userDTO.email = userProfileImage.user.email
        userDTO.contact = userProfileImage.user.contact
        userDTO.gender = userProfileImage.user.gender
        userDTO.dateOfBirth = userProfileImage.user.dateOfBirth

        UserProfileImageDTO userProfileImageDTO = new UserProfileImageDTO()
        userProfileImageDTO.user = userDTO
        userProfileImageDTO.inUse = userProfileImage.inUse
        userProfileImageDTO.imageLoc = userProfileImage.imageLoc
        userProfileImageDTO.imageType = userProfileImage.imageType
        userProfileImageDTO.lastUpdated = userProfileImage.lastUpdated

        return userProfileImageDTO


    }
}
