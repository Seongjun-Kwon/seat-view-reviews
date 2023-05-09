package com.goodseats.seatviewreviews.domain.seat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.goodseats.seatviewreviews.domain.seat.model.dto.SeatSectionsResponse;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatSectionRepository;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;

@ExtendWith(MockitoExtension.class)
class SeatSectionServiceTest {

	@Mock
	private SeatSectionRepository seatSectionRepository;

	@InjectMocks
	private SeatSectionService seatSectionService;

	@Test
	@DisplayName("Success - 좌석 구역 목록 조회에 성공한다")
	void getSeatSectionsSuccess() {
		// given
		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		SeatSection seatSection1 = new SeatSection("110", stadium, seatGrade);
		SeatSection seatSection2 = new SeatSection("111", stadium, seatGrade);
		List<SeatSection> seatSections = List.of(seatSection1, seatSection2);
		when(seatSectionRepository.findAllByStadium(anyLong())).thenReturn(seatSections);

		// when
		SeatSectionsResponse seatSectionsResponse = seatSectionService.getSeatSections(1L);

		// then
		verify(seatSectionRepository).findAllByStadium(1L);
		assertThat(seatSectionsResponse.seatSections().size()).isEqualTo(seatSections.size());

		for (int i = 0; i < seatSections.size(); i++) {
			assertThat(seatSectionsResponse.seatSections().get(i).sectionName()).isEqualTo(seatSections.get(i).getName());
		}
	}
}