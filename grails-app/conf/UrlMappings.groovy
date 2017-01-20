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

		"/api/upload/${userid}/${picType}" (controller: "restAPI") {
			action = [POST : "uploadPic"]
		}

		"/api/profile/${userid}" (controller: "restAPI") {
			action = [POST : "createProfile", GET:"showProfile", DELETE:"deleteProfile"]
		}

		"/api/photo/${userid}/${picType}" (controller: "restAPI") {
			action = [GET : "getImage"]
		}

		"/api/sendFriendRequest" (controller: "restAPI") {
			action = [POST : "sendFriendRequest"]
		}

		"/api/acceptFriendRequest" (controller: "restAPI") {
			action = [POST : "acceptFriendRequest"]
		}


		"/api/deleteFriendRequest/${senderId}/${recipientId}" (controller: "restAPI") {
			action = [DELETE : "deleteFriendRequest"]
		}


		"/api/deleteFriend/${profileUserId}/${toBeDeletedUserId}" (controller: "restAPI") {
			action = [DELETE : "deleteFriend"]
		}

		"/api/friends/${profileUserId}" (controller: "restAPI") {
			action = [GET : "getFriends"]
		}

		"/api/insertMongo"(controller: "restAPI") {
			action = [GET:"insertMongo"]
		}
	}
}
