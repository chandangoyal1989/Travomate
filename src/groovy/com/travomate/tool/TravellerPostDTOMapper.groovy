package com.travomate.tool

import com.travomate.TravellerPost
import com.travomate.dto.TravellerPostDTO

/**
 * Created by mchopra on 3/31/2017.
 */

@Singleton(lazy = true)
class TravellerPostDTOMapper {

    public TravellerPostDTO[] mapTravellerPostListToTravellerPostDTOArray(List<TravellerPost> travellerPostList) {
        if (travellerPostList == null || travellerPostList?.isEmpty()) {
            return null
        }

        List<TravellerPostDTO> travellerPostDTOs = new ArrayList<TravellerPostDTO>()
        for (TravellerPost travellerPost : travellerPostList) {
            travellerPostDTOs.add(mapTravellerPostToTravellerPostDTO(travellerPost))
        }

        return travellerPostDTOs.toArray()
    }


    public TravellerPostDTO mapTravellerPostToTravellerPostDTO(TravellerPost travellerPost){
        if(travellerPost == null){
            return null
        }

        TravellerPostDTO travellerPostDTO = new TravellerPostDTO()
        travellerPostDTO.id = travellerPost.id.toString()
        travellerPostDTO.userId = travellerPost.userId
        travellerPostDTO.source = travellerPost.source
        travellerPostDTO.destination = travellerPost.destination
        travellerPostDTO.startDate = travellerPost.startDate
        travellerPostDTO.endDate = travellerPost.endDate
        travellerPostDTO.postDescription = travellerPost.postDescription

        return travellerPostDTO

    }
}
