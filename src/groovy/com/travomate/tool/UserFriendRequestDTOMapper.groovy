package com.travomate.tool

import com.travomate.UserFriendRequest
import com.travomate.dto.UserFriendRequestDTO

/**
 * Created by mchopra on 3/6/2017.
 */
@Singleton(lazy = true)
class UserFriendRequestDTOMapper {
    public UserFriendRequestDTO[] mapUserFriendRequestListToUserFriendRequestDTOArray(List<UserFriendRequest> userFriendRequestList){
        if(userFriendRequestList == null || userFriendRequestList?.isEmpty()){
            return null
        }

        List<UserFriendRequestDTO> userFriendRequestDTOList = new ArrayList<UserFriendRequestDTO>()
        for(UserFriendRequest userFriendRequest: userFriendRequestList){
            userFriendRequestDTOList.add(mapUserFriendRequestToUserFriendRequestDTO(userFriendRequest))
        }

        return userFriendRequestDTOList.toArray()
    }


    public UserFriendRequestDTO mapUserFriendRequestToUserFriendRequestDTO(UserFriendRequest userFriendRequest){
        if(userFriendRequest == null){
            return null
        }

        UserFriendRequestDTO userFriendRequestDTO = new UserFriendRequestDTO()
        userFriendRequestDTO.recipient = userFriendRequest.recipient
        userFriendRequestDTO.sender = userFriendRequest.sender
        userFriendRequestDTO.requestSent = userFriendRequest.requestSent

        return userFriendRequestDTO

    }

}
