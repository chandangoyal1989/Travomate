package com.travomate


import com.mongodb.BasicDBObject
import com.mongodb.BasicDBObjectBuilder
import com.mongodb.DB
import com.mongodb.DBAddress
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.Mongo
import com.mongodb.MongoClient
import grails.converters.JSON
import org.bson.types.ObjectId
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION


class MongoService {

    static transactional = 'mongo'

    private static  Mongo _mongo;

    public   void setupMongo() throws Exception {
        _mongo = new Mongo(new DBAddress("127.0.0.1", 27017, "travomate"));
//        _mongo = new Mongo(new DBAddress("122.160.30.50", 27017, "travomate"));
        DBObject index2d = BasicDBObjectBuilder.start("location", "2d").get();
        DBCollection collection = getCollection();
        collection.ensureIndex(index2d);
    }

    private  DBCollection getCollection() {

        return _mongo.getDB("travomate").getCollection("user_location");
    }



    ObjectId createOrModifyTravellerPost(def postParams, ObjectId postId){
        log.info("in saveTravellerPost")
        TravellerPost tp = null
        if(postId != null){
            tp = TravellerPost.get(postId)
        }
        if(tp == null) {
            tp = new TravellerPost()
            tp.source = postParams.source
            tp.destination = postParams.destination
            tp.startDate = postParams.startDate
            tp.endDate = postParams.endDate
            tp.startTime = postParams.startTime
            tp.endTime = postParams.endTime
            tp.postDescription = postParams.postDescription
            tp.userId = Long.parseLong(postParams.userId + "")
            tp.postTime = System.currentTimeMillis()
            Double[] location = [Double.parseDouble(postParams.longitude + ""), Double.parseDouble(postParams.latitude + "")];
            tp.location = location
            tp = tp.save(failOnError: true)

        } else {
            //delete existing notifications and add new notifications
            if(postParams.destination != null){
                deleteNotifications(postId.toString())
                sendNotification(postId.toString(), postParams, Constants.PostType.TRAVELLER)
            }
            tp.source = postParams.source != null ? postParams.source : tp.source
            tp.destination = postParams.destination != null ? postParams.destination : tp.destination
            tp.startDate = postParams.startDate != null ? postParams.startDate : tp.startDate
            tp.endDate = postParams.endDate != null ? postParams.endDate : tp.endDate
            tp.startTime = postParams.startTime != null ? postParams.startTime : tp.startTime
            tp.endTime = postParams.endTime != null ? postParams.endTime : tp.endTime
            tp.postDescription = postParams.postDescription != null ? postParams.postDescription : tp.postDescription
            tp.postTime = System.currentTimeMillis()
            tp = tp.save(failOnError: true)
        }
        log.info("Saved Traveller Post : "+tp.id)
        return tp.id
    }


    def deleteTravellerPost(String postId){
        def objectPostId = new ObjectId(postId)
        TravellerPost tp = TravellerPost.get(objectPostId)
        tp.delete()
    }


    def deleteNotifications(String postId){
        List<Notification> notificationList = Notification.findAllByPostId(postId)
        notificationList?.each{ notification ->
            notification.delete()
        }
    }


    def createOrModifyGuidePost(def postParams, ObjectId postId){
        log.info("In saveGuidePost")
        GuidePost gp = null

        if(postId != null){
            gp = GuidePost.get(postId)
        }

        if(gp == null){
            gp = new GuidePost()
            gp.place = postParams.place
            gp.serviceStartTime = postParams.serviceStartTime
            gp.serviceEndTime = postParams.serviceEndTime
            gp.serviceFromDate = postParams.serviceFromDate
            gp.serviceToDate = postParams.serviceToDate
            gp.serviceDescription = postParams.serviceDescription
            gp.postDescription = postParams.postDescription
            gp.postTime = System.currentTimeMillis()
            gp.userId = Long.parseLong(postParams.userId + "")
        } else {
            //delete existing notifications and add new notifications
            if(postParams.serviceDescription != null){
                deleteNotifications(postId.toString())
                sendNotification(postId.toString(), postParams, Constants.PostType.GUIDE)
            }
            gp.place = postParams.place ?: gp.place
            gp.serviceStartTime = postParams.serviceStartTime ?: gp.serviceStartTime
            gp.serviceEndTime = postParams.serviceEndTime ?: gp.serviceEndTime
            gp.serviceFromDate = postParams.serviceFromDate ?: gp.serviceFromDate
            gp.serviceToDate = postParams.serviceToDate ?: gp.serviceToDate
            gp.serviceDescription = postParams.serviceDescription ?: gp.serviceDescription
            gp.postDescription = postParams.postDescription ?: gp.postDescription
            gp.postTime = System.currentTimeMillis()
        }
        gp = gp.save(failOnError: true)
        return gp.id
    }


