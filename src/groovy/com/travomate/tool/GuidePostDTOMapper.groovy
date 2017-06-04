package com.travomate.tool

import com.travomate.GuidePost
import com.travomate.TravellerPost
import com.travomate.dto.GuidePostDTO
import com.travomate.dto.TravellerPostDTO

/**
 * Created by mchopra on 3/31/2017.
 */

@Singleton(lazy = true)
class GuidePostDTOMapper {

    public GuidePostDTO[] mapGuidePostListToGuidePostDTOArray(List<GuidePost> guidePostList) {
        if (guidePostList == null || guidePostList?.isEmpty()) {
            return null
        }

        List<GuidePostDTO> guidePostDTOs = new ArrayList<GuidePostDTO>()
        for (GuidePost guidePost : guidePostList) {
            guidePostDTOs.add(mapGuidePostToGuidePostDTO(guidePost))
        }

        return guidePostDTOs.toArray()
    }


    public GuidePostDTO mapGuidePostToGuidePostDTO(GuidePost guidePost){
        if(guidePost == null){
            return null
        }

        GuidePostDTO guidePostDTO = new GuidePostDTO()
        guidePostDTO.id = guidePost.id.toString()
        guidePostDTO.userId = guidePost.userId
        guidePostDTO.place = guidePost.place
        guidePostDTO.serviceDate = guidePost.serviceDate
        guidePostDTO.serviceTime = guidePost.serviceTime
        guidePostDTO.serviceDescription = guidePost.serviceDescription
        guidePostDTO.postDescription = guidePost.postDescription

        return guidePostDTO

    }
}
