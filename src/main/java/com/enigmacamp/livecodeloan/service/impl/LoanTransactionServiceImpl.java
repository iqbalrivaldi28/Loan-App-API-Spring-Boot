package com.enigmacamp.livecodeloan.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.enigmacamp.livecodeloan.model.dto.req.ApproveTransactionByAdminDto;
import com.enigmacamp.livecodeloan.model.dto.req.LoanTransactionDetailRequestDto;
import com.enigmacamp.livecodeloan.model.dto.req.LoanTransactionRequestDto;
import com.enigmacamp.livecodeloan.model.dto.res.TransactionalResponseLoanDto;
import com.enigmacamp.livecodeloan.model.entity.Customer;
import com.enigmacamp.livecodeloan.model.entity.InstalmentType;
import com.enigmacamp.livecodeloan.model.entity.LoanTransaction;
import com.enigmacamp.livecodeloan.model.entity.LoanTransactionDetail;
import com.enigmacamp.livecodeloan.model.entity.LoanType;
import com.enigmacamp.livecodeloan.model.entity.User;
import com.enigmacamp.livecodeloan.repository.LoanTransactionDetailRepository;
import com.enigmacamp.livecodeloan.repository.LoanTransactionRepository;
import com.enigmacamp.livecodeloan.repository.UserRepository;
import com.enigmacamp.livecodeloan.service.CustomerService;
import com.enigmacamp.livecodeloan.service.InstalmentTypeService;
import com.enigmacamp.livecodeloan.service.LoanTransactionService;
import com.enigmacamp.livecodeloan.service.LoanTypeService;
import com.enigmacamp.livecodeloan.utils.constant.ApprovalStatus;
import com.enigmacamp.livecodeloan.utils.constant.LoanStatus;

import jakarta.transaction.Transactional;

@Service
public class LoanTransactionServiceImpl implements LoanTransactionService {
  
  @Autowired
  private LoanTransactionRepository loanTransactionRepository;

  @Autowired
  private LoanTypeService loanTypeService;

  @Autowired
  private InstalmentTypeService instalmentTypeService;

  @Autowired
  private CustomerService customerService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private LoanTransactionDetailRepository loanTransactionDetailRepository;

  @Override
  @Transactional
  public TransactionalResponseLoanDto createTrxLoan(LoanTransactionRequestDto loanTransactionRequestDto) {

    Customer customer = this.customerService.getCustomerById(
      loanTransactionRequestDto.getCustomer().getId()
    );

    InstalmentType instalmentType = this.instalmentTypeService.getInstalmentTypeById(
      loanTransactionRequestDto.getInstalmentType().getId()
    );

    Double totalNominal = 0.0;
    List<LoanTransactionDetail> loanTrxDetailsWrapper = new ArrayList<>();

    LoanTransaction loanTransaction = LoanTransaction.builder()
    .customer(customer)
    .instalmentType(instalmentType)
    .createdAt(new Date())
    .loanTransactionDetails(loanTrxDetailsWrapper)
    .build();

    for(LoanTransactionDetailRequestDto loanTransactionDetailRequestDto : loanTransactionRequestDto.getLoanTransactionDetail()) {

      LoanType loanType = this.loanTypeService.getLoanTypeById(loanTransactionDetailRequestDto.getLoanTypeId());
      
      if (loanTransactionDetailRequestDto.getNominal() > loanType.getMaxLoan()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gagal Request Loan, Nominal Melebihi Batas maksimal nominal");
      }
      
      LoanTransactionDetail loanTransactionDetail = LoanTransactionDetail.builder()
      .nominal(loanTransactionDetailRequestDto.getNominal())
      .transactionDate(new Date())
      .loanStatus(LoanStatus.UNPAID)
      .loanTransaction(loanTransaction)
      .loanType(loanType)
      .createdAt(new Date())
      .build();

      loanTrxDetailsWrapper.add(loanTransactionDetail);
      totalNominal += loanTransactionDetailRequestDto.getNominal();
      this.loanTransactionDetailRepository.save(loanTransactionDetail);
    }

    loanTransaction.setNominal(totalNominal);
    this.loanTransactionRepository.save(loanTransaction);
    return this.wrapperResponse(loanTransaction);

  }

  @Override
  public TransactionalResponseLoanDto getTrxLoanById(String id) {
    LoanTransaction loanTransaction =  this.loanTransactionRepository.findById(id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan Transaction Not FoundL")
    );

    return this.wrapperResponse(loanTransaction);
  }

  @Override
  @Transactional
  public TransactionalResponseLoanDto ApproveTrxByAdmin(String adminId, ApproveTransactionByAdminDto approveTransactionByAdminDto) {
    User user = this.userRepository.findById(adminId).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found")
    );

    LoanTransaction loanTransaction =  this.loanTransactionRepository.findById(
      approveTransactionByAdminDto.getLoanTransactionId()).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan Transaction Not Found")
    );

    loanTransaction.setApprovedBy(user.getEmail());
    loanTransaction.setApprovedAt(new Date());
    loanTransaction.setApprovalStatus(ApprovalStatus.APPROVED);
    loanTransaction.setUpdatedAt(new Date());

    this.loanTransactionRepository.save(loanTransaction);

    return this.wrapperResponse(loanTransaction);

  }

  private TransactionalResponseLoanDto wrapperResponse(LoanTransaction loanTransaction) {
    return TransactionalResponseLoanDto.builder()
    .id(loanTransaction.getId())
    .instalmentTypeId(loanTransaction.getInstalmentType().getId())
    .customerId(loanTransaction.getCustomer().getId())
    .nominal(loanTransaction.getNominal())
    .approvedAt(loanTransaction.getApprovedAt())
    .approvedBy(loanTransaction.getApprovedBy())
    .approvalStatus(loanTransaction.getApprovalStatus())
    .loanTransactionDetails(loanTransaction.getLoanTransactionDetails())
    .createdAt(loanTransaction.getCreatedAt())
    .updatedAt(loanTransaction.getUpdatedAt())
    .build();
  }
  
}
