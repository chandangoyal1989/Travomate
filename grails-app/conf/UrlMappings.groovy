class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')

		"/api/login" (controller: "restAPI") {
			action = [POST: "login"]
		}

		"/api/signup" (controller: "restAPI") {
			action = [POST: "signup"]
		}

		"/api/verifyMail" (controller: "restAPI") {
			action = [POST : "sendMail"]
		}

		"/api/verifyContact" (controller: "restAPI") {
			action = [POST : "sendMsg"]
		}
	}
}
