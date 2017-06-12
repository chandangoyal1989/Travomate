package com.travomate

class UserRole implements Serializable{

    User user
    Role role
    static mapping = {
        id composite: ['role', 'user']
        table  'user_role'
        version false
    }
}
