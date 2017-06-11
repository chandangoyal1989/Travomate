package com.travomate

import org.apache.commons.io.FilenameUtils
import org.springframework.dao.DataIntegrityViolationException

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class UserProfileController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [userProfileInstanceList: UserProfile.list(params), userProfileInstanceTotal: UserProfile.count()]
    }

    def create() {
        [userProfileInstance: new UserProfile(params)]
    }

    def save() {
        def userProfileInstance = new UserProfile(params)
        if (!userProfileInstance.save(flush: true)) {
            render(view: "create", model: [userProfileInstance: userProfileInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'userProfile.label', default: 'UserProfile'), userProfileInstance.id])
        redirect(action: "show", id: userProfileInstance.id)
    }

    def show(Long id) {
        def userProfileInstance = UserProfile.get(id)
        if (!userProfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'userProfile.label', default: 'UserProfile'), id])
            redirect(action: "list")
            return
        }

        [userProfileInstance: userProfileInstance]
    }

    def edit(Long id) {
        def userProfileInstance = UserProfile.get(id)
        if (!userProfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'userProfile.label', default: 'UserProfile'), id])
            redirect(action: "list")
            return
        }

        [userProfileInstance: userProfileInstance,verificationStatus:Constants.VerificationStatus]
    }

    def update(Long id, Long version) {
        def userProfileInstance = UserProfile.get(id)
        if (!userProfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'userProfile.label', default: 'UserProfile'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (userProfileInstance.version > version) {
                userProfileInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'userProfile.label', default: 'UserProfile')] as Object[],
                          "Another user has updated this UserProfile while you were editing")
                render(view: "edit", model: [userProfileInstance: userProfileInstance])
                return
            }
        }

        userProfileInstance.properties = params

        if (!userProfileInstance.save(flush: true)) {
            render(view: "edit", model: [userProfileInstance: userProfileInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'userProfile.label', default: 'UserProfile'), userProfileInstance.id])
        redirect(action: "show", id: userProfileInstance.id)
    }

    def delete(Long id) {
        def userProfileInstance = UserProfile.get(id)
        if (!userProfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'userProfile.label', default: 'UserProfile'), id])
            redirect(action: "list")
            return
        }

        try {
            userProfileInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'userProfile.label', default: 'UserProfile'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'userProfile.label', default: 'UserProfile'), id])
            redirect(action: "show", id: id)
        }
    }

    def renderImage = {

        log.info("render image ")
        def userProfile = UserProfile.get(params.id)

        String imageLoc
        if(params.imageType=='profile')
            imageLoc = userProfile.profileImageLoc
        if(params.imageType=='idProof')
            imageLoc = userProfile.idProofLoc

        log.info("profileImageLoc "+imageLoc)

        File imageFile =new File(imageLoc);
        String extension = FilenameUtils.getExtension(imageLoc)
        FilenameUtils.getName(imageLoc)
        log.info("extensions "+extension)
        BufferedImage originalImage=ImageIO.read(imageFile);

        ByteArrayOutputStream baos=new ByteArrayOutputStream();

        ImageIO.write(originalImage, extension, baos );

        byte[] imageInByte=baos.toByteArray();

        response.setHeader('Content-length', imageInByte.length.toString())

        response.contentType = 'image/jpg' // or the appropriate image content type

        response.outputStream << imageInByte
        response.outputStream.flush()

    }
}
