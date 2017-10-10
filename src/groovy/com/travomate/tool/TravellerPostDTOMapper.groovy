package com.travomate.tool

import com.travomate.TravellerPost
import com.travomate.User
import com.travomate.UserProfile
import com.travomate.dto.TravellerPostDTO
import com.travomate.dto.UserProfileDTO

/**
 * Created by mchopra on 3/31/2017.
 */

@Singleton(lazy = true)
class TravellerPostDTOMapper {

    UserProfileDTOMapper userProfileDTOMapper = UserProfileDTOMapper.getInstance()

    public TravellerPostDTO[] mapTravellerPostListToTravellerPostDTOArray(List<TravellerPost> travellerPostList) {
        if (travellerPostList == null || travellerPostList?.isEmpty()) {
            return null
        }

        List<TravellerPostDTO> travellerPostDTOs = new ArrayList<TravellerPostDTO>()
        for (TravellerPost travellerPost : travellerPostList) {
            travellerPostDTOs.add(mapTravellerPostToTravellerPostDTO(travellerPost))
        }

        return travellerPostDTOs.toArray()
    }


    public TravellerPostDTO mapTravellerPostToTravellerPostDTO(TravellerPost travellerPost){
        if(travellerPost == null){
            return null
        }

        User user = User.get(travellerPost.userId)
        UserProfileDTO userProfileDTO = null
        if(user != null){
            UserProfile userProfile = UserProfile.findByUser(user)
            userProfileDTO = userProfileDTOMapper.mapUserProfileToUserProfileDTO(userProfile)
        }

        TravellerPostDTO travellerPostDTO = new TravellerPostDTO()
        travellerPostDTO.id = travellerPost.id.toString()
        travellerPostDTO.user = userProfileDTO
        travellerPostDTO.source = travellerPost.source
        travellerPostDTO.destination = travellerPost.destination
        travellerPostDTO.startDate = travellerPost.startDate
        travellerPostDTO.endDate = travellerPost.endDate
        travellerPostDTO.startTime = travellerPost.startTime
        travellerPostDTO.endTime = travellerPost.endTime
        travellerPostDTO.postDescription = travellerPost.postDescription

        return travellerPostDTO

    }
}
