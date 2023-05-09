package com.goodseats.seatviewreviews.domain.stadium.service;

import static com.goodseats.seatviewreviews.common.error.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.goodseats.seatviewreviews.common.error.exception.NotFoundException;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.StadiumDetailResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.StadiumsResponse;
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
		Stadium jamsil = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		Stadium hanwha = new Stadium("한화생명 이글스 파크", "대전 중구 대종로 373", HomeTeam.HANWHA);
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

	@Test
	@DisplayName("Success - 경기장 상세 조회에 성공한다")
	void getStadiumSuccess() {
		// given
		Long stadiumId = 1L;
		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));

		// when
		StadiumDetailResponse stadiumDetailResponse = stadiumService.getStadium(stadiumId);

		// then
		verify(stadiumRepository).findById(stadiumId);
		assertThat(stadiumDetailResponse.stadiumName()).isEqualTo(stadium.getName());
		assertThat(stadiumDetailResponse.address()).isEqualTo(stadium.getAddress());
	}

	@Test
	@DisplayName("Fail - 해당하는 경기장 id 가 없으면 경기장 상세 조회에 실패한다")
	void getStadiumFailByNotFound() {
		// given
		Long stadiumId = 1L;
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> stadiumService.getStadium(stadiumId))
				.isExactlyInstanceOf(NotFoundException.class)
				.hasMessage(NOT_FOUND.getMessage());
	}
}