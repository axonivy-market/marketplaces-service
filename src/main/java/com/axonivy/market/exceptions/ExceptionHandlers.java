package com.axonivy.market.exceptions;

import com.axonivy.market.exceptions.model.DuplicatedEntityException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.Message;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ExceptionHandlers extends ResponseEntityExceptionHandler {

  private static final String NOT_FOUND_EXCEPTION_CODE = "-1";
  private static final String DUPLICATED_ENTITY_EXCEPTION_CODE = "-1";
  private static final String METHOD_ARGUMENT_NOT_VALID_EXCEPTION_CODE = "-1";

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    BindingResult bindingResult = ex.getBindingResult();
    List<String> errors = new ArrayList<>();

    if (bindingResult.hasErrors()) {
      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        errors.add(fieldError.getDefaultMessage());
      }
    } else {
      errors.add(ex.getMessage());
    }

    var errorMessage = new Message();
    errorMessage.setErrorCode(METHOD_ARGUMENT_NOT_VALID_EXCEPTION_CODE);
    errorMessage.setMessageDetails(String.join("; ", errors));
    return new ResponseEntity<>(errorMessage, status);
  }

  @ExceptionHandler(MissingHeaderException.class)
  public ResponseEntity<Object> handleMissingServletRequestParameter(MissingHeaderException missingHeaderException) {
    var errorMessage = new Message();
    errorMessage.setMessageDetails(missingHeaderException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Object> handleNotFoundException(NotFoundException notFoundException) {
    var errorMessage = new Message();
    errorMessage.setErrorCode(NOT_FOUND_EXCEPTION_CODE);
    errorMessage.setMessageDetails(notFoundException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(DuplicatedEntityException.class)
  public ResponseEntity<Object> handleNotFoundException(DuplicatedEntityException duplicateFeedbackException) {
    var errorMessage = new Message();
    errorMessage.setErrorCode(DUPLICATED_ENTITY_EXCEPTION_CODE);
    errorMessage.setMessageDetails(duplicateFeedbackException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Oauth2ExchangeCodeException.class)
  public ResponseEntity<Object> handleNotFoundException(Oauth2ExchangeCodeException oauth2ExchangeCodeException) {
    var errorMessage = new Message();
    errorMessage.setErrorCode(oauth2ExchangeCodeException.getError());
    errorMessage.setMessageDetails(oauth2ExchangeCodeException.getErrorDescription());
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }
}
