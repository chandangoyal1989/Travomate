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
    String deviceId

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
        deviceId nullable: true
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (id == null || obj == null || getClass() != obj.getClass())
            return false;
        User that = (User) obj;
        return id.equals(that.id);
    }


    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
