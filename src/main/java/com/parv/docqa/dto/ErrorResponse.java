package com.parv.docqa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
  public int status;
  public String error;
  public String response;
  public String timestamp;
}
