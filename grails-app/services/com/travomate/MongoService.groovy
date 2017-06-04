package com.travomate


import com.mongodb.BasicDBObject
import com.mongodb.BasicDBObjectBuilder
import com.mongodb.DB
import com.mongodb.DBAddress
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.Mongo
import com.mongodb.MongoClient
import org.bson.types.ObjectId

//import org.mongodb.morphia.Datastore
//import org.mongodb.morphia.Morphia


class MongoService {

//    Datastore datastore = null
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



//    def mongo
   ObjectId createOrModifyTravellerPost(def postParams, ObjectId postId){
       System.out.println("in saveTravellerPost")
//       def mongo = new GMongo()
//       def db = mongo.getDB("travomate")
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
           tp.postDescription = postParams.postDescription
           tp.userId = Long.parseLong(postParams.userId + "")
           tp.postTime = System.currentTimeMillis()
           tp = tp.save(flush: true, failOnError: true)

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
           tp.postDescription = postParams.postDescription != null ? postParams.postDescription : tp.postDescription
           tp.postTime = System.currentTimeMillis()
           tp = tp.save(flush: true, failOnError: true)
       }
        log.info("Saved Traveller Post : "+tp.id)
       return tp.id

//        def post = new TravellerPost(source:"delhi", destination: "Mathura", startDate: "29/01/2017", endDate: "15/02/2017")
//       def relevantPost = post.properties.findAll { !['class', 'metaClass'].contains(it.key) }

//       db.traveller_post.insert(source:"delhi", destination: "Mathura", startDate: "29/01/2017", endDate: "15/02/2017")
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
            gp.serviceTime = postParams.serviceTime
            gp.serviceDate = postParams.serviceDate
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
            gp.serviceTime = postParams.serviceTime ?: gp.serviceTime
            gp.serviceDate = postParams.serviceDate ?: gp.serviceDate
            gp.serviceDescription = postParams.serviceDescription ?: gp.serviceDescription
            gp.postDescription = postParams.postDescription ?: gp.postDescription
            gp.postTime = System.currentTimeMillis()
        }
        gp = gp.save(flush: true, failOnError: true)
        return gp.id
    }


    def deleteGuidePost(String postId){
        def objectPostId = new ObjectId(postId)
        GuidePost gp = GuidePost.get(objectPostId)
        gp.delete()
    }


    def saveUserLatLong(def postParams){
//        initiateMongo()

        setupMongo()
        Double[] location = [Double.parseDouble(postParams.longitude + ""), Double.parseDouble(postParams.latitude + "")];
        final BasicDBObject loc = new BasicDBObject("user_id", Long.parseLong(postParams.userId + ""));
        loc.put("location", location);
        getCollection().update(new BasicDBObject("user_id", Long.parseLong(postParams.userId + "")), loc, true, false);

        /*
        UserLocation userLocation = new UserLocation()
        userLocation.userId = Long.parseLong(postParams.userId + "")

        userLocation.location = [
                Double.parseDouble(postParams.longitude + ""),
                Double.parseDouble(postParams.latitude + "")
        ]
        datastore.save(userLocation)
        */




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
            //System.out.println("---- near venue: " + venue.get("name"));
            count++;
        }
