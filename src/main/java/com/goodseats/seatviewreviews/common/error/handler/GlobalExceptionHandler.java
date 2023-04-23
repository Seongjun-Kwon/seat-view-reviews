package com.goodseats.seatviewreviews.common.error.handler;

import static com.goodseats.seatviewreviews.common.error.dto.ErrorResponse.*;
import static org.springframework.http.HttpStatus.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.goodseats.seatviewreviews.common.error.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
			HttpServletRequest request, DataIntegrityViolationException e
	) {
		logInfo(e, request.getRequestURI());

		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(
						of(
								HttpStatus.CONFLICT.value(),
								request.getRequestURI(),
								e.getClass().getSimpleName(),
								e.getMessage(),
								null
						)
				);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
			HttpServletRequest request, MethodArgumentTypeMismatchException e
	) {
		logInfo(e, request.getRequestURI());

		return ResponseEntity
				.badRequest()
				.body(
						of(
								BAD_REQUEST.value(),
								request.getRequestURI(),
								e.getClass().getSimpleName(),
								e.getMessage(),
								List.of(new FieldError(e.getName(), Objects.requireNonNull(e.getValue()).toString(), e.getMessage()))
						)
				);
	}

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ErrorResponse> handleNullPointException(HttpServletRequest request, NullPointerException e) {
		logInfo(e, request.getRequestURI());

		return ResponseEntity
				.badRequest()
				.body(
						of(
								BAD_REQUEST.value(),
								request.getRequestURI(),
								e.getClass().getSimpleName(),
								e.getMessage(),
								null
						)
				);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(
			HttpServletRequest request, ConstraintViolationException e
	) {
		logInfo(e, request.getRequestURI());

		return ResponseEntity
				.badRequest()
				.body(
						of(
								BAD_REQUEST.value(),
								request.getRequestURI(),
								e.getClass().getSimpleName(),
								e.getMessage(),
								makeFieldErrorsFromConstraintViolation(e.getConstraintViolations())
						)
				);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
			HttpServletRequest request, MethodArgumentNotValidException e
	) {
		logInfo(e, request.getRequestURI());

		return ResponseEntity
				.badRequest()
				.body(
						ErrorResponse.of(
								BAD_REQUEST.value(),
								request.getRequestURI(),
								e.getClass().getSimpleName(),
								e.getMessage(),
								makeFieldErrorsFromBindingResult(e.getBindingResult())
						)
				);
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorResponse> handleBindException(
			HttpServletRequest request, BindException e
	) {
		logInfo(e, request.getRequestURI());

		return ResponseEntity
				.badRequest()
				.body(
						ErrorResponse.of(
								BAD_REQUEST.value(),
								request.getRequestURI(),
								e.getClass().getSimpleName(),
								e.getMessage(),
								makeFieldErrorsFromBindingResult(e.getBindingResult())
						)
				);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
			HttpServletRequest request, IllegalArgumentException e) {
		logInfo(e, request.getRequestURI());

		return ResponseEntity
				.badRequest()
				.body(
						ErrorResponse.of(
								BAD_REQUEST.value(),
								request.getRequestURI(),
								e.getClass().getSimpleName(),
								e.getMessage(),
								null
						)
				);
	}

	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<ErrorResponse> handleInvalidFormatException(
			HttpServletRequest request, InvalidFormatException e) {
		logInfo(e, request.getRequestURI());

		return ResponseEntity
				.badRequest()
				.body(
						ErrorResponse.of(
								BAD_REQUEST.value(),
								request.getRequestURI(),
								e.getClass().getSimpleName(),
								e.getMessage(),
								null
						)
				);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleRuntimeException(HttpServletRequest request, RuntimeException e) {
		logWarn(e, request.getRequestURI());

		return ResponseEntity
				.badRequest()
				.body(
						of(
								BAD_REQUEST.value(),
								request.getRequestURI(),
								e.getClass().getSimpleName(),
								e.getMessage(),
								null
						)
				);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
		logError(e, request.getRequestURI());

		return ResponseEntity
				.internalServerError()
				.body(
						of(
								HttpStatus.INTERNAL_SERVER_ERROR.value(),
								request.getRequestURI(),
								e.getClass().getSimpleName(),
								e.getMessage(),
								null
						)
				);
	}

	private List<ErrorResponse.FieldError> makeFieldErrorsFromBindingResult(BindingResult bindingResult) {

		return bindingResult.getFieldErrors()
				.stream()
				.map(error -> new ErrorResponse.FieldError(
						error.getField(),
						error.getRejectedValue(),
						error.getDefaultMessage()
				))
				.toList();
	}

	private List<ErrorResponse.FieldError> makeFieldErrorsFromConstraintViolation(
			Set<ConstraintViolation<?>> constraintViolations
	) {
		return constraintViolations.stream()
				.map(violation -> new ErrorResponse.FieldError(
						getFieldFromPath(violation.getPropertyPath()),
						violation.getInvalidValue().toString(),
						violation.getMessage()
				))
				.toList();
	}

	private String getFieldFromPath(Path fieldPath) {
		PathImpl pathImpl = (PathImpl)fieldPath;
		return pathImpl.getLeafNode().toString();
	}

	private void logInfo(Exception e, String url) {
		log.info("URL = {}, Exception = {}, Message = {}", url, e.getClass().getSimpleName(), e.getMessage());
	}

	private void logWarn(Exception e, String url) {
		log.info("URL = {}, Exception = {}, Message = {}", url, e.getClass().getSimpleName(), e.getMessage());
	}

	private void logError(Exception e, String url) {
		log.info("URL = {}, Exception = {}, Message = {}", url, e.getClass().getSimpleName(), e.getMessage());
	}
}
