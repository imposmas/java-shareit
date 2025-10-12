package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.constants.BookingStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {
    private Long id;
    private Long itemId;
    private Long bookerId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime start;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime end;
    private BookingStatus status;
}