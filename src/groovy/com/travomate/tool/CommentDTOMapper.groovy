package com.travomate.tool

import com.travomate.Comment
import com.travomate.User
import com.travomate.UserProfile
import com.travomate.dto.CommentDTO

/**
 * Created by mchopra on 5/13/2017.
 */
@Singleton(lazy = true)
class CommentDTOMapper {

    UserProfileDTOMapper userProfileDTOMapper = UserProfileDTOMapper.getInstance()

    public CommentDTO[] mapCommentListToCommentDTOArray(List<Comment> comentList){
        if(comentList == null){
            return
        }

        List<CommentDTO> commentDTOs = new ArrayList<CommentDTO>()
        for(Comment comment: comentList){
            commentDTOs.add(mapCommentToCommentDTO(comment))
        }

        return commentDTOs.toArray()
    }


    public CommentDTO mapCommentToCommentDTO(Comment comment){
        if(comment == null){
            return null
        }

        CommentDTO commentDTO = new CommentDTO()
        commentDTO.id = comment.id.toString()
        commentDTO.postId = comment.postId
        commentDTO.postDate = comment.postDate
        commentDTO.postedBy =  userProfileDTOMapper.mapUserProfileToUserProfileDTO(UserProfile.findByUser(User.get(comment.postedById)))
        commentDTO.commentText = comment.commentText
        commentDTO.parentCommentId = comment.parentCommentId

        return commentDTO

    }
}
