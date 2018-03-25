package com.travomate.tool

import com.travomate.*
import com.travomate.dto.UserExpressionDTO
import com.travomate.dto.UserProfileDTO

/**
 * Created by chandan on 3/24/2018.
 */

@Singleton(lazy = true)
class UserExpressionDTOMapper {

    UserProfileDTOMapper userProfileDTOMapper = UserProfileDTOMapper.getInstance()

    public UserExpressionDTO[] mapUserExpressionListToUserExpressionDTOArray(List<UserExpression> UserExpressionList) {
        List<UserExpressionDTO> UserExpressionDTOs = new ArrayList<UserExpressionDTO>()
        if (UserExpressionList == null || UserExpressionList?.isEmpty()) {
            return UserExpressionDTOs
        }


        for (UserExpression UserExpression : UserExpressionList) {
            UserExpressionDTOs.add(mapUserExpressionToUserExpressionDTO(UserExpression))
        }

        return UserExpressionDTOs.toArray()
    }


    public UserExpressionDTO mapUserExpressionToUserExpressionDTO(UserExpression UserExpression){
        if(UserExpression == null){
            return null
        }

        User user = User.get(UserExpression.userId)
        UserProfileDTO userProfileDTO = null
        if(user != null){
            UserProfile userProfile = UserProfile.findByUser(user)
            userProfileDTO = userProfileDTOMapper.mapUserProfileToUserProfileDTO(userProfile)
        }

        UserExpressionDTO UserExpressionDTO = new UserExpressionDTO()
        UserExpressionDTO.id = UserExpression.id.toString()
        UserExpressionDTO.user = userProfileDTO
        UserExpressionDTO.description = UserExpression.description
        UserExpressionDTO.likeCount = Like.countByLikedObjectIdAndLikedObjectType(UserExpression.id.toString(), Constants.FEED_TYPE)
        UserExpressionDTO.commentCount = Comment.countByPostIdAndPostTypeAndParentCommentIdIsNull(UserExpression.id.toString(), Constants.FEED_TYPE)

        return UserExpressionDTO

    }
}
