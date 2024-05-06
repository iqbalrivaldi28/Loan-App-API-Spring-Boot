package com.enigmacamp.livecodeloan.model.dto.req;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanTransactionRequestDto {
  
  private RequestIdTransactionalDto customer;
  private RequestIdTransactionalDto instalmentType;
  private List<LoanTransactionDetailRequestDto> loanTransactionDetail;

}
