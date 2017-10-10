package com.travomate.tool

import com.travomate.Like
import com.travomate.User
import com.travomate.UserProfile
import com.travomate.dto.LikeDTO

/**
 * Created by mchopra on 5/16/2017.
 */

@Singleton(lazy = true)
class LikeDTOMapper {

    UserProfileDTOMapper userProfileDTOMapper = UserProfileDTOMapper.getInstance()

    public LikeDTO[] mapLikeListToLikeDTOArray(List<Like> userLikeList){
        if(userLikeList == null){
            return
        }

        List<LikeDTO> userLikeDTOs = new ArrayList<LikeDTO>()
        for(Like userLike: userLikeList){
            userLikeDTOs.add(mapLikeToLikeDTO(userLike))
        }

        return userLikeDTOs.toArray()
    }


    public LikeDTO mapLikeToLikeDTO(Like userLike){
        if(userLike == null){
            return
        }

        LikeDTO likeDTO = new LikeDTO()
        likeDTO.likedObjectType = userLike.likedObjectType
        likeDTO.likedOn = userLike.likedOn
        likeDTO.likedBy = userProfileDTOMapper.mapUserProfileToUserProfileDTO(UserProfile.findByUser(User.get(userLike.likedBy)))
        likeDTO.id = userLike.id.toString()
        likeDTO.likedObjectId = userLike.likedObjectId

        return likeDTO
    }
}
