package com.travomate.tool

import com.travomate.User
import com.travomate.dto.UserDTO

/**
 * Created by mchopra on 1/12/2017.
 */
@Singleton(lazy = true)
class UserDTOMapper {
    public UserDTO[] mapUserListToUserDTOArray(List<User> userList){
        if(userList == null || userList?.isEmpty()){
            return null
        }

        List<UserDTO> userDTOList = new ArrayList<UserDTO>()
        for(User user: userList){
            userDTOList.add(mapUserToUserDTO(user))
        }

        return userDTOList.toArray()
    }


    public UserDTO mapUserToUserDTO(User user){
        if(user == null){
            user
        }

        UserDTO userDTO = new UserDTO()
        userDTO.id = user.id
        userDTO.email = user.email
        userDTO.contact = user.contact
        userDTO.gender = user.gender
        userDTO.dateOfBirth = user.dateOfBirth

        return userDTO

    }
}