    def deleteGuidePost(String postId){
        def objectPostId = new ObjectId(postId)
        GuidePost gp = GuidePost.get(objectPostId)
        gp.delete()
    }


    def saveUserLatLong(def postParams){
        //Connect to Mongo DB
        setupMongo()

        //Save location in db
        Double[] location = [Double.parseDouble(postParams.longitude + ""), Double.parseDouble(postParams.latitude + "")];
        System.out.println("Save user location:"+location);
        final BasicDBObject loc = new BasicDBObject("user_id", Long.parseLong(postParams.userId + ""));
        loc.put("location", location);
        getCollection().update(new BasicDBObject("user_id", Long.parseLong(postParams.userId + "")), loc, true, false);
    }


    def addLocation(final Long userId,  final double [] location){
        final BasicDBObject loc = new BasicDBObject("user_id", userId);
        loc.put("location", location);
        getCollection().update(new BasicDBObject("name", pName), loc, true, false);
    }

    def insertMongo(){
        MongoClient mongoClient = new MongoClient("localhost")
        log.info("insertMongo service &&")
        DB db = mongoClient.getDB("travomate")
        DBCollection collection = db.getCollection("user_location")


        Double longLoc = -73.93414657
        Double latLoc = 40.82302903
        Double maxDistance = 5 * 1609.34
        double[] loc = [longLoc, latLoc] ;
        collection.ensureIndex(new BasicDBObject("location", "2dsphere"), "geospatialIdx");
        BasicDBObject criteria = new BasicDBObject("\$nearSphere", loc);
        criteria.put("\$maxDistance", maxDistance);

        BasicDBObject query = new BasicDBObject("location", criteria);

        int count = 0;
        for (final DBObject venue : collection.find(query).toArray()) {
            count++;
        }
        System.out.println("obj count"+ count)

    }


    private void addPlace(DBCollection collection, Long userId, double latLoc, double longLoc,final double[] location)
    {
        DBObject dbObject = new BasicDBObject([type: "Point", coordinates:[longLoc, latLoc]])
        DBObject userLocation = new BasicDBObject([userId:userId, latitude:latLoc, longitude:longLoc, location:dbObject])
        collection.insert(userLocation)
    }


    /**
     * This method returns the list of users who are within 5km radius of a given location
     * @param location
     * @return
     */
    public  List<Long> nearSphereWIthMaxDistance(Double[] location) {
        setupMongo()
        List<Long> nearUsers = new ArrayList<Long>()
        //Double[] location = [-73.99171, 40.738868]
        final BasicDBObject filter = new BasicDBObject("\$nearSphere", location)

        //maxDistance = 5Km(convert it into radians : 5/6371) = 0.000784806153
        Double maxDistance = 0.000784806153
        filter.put("\$maxDistance", maxDistance)

        // Radius of the earth: 3959.8728
        final BasicDBObject query = new BasicDBObject("location", filter)

        int count = 0;
        for (final DBObject nearLocation : getCollection().find(query).toArray()) {
            log.info("---- near venue: " + nearLocation.get("user_id"))
            nearUsers.add(nearLocation.get("user_id"))
            count++
        }

        log.info("nearSphereWIthMaxDistance count : "+count)
        return nearUsers

    }

    /**
     * This methos stores notification details for a post in db
     * @param postId
     * @param postParams
     * @param notifiedUsersList
     * @param notificationType
     * @param postType
     * @return
     */
    def storeNotification(String postId, def postParams, List<Long> notifiedUsersList, Constants.NotificationType notificationType, Constants.PostType postType){
        notifiedUsersList?.each{ notifiedUserId ->
            Notification notification = new Notification()
            notification.postId = postId
            notification.postedBy = Long.parseLong(postParams.userId + "")
            notification.notifiedUserId = notifiedUserId
            notification.postStatus = Constants.NotificationStatus.UNSEEN.toString()
            notification.postDate = System.currentTimeMillis()
            notification.notificationType = notificationType.toString()
            notification.postType = postType.toString()
            notification.save(failOnError: true)
        }
    }


