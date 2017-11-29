package com.travomate;

/**
 * Created by mchopra on 3/21/2017.
 */
public class Constants {

    public enum NotificationStatus{
        SEEN,UNSEEN,UNREAD
    }

    public enum NotificationType{
        FRIENDS,NEARBY,SAME_DESTINATION,NATIVE
    }

    public enum PostType{
        TRAVELLER,GUIDE
    }
    public enum VerificationStatus{
        Pending,Rejected,Approved
    }

    public static final Integer TOP_NOTIFICATIONS_COUNT = 10;
    public static final Integer TOP_POST_COUNT = 20;
    public static final String PROFILE_TYPE_IMAGE = "profileImage";
    public static final String COVER_PIC_TYPE_IMAGE = "coverImage";
    public static final String ID_PROOF_TYPE_IMAGE = "idProof";
    public static final String TRIP_REVIEW_COVER_IMAGE = "tripReviewCover";
    public static final String TRIP_REVIEW_ALBUM = "tripReviewAlbum";
    public static final String OTP_MAIL_SOURCE_KEY = "email";
    public static final String OTP_PHONE_SOURCE_KEY = "phone";
    public static final String SMS_AUTH_KEY = "136393AhWl0TxW1586fd9de";
    public static final String SMS_SENDER_ID = "TMOTPI";//"GPSALT";//"ONESS-5432";
    public static final Integer SMS_COUNTRY_ID = 91;
    public static final String SMS_URL = "http://api.msg91.com/api/sendhttp.php?";

    public static final String IMAGE_BASE_DIR = "C:/usr/travomate/images/";
    public static final String IMAGE_ROOT_DIR = "C:/usr/travomate/";
    public static final String PROFILE_IMAGE_DIR = "profile_image";
    public static final String PROFILE_COVER_IMAGE_DIR = "cover_image";
    public static final String ID_PROOF_IMAGE_DIR = "id_proof_image";
    public static final String TRIP_REVIEW_IMAGE_DIR = "trip_review";
    public static final String FILE_PATH_DELIMITER = "/";

    public static final String LIKE_POST_STRING = "post";
    public static final String LIKE_COMMENT_STRING = "comment";

    public static final String RECIPIENT_FRIEND_REQUEST_API_PATH_STR = "recipient";
    public static final String SENDER_FRIEND_REQUEST_API_PATH_STR = "sender";
    public static final String TRIP_REVIEW_COVER_PIC_DIR = "cover_image";
    public static final String TRIP_REVIEW_ALBUM_DIR = "trip_review_album";


    public final static String AUTH_KEY_FCM = "AIzaSyBh5nInaMcXvaOvbAOs9oNNfn1BsC9OfUA";
    public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";


}
