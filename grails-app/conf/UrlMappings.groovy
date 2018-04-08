class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: "login", action: "auth")

//		"/"(view:"/index")
        "500"(view: '/error')

        "/api/login"(controller: "restAPI") {
            action = [POST: "login"]
        }

        "/api/device"(controller: "restAPI") {
            action = [POST: "registerDevice"]
        }

        "/api/signup"(controller: "restAPI") {
            action = [POST: "signup"]
        }

        "/api/verifyMail"(controller: "restAPI") {
            action = [POST: "sendMail"]
        }

        "/api/verifyContact"(controller: "restAPI") {
            action = [POST: "sendMsg"]
        }

        "/api/verifyOTP"(controller: "restAPI") {
            action = [POST: "verifyOTP"]
        }

        "/api/upload/${userid}/${picType}"(controller: "restAPI") {
            action = [POST: "uploadPic"]
        }

        "/api/profile/${userid}"(controller: "restAPI") {
            action = [POST: "createProfile", GET: "showProfile", DELETE: "deleteProfile"]
        }

        "/api/photo/${userid}/${picType}"(controller: "restAPI") {
            action = [GET: "getImage"]
        }

        "/api/sendFriendRequest"(controller: "restAPI") {
            action = [POST: "sendFriendRequest"]
        }

        "/api/acceptFriendRequest"(controller: "restAPI") {
            action = [POST: "acceptFriendRequest"]
        }


        "/api/deleteFriendRequest/${senderId}/${recipientId}"(controller: "restAPI") {
            action = [DELETE: "deleteFriendRequest"]
        }

        "/api/showFriendRequests/${profileUserId}/${requestType}"(controller: "restAPI") {
            action = [GET: "showFriendRequests"]
        }


        "/api/deleteFriend/${profileUserId}/${toBeDeletedUserId}"(controller: "restAPI") {
            action = [DELETE: "deleteFriend"]
        }

        "/api/friends/${profileUserId}"(controller: "restAPI") {
            action = [GET: "getFriends"]
        }

        "/api/insertMongo"(controller: "restAPI") {
            action = [GET: "insertMongo"]
        }

        "/api/travellerFeed/"(controller: "restAPI") {
            action = [POST: "postTravellerFeed"]
        }

        "/api/travellerFeed/list/${offset}"(controller: "restAPI") {
            action = [GET: "getTravellerFeeds"]
        }

        "/api/travellerFeed/${postId}"(controller: "restAPI") {
            action = [DELETE: "deleteTravellerFeed", PUT: "editTravellerPost"]
        }

        "/api/guideFeed"(controller: "restAPI") {
            action = [POST: "postGuideFeed"]
        }

        "/api/guideFeed/list/${offset}"(controller: "restAPI") {
            action = [GET: "getGuideFeeds"]
        }

        "/api/guideFeed/${postId}"(controller: "restAPI") {
            action = [DELETE: "deleteGuidePost", PUT: "modifyGuidePost"]
        }


        "/api/userLocation"(controller: "restAPI") {
            action = [POST: "storeUserLocation"]
        }

        "/api/findNearUsers"(controller: "restAPI") {
            action = [POST: "findNearUsers"]
        }

        "/api/notification/${profileUserId}"(controller: "restAPI") {
            action = [GET: "userNotification"]
        }

        "/api/search/${name}"(controller: "restAPI") {
            action = [GET: "searchUser"]
        }

        "/api/comment/${postId}/${postType}"(controller: "restAPI") {
            action = [POST: "addComment", GET: "getComments"]
        }

        "/api/comment/edit/${commentId}"(controller: "restAPI") {
            action = [PUT: "editComment", DELETE: "deleteComment"]
        }

        "/api/reply/${commentId}"(controller: "restAPI") {
            action = [GET: "getReplies"]
        }

        "/api/like/${objectId}"(controller: "restAPI") {
            action = [POST: "addPostLike", GET: "getPostLikes"]
        }


        "/api/like/${objectId}/${objectType}"(controller: "restAPI") {
            action = [GET: "getLikesForAnObject"]
        }


        "/api/like/edit/${likeId}"(controller: "restAPI") {
            action = [DELETE: "deleteLike"]
        }

        "/api/resetPassword"(controller: "restAPI") {
            action = [POST: "resetPassword"]
        }

        "/api/tripreview/${userid}/list"(controller: "restAPI") {
            action = [GET: "getTripReview"]
        }

        "/api/tripreview/${userid}/save"(controller: "restAPI") {
            action = [POST: "saveTripReview"]
        }

        "/api/tripreview/${tripReviewId}"(controller: "restAPI") {
            action = [PUT: "editTripReview", DELETE: "deleteTripReview"]
        }

        "/api/tripreview/deleteImage/${imageId}"(controller: "restAPI") {
            action = [DELETE: "deleteTripReviewImage"]
        }

        "/api/tripreview/uploadPic/${userid}/${tripReviewId}/${title}/${picType}"(controller: "restAPI") {
            action = [POST: "uploadTripReviewPic"]
        }

        "/api/alltripreview/list"(controller: "restAPI") {
            action = [GET: "getAllTripReview"]
        }

        "/api/userExpression/${userId}/save"(controller: "restAPI") {
            action = [POST: "postUserExpression"]
        }

        "/api/userExpression/list/${offset}"(controller: "restAPI") {
            action = [GET: "getUserExpressions"]
        }

        "/api/userExpression/${postId}/${userId}"(controller: "restAPI") {
            action = [POST: "editUserExpression"]
        }

        "/api/userExpression/${postId}"(controller: "restAPI") {
            action = [DELETE: "deleteUserExpression", GET: "getUserExpression"]
        }

        "/api/testAPI"(controller: "restAPI") {
            action = [POST: "testAPI"]
        }

        "/api/searchFeed/${cityName}/${feedType}/${offset}"(controller: "restAPI") {
            action = [GET: "searchFeed"]
        }

        "/api/logout"(controller: "restAPI") {
            action = [GET: "logout"]
        }
    }
}
