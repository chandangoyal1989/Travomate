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

    public UserExpressionDTO[] mapUserExpressionListToUserExpressionDTOArray(List<UserExpression> userExpressionList) {
        List<UserExpressionDTO> UserExpressionDTOs = new ArrayList<UserExpressionDTO>()
        if (userExpressionList == null || userExpressionList?.isEmpty()) {
            return UserExpressionDTOs
        }


        for (UserExpression UserExpression : userExpressionList) {
            UserExpressionDTOs.add(mapUserExpressionToUserExpressionDTO(UserExpression))
        }

        return UserExpressionDTOs.toArray()
    }


    public UserExpressionDTO mapUserExpressionToUserExpressionDTO(UserExpression userExpression){
        if(userExpression == null){
            return null
        }

        User user = User.get(userExpression.userId)
        UserProfileDTO userProfileDTO = null
        if(user != null){
            UserProfile userProfile = UserProfile.findByUser(user)
            userProfileDTO = userProfileDTOMapper.mapUserProfileToUserProfileDTO(userProfile)
        }

        UserExpressionDTO userExpressionDTO = new UserExpressionDTO()
        userExpressionDTO.id = userExpression.id.toString()
        userExpressionDTO.imageLoc = userExpression.imageLoc?.replace(Constants.IMAGE_ROOT_DIR,"/")
        userExpressionDTO.user = userProfileDTO
        userExpressionDTO.description = userExpression.description
        userExpressionDTO.likeCount = Like.countByLikedObjectIdAndLikedObjectType(userExpression.id.toString(), Constants.USEREXRESSION_FEED_TYPE)
        userExpressionDTO.commentCount = Comment.countByPostIdAndPostTypeAndParentCommentIdIsNull(userExpression.id.toString(), Constants.USEREXRESSION_FEED_TYPE)

        return userExpressionDTO

    }
}
