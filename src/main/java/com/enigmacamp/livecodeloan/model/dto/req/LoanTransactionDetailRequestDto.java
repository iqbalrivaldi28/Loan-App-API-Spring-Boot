package com.enigmacamp.livecodeloan.model.dto.req;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanTransactionDetailRequestDto {

  private String loanTypeId;
  private Double nominal;

}
