package com.goodseats.seatviewreviews.domain.seat.controller;

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
import com.goodseats.seatviewreviews.domain.seat.mapper.SeatSectionMapper;
import com.goodseats.seatviewreviews.domain.seat.model.dto.SeatSectionsResponse;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatGrade;
import com.goodseats.seatviewreviews.domain.seat.model.entity.SeatSection;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatGradeRepository;
import com.goodseats.seatviewreviews.domain.seat.repository.SeatSectionRepository;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;
import com.goodseats.seatviewreviews.domain.stadium.repository.StadiumRepository;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SeatSectionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private StadiumRepository stadiumRepository;

	@Autowired
	private SeatGradeRepository seatGradeRepository;

	@Autowired
	private SeatSectionRepository seatSectionRepository;

	@Test
	@DisplayName("Success - 좌석 구역 목록 조회에 성공하고 200 으로 응답한다")
	void getSeatSectionsSuccess() throws Exception {
		// given
		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		SeatGrade seatGrade = new SeatGrade("테이블", "주중 47,000 / 주말 53,000", stadium);
		stadiumRepository.save(stadium);
		seatGradeRepository.save(seatGrade);

		SeatSection seatSection1 = new SeatSection("110", stadium, seatGrade);
		SeatSection seatSection2 = new SeatSection("111", stadium, seatGrade);
		List<SeatSection> seatSections = List.of(seatSection1, seatSection2);
		seatSectionRepository.saveAll(seatSections);

		SeatSectionsResponse seatSectionsResponse = SeatSectionMapper.toSeatSectionsResponse(seatSections);

		// when & then
		mockMvc.perform(get("/api/v1/sections")
						.param("stadiumId", stadium.getId().toString())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json(objectMapper.writeValueAsString(seatSectionsResponse)))
				.andDo(print());
	}
}