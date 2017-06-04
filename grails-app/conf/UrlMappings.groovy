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

		"/api/verifyOTP" (controller: "restAPI") {
			action = [POST : "verifyOTP"]
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

		"/api/showFriendRequests/${profileUserId}" (controller: "restAPI") {
			action = [GET : "showFriendRequests"]
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

		"/api/travellerFeed/"(controller: "restAPI") {
			action = [POST : "postTravellerFeed"]
		}

		"/api/travellerFeed/list/${offset}"(controller: "restAPI") {
			action = [GET:"getTravellerFeeds"]
		}

		"/api/travellerFeed/${postId}"(controller: "restAPI") {
			action = [DELETE : "deleteTravellerFeed", PUT : "editTravellerPost"]
		}

		"/api/guideFeed" (controller: "restAPI") {
			action = [POST : "postGuideFeed"]
		}

		"/api/guideFeed/list/${offset}"(controller: "restAPI") {
			action = [GET:"getGuideFeeds"]
		}

		"/api/guideFeed/${postId}"(controller: "restAPI") {
			action = [DELETE : "deleteGuidePost", PUT : "modifyGuidePost"]
		}


		"/api/userLocation" (controller: "restAPI") {
			action = [POST: "storeUserLocation"]
		}

		"/api/findNearUsers" (controller: "restAPI") {
			action = [POST: "findNearUsers"]
		}

		"/api/notification/${profileUserId}" (controller: "restAPI"){
			action = [GET: "userNotification"]
		}

		"/api/search/${name}" (controller: "restAPI") {
			action = [GET: "searchUser"]
		}

		"/api/comment/${postId}"(controller: "restAPI") {
			action = [POST: "addComment", GET: "getComments"]
		}

		"/api/like/${objectId}"(controller: "restAPI") {
			action = [POST: "addPostLike", GET:"getPostLikes"]
		}

		"/api/resetPassword" (controller: "restAPI") {
			action = [POST: "resetPassword"]
		}

		"/api/tripreview/${userid}"(controller: "restAPI") {
			action = [GET: "getTripReview", POST: "saveTripReview"]
		}

		"/api/tripreview/uploadPic/${userid}/${tripReviewId}/${picType}"(controller: "restAPI") {
			action = [POST: "uploadTripReviewPic"]
		}

		"/api/logout"(controller: "restAPI") {
			action = [GET: "logout"]
		}
	}
}