//
//        DBObject obj = db.getCollection("restaurants").findOne(query)
//        DBCursor cursor = collection.find(query);
//        cursor.size()
        System.out.println("obj count"+ count)

    }


    private void addPlace(DBCollection collection, Long userId, double latLoc, double longLoc,final double[] location)
    {
        DBObject dbObject = new BasicDBObject([type: "Point", coordinates:[longLoc, latLoc]])
        DBObject userLocation = new BasicDBObject([userId:userId, latitude:latLoc, longitude:longLoc, location:dbObject])
        collection.insert(userLocation)

//        final BasicDBObject place = new BasicDBObject();
//        place.put("userId", userId);
//        place.put("latitude", latLoc);
//        place.put("longitude", longLoc);
//        place.put("location", location);
//        place.put("")
//        collection.insert(place);
    }


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
            notification.save(flush: true, failOnError: true)
        }
    }



    void sendNotification(String postId, def postParams, Constants.PostType postType){
        List<Long> notifiedUsersId = new ArrayList<Long>()
        Long userId = Long.parseLong(postParams.userId + "")
        Double[] location = [Double.parseDouble(postParams.longitude + ""), Double.parseDouble(postParams.latitude + "")]
        List<Long> nearUsers = nearSphereWIthMaxDistance(location)
        UserProfile userProfile = UserProfile.findByUser(User.get(userId))
        List<UserFriends> userFriendsList = UserFriends.findAllByProfileUser(userProfile)
        List<Long> friendUserIdList = userFriendsList?.profileUser?.user?.id
        List<TravellerPost> sameDestinationPost = TravellerPost.findAllByDestinationAndUserIdNotEqual(postParams.destination, userId)
        List<Long> sameDestinationUserList = sameDestinationPost.userId
        List<UserProfile> residentUsers = UserProfile.findAllByCityOrState(postParams.destination, postParams.destination)
        List<Long> residentUserId = residentUsers.user.id

        if(nearUsers != null && nearUsers?.size() > 0) {
            storeNotification(postId, postParams, nearUsers, Constants.NotificationType.NEARBY, postType)

        }
        if(friendUserIdList != null && friendUserIdList.size() > 0) {
            storeNotification(postId, postParams, friendUserIdList, Constants.NotificationType.FRIENDS, postType)

        }
        if(sameDestinationUserList != null && sameDestinationUserList.size() > 0){
            storeNotification(postId, postParams, sameDestinationUserList, Constants.NotificationType.SAME_DESTINATION, postType)
        }
        if(residentUserId != null && residentUserId.size() > 0){
            storeNotification(postId, postParams, residentUserId, Constants.NotificationType.NATIVE, postType)
        }

    }

    def getUserNotifications(Long userId){
        List<Notification> userNotifications = Notification.findAllByNotifiedUserId(userId)
        List<Notification> orderedUserNotifications = userNotifications.sort{it.postDate}.reverse(true)
        return orderedUserNotifications.take(Constants.TOP_NOTIFICATIONS_COUNT)
    }

    def getLatestTravellerFeeds(Integer offset){
        if(offset == null){
            offset = 0
        }
        def travellerPost = TravellerPost.createCriteria()
        def topPosts = travellerPost.list(max:Constants.TOP_POST_COUNT, offset:offset){
//            maxResults(Constants.TOP_POST_COUNT)
            order("postTime","desc")
        }

        return topPosts
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


    void saveComment(String postId, def postParams){
        Comment comment = new Comment()
        comment.postId = postId
        comment.commentText = postParams.commentText
        comment.postDate = System.currentTimeMillis()
        comment.postedBy = Long.parseLong(postParams.userId + "")
        comment.parentCommentId = Long.parseLong(postParams.parentCommentId + "")
        comment.save(flush:true, failOnError: true)
    }


    List<Comment> getCommentListForPost(String postId){
       return Comment.findAllByPostIdAndParentCommentIdIsNull(postId)
    }


    List<Like> getUserLikesForPost(String postId){
        return Like.findAllByLikedObjectIdAndLikedObjectType(postId, Constants.LIKE_POST_STRING)
    }


    void addUserLike(String likedObjectId, def postParams){
        Like userLike = new Like()
        userLike.likedBy = Long.parseLong(postParams.userId + "")
        userLike.likedOn = System.currentTimeMillis()
        userLike.likedObjectId = likedObjectId
        userLike.likedObjectType = postParams.likedObjectType.toLowerCase()  //can be either "post" or "comment"
        userLike.save(flush: true, failOnError: true)
    }


}