    /**
     * This method generates the list of users who will get the notification for a given post
     * @param postId
     * @param postParams
     * @param postType
     */
    void sendNotification(String postId, def postParams, Constants.PostType postType){
        log.info("sendNotification")
        List<Long> notifiedUsersId = new ArrayList<Long>()
        Long userId = Long.parseLong(postParams.userId + "")
        Double[] location = [Double.parseDouble(postParams.longitude + ""), Double.parseDouble(postParams.latitude + "")]
        List<Long> nearUsers = nearSphereWIthMaxDistance(location)
        UserProfile userProfile = UserProfile.findByUser(User.get(userId))
        log.info("sendNotification userProfile " + userProfile + " user : " + User.get(userId))
        List<UserFriends> userFriendsList = UserFriends.findAllByProfileUser(User.get(userId))
        List<Long> friendUserIdList = userFriendsList?.profileUser?.id
        List<TravellerPost> sameDestinationPost = TravellerPost.findAllByDestinationAndUserIdNotEqual(postParams.destination, userId)
        List<Long> sameDestinationUserList = sameDestinationPost.userId
        List<UserProfile> residentUsers = UserProfile.findAllByCityOrState(postParams.destination, postParams.destination)
        List<Long> residentUserId = residentUsers.user.id

        //Nearby users
        if(nearUsers != null && nearUsers?.size() > 0) {
            storeNotification(postId, postParams, nearUsers, Constants.NotificationType.NEARBY, postType)

        }

        //Friends
        if(friendUserIdList != null && friendUserIdList.size() > 0) {
            storeNotification(postId, postParams, friendUserIdList, Constants.NotificationType.FRIENDS, postType)

        }

        //USers going to the same place
        if(sameDestinationUserList != null && sameDestinationUserList.size() > 0){
            storeNotification(postId, postParams, sameDestinationUserList, Constants.NotificationType.SAME_DESTINATION, postType)
        }

        //Users staying at the destination
        if(residentUserId != null && residentUserId.size() > 0){
            storeNotification(postId, postParams, residentUserId, Constants.NotificationType.NATIVE, postType)
        }
    }

    def getUserNotifications(Long userId){
        List<Notification> userNotifications = Notification.findAllByNotifiedUserId(userId)
        List<Notification> orderedUserNotifications = userNotifications.sort{it.postDate}.reverse(true)
        return orderedUserNotifications.take(Constants.TOP_NOTIFICATIONS_COUNT)
    }

    /**
     * This method returns the list of traveller feeds from a given offset
     * @param offset
     * @return
     */
    def getLatestTravellerFeeds(Integer offset){
        if(offset == null){
            offset = 0
        }
        def travellerPost = TravellerPost.createCriteria()
        def topPosts = travellerPost.list(max:Constants.TOP_POST_COUNT, offset:offset){
            order("postTime","desc")
        }
        return topPosts
    }

    def getTravellerPostDestination(String destination){
        List<TravellerPost> travellerPostList = TravellerPost.findAllByDestination(destination)
        return travellerPostList
    }

    def getUserIdFromPostId(String postId){
        User user;
        TravellerPost travellerPost = TravellerPost.findById(postId)
        if(travellerPost.userId!=null) {
            user = User.get(travellerPost.userId);
        }
        else{
            GuidePost guidePost = GuidePost.findById(postId)
            user = User.get(guidePost.userId);
        }
        return user;
    }

    def getUserIdFromLikedObjectId(String likedObjectId,String likedObjectType){
        User user;
        if(likedObjectType.equals("comment")) {
            Comment comment = Comment.findById(likedObjectId)
            likedObjectId = comment.postId
        }
        TravellerPost travellerPost = TravellerPost.findById(likedObjectId)
        if(travellerPost.userId!=null) {
            user = User.get(travellerPost.userId);
        }
        else{
            GuidePost guidePost = GuidePost.findById(likedObjectId)
            user = User.get(guidePost.userId);
        }
        return user;
    }

