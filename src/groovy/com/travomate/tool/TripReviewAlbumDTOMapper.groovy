package com.travomate.tool

import com.travomate.Constants
import com.travomate.TripReviewAlbum
import com.travomate.dto.TripReviewAlbumDTO

/**
 * Created by mchopra on 6/11/2017.
 */

@Singleton(lazy = true)
class TripReviewAlbumDTOMapper {

    TripReviewDTOMapper tripReviewDTOMapper = TripReviewDTOMapper.getInstance()

    public TripReviewAlbumDTO[] mapTripReviewAlbumListtoTripReviewAlbumDTO(List<TripReviewAlbumDTO> tripReviewAlbumList) {
        if (tripReviewAlbumList == null || tripReviewAlbumList?.isEmpty()) {
            return null
        }

        List<TripReviewAlbumDTO> tripReviewAlbumDTOs = new ArrayList<TripReviewAlbumDTO>()
        for (TripReviewAlbum tripReview : tripReviewAlbumList) {
            tripReviewAlbumDTOs.add(mapTripReviewAlbumToTripReviewAlbumDTO(tripReview))
        }

        return tripReviewAlbumDTOs.toArray()
    }


    public TripReviewAlbumDTO mapTripReviewAlbumToTripReviewAlbumDTO(TripReviewAlbum tripReviewAlbum){
        if(tripReviewAlbum == null){
            return null
        }

        TripReviewAlbumDTO tripReviewAlbumDTO = new TripReviewAlbumDTO()
//        tripReviewAlbumDTO.tripReviewDTO = tripReviewDTOMapper.mapTripReviewToTripReviewDTO(tripReviewAlbum.tripReview)
        tripReviewAlbumDTO.id = tripReviewAlbum.id
        tripReviewAlbumDTO.imageLoc = tripReviewAlbum.imageLoc?.replace(Constants.IMAGE_ROOT_DIR,"/")
        tripReviewAlbumDTO.isCover = tripReviewAlbum.isCover

        return tripReviewAlbumDTO

    }
}
