package com.goodseats.seatviewreviews.domain.stadium.controller;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodseats.seatviewreviews.domain.stadium.mapper.StadiumMapper;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.StadiumsElementResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.dto.response.StadiumsResponse;
import com.goodseats.seatviewreviews.domain.stadium.model.entity.Stadium;
import com.goodseats.seatviewreviews.domain.stadium.model.vo.HomeTeam;
import com.goodseats.seatviewreviews.domain.stadium.repository.StadiumRepository;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
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
				.andDo(document("경기장 목록 조회 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Accept").description("가능한 응답 타입 정보")),
						responseFields(
								fieldWithPath("stadiums").type(JsonFieldType.ARRAY).description("경기장 정보"),
								fieldWithPath("stadiums[].stadiumId").type(JsonFieldType.NUMBER).description("경기장 id"),
								fieldWithPath("stadiums[].name").type(JsonFieldType.STRING).description("경기장 이름"),
								fieldWithPath("stadiums[].homeTeam").type(JsonFieldType.STRING).description("홈 팀 이름")
						)));
	}

	@Test
	@DisplayName("Success - 경기장 상세 조회에 성공하고 200 으로 응답한다")
	void getStadiumSuccess() throws Exception {
		// given
		Stadium stadium = new Stadium("잠실 야구장", "서울 송파구 올림픽로 19-2 서울종합운동장", HomeTeam.DOOSAN_LG);
		Stadium savedStadium = stadiumRepository.save(stadium);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/stadiums/{stadiumId}", savedStadium.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("stadiumName").value(savedStadium.getName()))
				.andExpect(jsonPath("address").value(savedStadium.getAddress()))
				.andDo(print())
				.andDo(document("경기장 상세 조회 성공",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Accept").description("가능한 응답 타입 정보")),
						pathParameters(parameterWithName("stadiumId").description("경기장 id")),
						responseFields(
								fieldWithPath("stadiumName").type(JsonFieldType.STRING).description("경기장 이름"),
								fieldWithPath("address").type(JsonFieldType.STRING).description("경기장 주소")
						)));
	}

	@Test
	@DisplayName("Fail - 해당하는 경기장 id 가 없으면 경기장 상세 조회에 실패하고 404 로 응답한다")
	void getStadiumFailByNotFound() throws Exception {
		// given
		Long wrongStadiumId = -1L;

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/stadiums/{stadiumId}", wrongStadiumId)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andDo(print())
				.andDo(document("경기장 상세 조회 실패 - 경기장 id 없는 경우",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestHeaders(headerWithName("Accept").description("가능한 응답 타입 정보")),
						pathParameters(parameterWithName("stadiumId").description("경기장 id")),
						responseFields(
								fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
								fieldWithPath("url").type(JsonFieldType.STRING).description("요청한 url"),
								fieldWithPath("exceptionName").type(JsonFieldType.STRING).description("발생한 예외 이름"),
								fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메세지"),
								fieldWithPath("createdAt").type(JsonFieldType.STRING).description("예외 발생 시간"),
								fieldWithPath("fieldErrors").type(JsonFieldType.NULL).description("필드 에러 목록")
						)));
	}
}