package com.travomate.tool

import com.travomate.TripReview
import com.travomate.TripReviewAlbum
import com.travomate.dto.TripReviewDTO
/**
 * Created by asaxena on 6/4/2017.
 */

@Singleton(lazy = true)
class TripReviewDTOMapper {

    UserDTOMapper userDTOMapper = UserDTOMapper.getInstance()

    public TripReviewDTO[] mapTripReviewListtoTripReviewDTO(List<TripReview> tripReviewList) {
        if (tripReviewList == null || tripReviewList?.isEmpty()) {
            return null
        }

        List<TripReviewDTO> tripReviewDTOs = new ArrayList<TripReviewDTO>()
        for (TripReview tripReview : tripReviewList) {
            tripReviewDTOs.add(mapTripReviewToTripReviewDTO(tripReview))
        }

        return tripReviewDTOs.toArray()
    }


    public TripReviewDTO mapTripReviewToTripReviewDTO(TripReview tripReview){
        if(tripReview == null){
            return null
        }

        List<TripReviewAlbum> tripReviewAlbumList = TripReviewAlbum.findAllByTripReview(tripReview)

        TripReviewDTO tripReviewDTO = new TripReviewDTO()
        tripReviewDTO.id = tripReview?.id
         tripReviewDTO.title = tripReview?.title
        tripReviewDTO.timeToVisit = tripReview?.timeToVisit
        tripReviewDTO.routeToTake = tripReview?.routeToTake
        tripReviewDTO.tripDescription = tripReview?.tripDescription


        return tripReviewDTO

    }
}
