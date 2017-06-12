package com.travomate

class Role {

    String name

    static mapping = {
        cache true
        table  'role'
        id generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence:'ROLE_SEQ']
    }

    static constraints = {
        name blank: false, unique: true
    }
}
