package com.goodseats.seatviewreviews.domain.stadium.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.goodseats.seatviewreviews.domain.stadium.model.dto.StadiumsResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;
import com.goodseats.seatviewreviews.domain.stadium.repository.StadiumRepository;

@ExtendWith(MockitoExtension.class)
class StadiumServiceTest {

	@Mock
	private StadiumRepository stadiumRepository;

	@InjectMocks
	private StadiumService stadiumService;

	@Test
	@DisplayName("Success - 경기장 목록 조회에 성공한다")
	void getStadiumsSuccess() {
		// given
		Stadium jamsil = new Stadium("잠실 야구장", "testAddress", HomeTeam.DOOSAN_LG);
		Stadium hanwha = new Stadium("한화생명 이글스 파크", "testAddress", HomeTeam.HANWHA);
		List<Stadium> stadiums = List.of(jamsil, hanwha);
		when(stadiumRepository.findAll()).thenReturn(stadiums);

		// when
		StadiumsResponse stadiumsResponse = stadiumService.getStadiums();

		// then
		verify(stadiumRepository).findAll();
		assertThat(stadiumsResponse.stadiums().size()).isEqualTo(stadiums.size());

		for (int i = 0; i < stadiums.size(); i++) {
			assertThat(stadiumsResponse.stadiums().get(i)).usingRecursiveComparison()
					.ignoringFields("stadiumId")
					.isEqualTo(stadiums.get(i));
		}
	}
}