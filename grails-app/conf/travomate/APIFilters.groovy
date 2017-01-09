package travomate

import grails.converters.JSON
import org.apache.log4j.MDC
import com.travomate.security.AuthenticatedSession

class APIFilters {

    def authenticationService

    def filters = {
        all(uri:'/api/**', uriExclude:'/api/login') {

            before = {
                log.info("api filter forwardURI : "+request.forwardURI)
                def postParams = JSON.parse(request.JSON.toString())
                log.info(" the post params "+postParams)
                if(!request.forwardURI.contains('/api/signup') && !(postParams.userid == null &&
                        (request.forwardURI.contains('/api/verifyContact') || request.forwardURI.contains('/api/verifyOTP')) )) {
                    log.info(params["sid"] + ", " + params["controller"] + ", " + actionName)

                    String sid = params["sid"]
                    AuthenticatedSession asession = AuthenticatedSession.findBySid(sid)
                    if (asession == null) {
                        redirect(controller: "restAPI", action: "handle")
                        return false
                    }
                    if(sid){
                        MDC.put("sid", sid)
                    }
                }

            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
    }
}
