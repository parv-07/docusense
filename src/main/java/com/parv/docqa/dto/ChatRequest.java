package com.parv.docqa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class ChatRequest {
   public void setQuestion(String question) {
      this.question = question;
   }

   public String getQuestion() {
      return question;
   }

   public String question;
}
