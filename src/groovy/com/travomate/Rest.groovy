package com.travomate

import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * Created by mchopra on 1/7/2017.
 */
public abstract class Rest {

    private static final logr = LogFactory.getLog(this)

    public def success(String message) {

        !logr.isDebugEnabled() ?: logr.debug(message)

        response.status = 200
        render (text:formatJson("{}", "success", "\"${message}\"", "200"), contentType:"application/json")

    }

    public def success(JSON data) {

        response.status = 200
        render (text:formatJson(data.toString(), "success", "\"\"", "200"), contentType:"application/json")

        data.setPrettyPrint(true)
        !logr.isDebugEnabled() ?: logr.debug(data.toString(true))
    }

    public def success(JSON data, String message, String code) {
        response.status = 200
        render (text:formatJson(data.toString(), "success", "\"${message}\"", "${code}"), contentType:"application/json")

        data.setPrettyPrint(true)
        !logr.isDebugEnabled() ?: logr.debug(data.toString(true))
    }

    public def success(JSON data,String message) {

        response.status = 200
        render (text:formatJson(data.toString(), "success", "\"${message}\"", "200"), contentType:"application/json")

        data.setPrettyPrint(true)
        !logr.isDebugEnabled() ?: logr.debug(data.toString(true))
    }

    public def error(JSON data,String message) {

        response.status = 200
        render (text:formatJson(data.toString(), "failure", "\"${message}\"", "400"), contentType:"application/json")

        data.setPrettyPrint(true)
        !logr.isDebugEnabled() ?: logr.debug(data.toString(true))
    }

    public def created(String message) {

        !logr.isDebugEnabled() ?: logr.debug(message)

        response.status = 200 //201
        render (text:formatJson("{}", "success", "\"${message}\"", "201"), contentType:"application/json")
    }

    public def created(JSON data, String message = "") {

        response.status = 200 //201
        render (text:formatJson(data.toString(), "success", "\"${message}\"", "201"), contentType:"application/json")

        data.setPrettyPrint(true)
        !logr.isDebugEnabled() ?: logr.debug(data.toString(true))
    }

    public def notFound(String message)
    {
        !logr.isWarnEnabled() ?: logr.warn(message)

        response.status = 200 //404
        render (text:formatJson("{}", "failure", "\"${message}\"", "404"), contentType:"application/json")
    }

    public def forbidden(String message) {

        !logr.isWarnEnabled() ?: logr.warn(message)

        response.status = 200 //403
        render (text:formatJson("{}", "failure", "\"${message}\"", "403"), contentType:"application/json")
    }

    public def error(String message)
    {
        logr.error(message)

        response.status = 500 //500
        render (text:formatJson("{}", "failure", "\"${message}\"", "500"), contentType:"application/json")
    }

    public Boolean validation( domain) {

        if (!domain.validate())
        {
            response.status = 200 //403
            String str = domain.errors.allErrors.collect {
                message(error:it,encodeAs:'HTML')
            } as JSON

            !logr.isWarnEnabled() ?: logr.warn(str.toString())

            render (text:formatJson("{}", "failure", str, "200"), contentType:"application/json")
            return false
        }
        return true
    }

    private formatJson(data, status, message, code)
    {
        return "{\"data\":${data},\"status\":\"${status}\",\"message\":${message},\"code\":\"${code}\"}"
    }

    static JSON createResponse(def data, String status, String msg, String code )
    {
        return ["data":data, "status":status, "message":msg, "code":code] as JSON
    }

    public def authenticationFailure(String message)
    {
        logr.error(message)

        response.status = 401
        render (text:formatJson("{}", "failure", "\"${message}\"", "500"), contentType:"application/json")
    }
}
