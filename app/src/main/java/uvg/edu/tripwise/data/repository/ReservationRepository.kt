package uvg.edu.tripwise.data.repository

import uvg.edu.tripwise.network.ApiProperty
import uvg.edu.tripwise.network.ApiUser
import uvg.edu.tripwise.network.RetrofitInstance
import uvg.edu.tripwise.network.ReservationResponse
import uvg.edu.tripwise.network.UpdateReservationRequest
import uvg.edu.tripwise.network.PropertyDeleted

class ReservationRepository {
    private val api = RetrofitInstance.api

    suspend fun getByProperty(propertyId: String): List<ReservationResponse> {
        val response = api.getReservationsProperty(propertyId)

        return response.reservations.map { item ->
            ReservationResponse(
                id = item.reservationId,
                reservationUser = ApiUser(
                    id = item.user.id,
                    name = item.user.name,
                    email = item.user.email,
                    pfp = null,
                    role = null,
                    properties = null,
                    bookings = null,
                    itineraries = null,
                    moneyProperty = null,
                    moneyItinerary = null,
                    interests = null,
                    createdAt = item.reservationDate,
                    deleted = null
                ),
                propertyBooked = ApiProperty(
                    _id = response.property.id,
                    name = response.property.name,
                    description = "",
                    location = "",
                    pricePerNight = 0.0,
                    capacity = 0,
                    pictures = emptyList(),
                    amenities = emptyList(),
                    propertyType = "",
                    owner = "",
                    approved = "",
                    latitude = null,
                    longitude = null,
                    createdAt = "",
                    deleted = PropertyDeleted(`is` = false, at = null),
                    reviews = null
                ),
                checkInDate = item.checkInDate,
                checkOutDate = item.checkOutDate,
                payment = item.payment,
                state = item.state,
                persons = item.persons,
                days = item.days,
                itinerary = null
            )
        }
    }

    suspend fun updateState(id: String, state: String): ReservationResponse? =
        api.updateReservation(id, UpdateReservationRequest(state = state)).body()

}


