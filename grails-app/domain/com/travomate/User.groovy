package com.travomate

class User {

    transient springSecurityService

    String username
    String password
    String firstName
    String lastName
    String email
    String contact
    String externalAuthServiceName
    String gender
    String dateOfBirth
    boolean isExternalLogin
    boolean enabled
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    boolean isContactVerified
    boolean isEmailVerified

    static constraints = {
        firstName nullable: true
        lastName nullable: true
        email nullable: true, email: true
        contact nullable: true, unique: true
        username nullable: true
        password nullable: true
        externalAuthServiceName nullable: true
        gender nullable: true
        dateOfBirth nullable: true
        isExternalLogin nullable:true

    }

    static mapping = {
        password column: '`password`'
        id generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence:'USER_SEQ']
        isEmailVerified defaultValue:false
        isContactVerified defaultValue:false
        isExternalLogin defaultValue:false
    }

    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect { it.role } as Set
    }

    def beforeInsert() {
        encodePassword()
    }

    def beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
    }

    protected void encodePassword() {
        password = springSecurityService.encodePassword(password)
    }
}
