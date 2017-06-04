package com.travomate.tool

import com.travomate.Notification
import com.travomate.dto.NotificationDTO

/**
 * Created by mchopra on 3/22/2017.
 */

@Singleton(lazy = true)
class NotificationDTOMapper {

    public NotificationDTO[] mapNotificationListToNotificationDTOArray(List<Notification> notificationList){
        if(notificationList == null || notificationList?.isEmpty()){
            return null
        }

        List<NotificationDTO> notificationDTOs = new ArrayList<NotificationDTO>()
        for(Notification notification: notificationList){
            notificationDTOs.add(mapNotificationToNotificationDTO(notification))
        }

        return notificationDTOs.toArray()
    }


    public NotificationDTO mapNotificationToNotificationDTO(Notification notification){
        if(notification == null){
            return null
        }

        NotificationDTO notificationDTO = new NotificationDTO()
        notificationDTO.id = notification.id
        notificationDTO.postId = notification.postId
        notificationDTO.postDate = notification.postDate
        notificationDTO.postedBy = notification.postedBy
        notificationDTO.postStatus = notification.postStatus
        notificationDTO.notificationType = notification.notificationType
        notificationDTO.notifiedUserId = notification.notifiedUserId

        return notificationDTO

    }
}
