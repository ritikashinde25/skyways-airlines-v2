package com.skyways.mapper;

import com.skyways.dto.BookingDTO;
import com.skyways.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    BookingDTO toDTO(Booking booking);
    Booking toEntity(BookingDTO bookingDTO);
}