package com.goodseats.seatviewreviews.domain.stadium.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodseats.seatviewreviews.domain.stadium.mapper.StadiumMapper;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.StadiumsElementResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.StadiumsResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;
import com.goodseats.seatviewreviews.domain.stadium.repository.StadiumRepository;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class StadiumControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private StadiumRepository stadiumRepository;

	@Test
	@DisplayName("Success - 경기장 목록 조회에 성공하고 200 으로 응답한다")
	void getStadiumsSuccess() throws Exception {
		// given
		Stadium jamsil = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		Stadium hanwha = new Stadium("한화생명 이글스 파크", "대전 중구 대종로 373", HomeTeam.HANWHA);
		List<Stadium> stadiums = List.of(jamsil, hanwha);
		stadiumRepository.saveAll(stadiums);

		List<StadiumsElementResponse> stadiumsElementResponses = stadiumRepository.findAll().stream()
				.map(StadiumMapper::toStadiumsElementResponse)
				.toList();
		StadiumsResponse stadiumsResponse = new StadiumsResponse(stadiumsElementResponses);

		// when & then
		mockMvc.perform(get("/api/v1/stadiums")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json(objectMapper.writeValueAsString(stadiumsResponse)))
				.andDo(print())
				.andReturn();
	}
}