package com.travomate.tool

import com.travomate.*
import com.travomate.dto.GuidePostDTO
import com.travomate.dto.UserProfileDTO
/**
 * Created by mchopra on 3/31/2017.
 */

@Singleton(lazy = true)
class GuidePostDTOMapper {

    UserProfileDTOMapper userProfileDTOMapper = UserProfileDTOMapper.getInstance()

    public GuidePostDTO[] mapGuidePostListToGuidePostDTOArray(List<GuidePost> guidePostList) {
        List<GuidePostDTO> guidePostDTOs = new ArrayList<GuidePostDTO>()
        if (guidePostList == null || guidePostList?.isEmpty()) {
            return guidePostDTOs
        }


        for (GuidePost guidePost : guidePostList) {
            guidePostDTOs.add(mapGuidePostToGuidePostDTO(guidePost))
        }

        return guidePostDTOs.toArray()
    }


    public GuidePostDTO mapGuidePostToGuidePostDTO(GuidePost guidePost){
        if(guidePost == null){
            return null
        }

        User user = User.get(guidePost.userId)
        UserProfileDTO userProfileDTO = null
        if(user != null){
            UserProfile userProfile = UserProfile.findByUser(user)
            userProfileDTO = userProfileDTOMapper.mapUserProfileToUserProfileDTO(userProfile)
        }

        GuidePostDTO guidePostDTO = new GuidePostDTO()
        guidePostDTO.id = guidePost.id.toString()
        guidePostDTO.user = userProfileDTO
        guidePostDTO.place = guidePost.place
        guidePostDTO.serviceFromDate = guidePost.serviceFromDate
        guidePostDTO.serviceToDate = guidePost.serviceToDate
        guidePostDTO.serviceStartTime = guidePost.serviceStartTime
        guidePostDTO.serviceEndTime = guidePost.serviceEndTime
        guidePostDTO.serviceDescription = guidePost.serviceDescription
        guidePostDTO.postDescription = guidePost.postDescription
        guidePostDTO.likeCount = Like.countByLikedObjectIdAndLikedObjectType(guidePost.id.toString(), Constants.GUIDE_FEED_TYPE)
        guidePostDTO.commentCount = Comment.countByPostIdAndPostTypeAndParentCommentIdIsNull(guidePost.id.toString(), Constants.GUIDE_FEED_TYPE)

        return guidePostDTO

    }
}