    def getTopGuideFeeds(Integer offset){
        if(offset == null){
            offset = 0
        }
        def guidePost = GuidePost.createCriteria()
        def topPosts = guidePost.list(max:Constants.TOP_POST_COUNT, offset:offset){
            order("postTime","desc")
        }
        return topPosts
    }

    Comment saveComment(String commentId, def params, def postParams){
        log.info("post params " + postParams)
        String postId = params?.postId
        String postType = params?.postType
        Comment comment = null
        if(commentId != null) {
            comment = Comment.findById(new ObjectId(commentId))
        } else {
            comment = new Comment()
            comment.postId = postId
            comment.postType = postType
            comment.postDate = System.currentTimeMillis()

        }
        if(comment != null) {
            comment.commentText = postParams.commentText
            comment.postedById = postParams.postedById != null ? Long.parseLong(postParams.postedById + "") : null;
            comment.parentCommentId = postParams.parentCommentId
            comment.updatedOn = System.currentTimeMillis().toString()
            return comment.save(failOnError: true)
        } else {
            return null
        }
    }

    void deleteComment(String commentId){
        Comment comment = Comment.findById(new ObjectId(commentId))
        if(comment != null){
            comment.delete()
        }
    }


    void deleteLike(String likeId){
        Like like = Like.findById(new ObjectId(likeId))
        if(like != null){
            like.delete()
        }
    }

    List<Comment> getCommentListForPost(def params){
        String postId = params.postId
        String postType = params.postType
        return Comment.findAllByPostIdAndPostTypeAndParentCommentIdIsNull(postId, postType)
    }


    List<Comment> getRepliesForComment(String commentId){
        return Comment.findAllByParentCommentId(commentId)
    }


    List<Like> getUserLikes(String objectId, String objectType){
        return Like.findAllByLikedObjectIdAndLikedObjectType(objectId, objectType.toString())
    }


    Like saveLike(String likedObjectId, def postParams){
        Like userLike = new Like()
        userLike.likedBy = Long.parseLong(postParams.likedById + "")
        userLike.likedOn = System.currentTimeMillis()
        userLike.likedObjectId = likedObjectId
        userLike.likedObjectType = postParams.likedObjectType.toLowerCase()  //can be  "guide" ,"traveller" ,"comment"
        return userLike.save(failOnError: true)
    }


    def saveGuidePost(def postParams){
        GuidePost.withTransaction { status ->
            def guidePostId = createOrModifyGuidePost(postParams, null)
            sendNotification(guidePostId.toString(), postParams, Constants.PostType.GUIDE)
            return guidePostId;
        }

    }


    ObjectId saveTravellerPost(def postParams){
        TravellerPost.withTransaction { status ->
            ObjectId postId = createOrModifyTravellerPost(postParams, null)
            sendNotification(postId.toString(), postParams, Constants.PostType.TRAVELLER)
            return postId
        }
    }


    public def filterFeedByCity(String cityName,String feedType, Integer offset){
        def criteriaQuery = null;
        def topPosts = null;
        if(offset == null){
            offset = 0
        }
        if(Constants.GUIDE_FEED_TYPE.equalsIgnoreCase(feedType)){
            criteriaQuery = GuidePost.createCriteria()
            topPosts = criteriaQuery.list(max:Constants.TOP_POST_COUNT, offset:offset){
                ilike("place", "%${cityName}%")
                order("postTime","desc")
            }
        } else if(Constants.TRAVELLER_FEED_TYPE.equalsIgnoreCase(feedType)){
            criteriaQuery = TravellerPost.createCriteria()
            topPosts = criteriaQuery.list(max:Constants.TOP_POST_COUNT, offset:offset){
                or {
                    ilike("source", "%${cityName}%")
                    ilike("destination", "%${cityName}%")
                }
                order("postTime","desc")
            }
        }
        return topPosts;
    }

    public Double[] getUserLocation(Long userId)
    {
        log.info("getting user information.")
        setupMongo();
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("user_id", userId);
        DBCursor cursor = getCollection().find(whereQuery);
        cursor.hasNext()
        BasicDBObject document = (BasicDBObject) cursor.next();
        Double [] location = document.get("location");
        log.info("User Location:"+location[0]+"  "+location[1]);
        return location;
    }
}




