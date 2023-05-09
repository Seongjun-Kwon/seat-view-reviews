package com.goodseats.seatviewreviews.domain.seat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.goodseats.seatviewreviews.domain.seat.model.dto.response.SeatsResponse;
import com.goodseats.seatviewreviews.domain.seat.model.entity.Seat;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatRepository;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

	@Mock
	private SeatRepository seatRepository;

	@InjectMocks
	private SeatService seatService;

	@Test
	@DisplayName("Success - 좌석 목록 조회에 성공한다")
	void getSeatsSuccess() {
		// given
		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		SeatSection seatSection = new SeatSection("110", stadium, seatGrade);
		Seat seat1 = new Seat("1", seatGrade, seatSection);
		Seat seat2 = new Seat("2", seatGrade, seatSection);
		List<Seat> seats = List.of(seat1, seat2);
		when(seatRepository.findAllBySeatSection(anyLong())).thenReturn(seats);

		// when
		SeatsResponse seatsResponse = seatService.getSeats(1L);

		// then
		verify(seatRepository).findAllBySeatSection(1L);
		assertThat(seatsResponse.seats().size()).isEqualTo(seats.size());

		for (int i = 0; i < seats.size(); i++) {
			assertThat(seatsResponse.seats().get(i).seatInfo()).isEqualTo(seats.get(i).getSeatInfo());
			assertThat(seatsResponse.seats().get(i).score()).isEqualTo(seats.get(i).getAverageScore());
		}
	}
}