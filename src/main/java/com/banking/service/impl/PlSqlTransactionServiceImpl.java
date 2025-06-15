package com.banking.service.impl;

import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.enums.ProcedureResult;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.BankingOperationException;
import com.banking.exception.InsufficientFundsException;
import com.banking.repository.TransactionRepository;
import com.banking.service.AccountService;
import com.banking.service.TransactionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Service
@Primary
@Qualifier("plsqlTransactionService")
public class PlSqlTransactionServiceImpl implements TransactionService {

    private final DataSource dataSource;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    public PlSqlTransactionServiceImpl(DataSource dataSource,
                                      AccountService accountService,
                                      TransactionRepository transactionRepository) {
        this.dataSource = dataSource;
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction deposit(String accountNumber, BigDecimal amount, String description) {
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("{call sp_deposit(?, ?, ?, ?)}");
            statement.setString(1, accountNumber);
            statement.setBigDecimal(2, amount);
            statement.setString(3, description);
            statement.registerOutParameter(4, Types.INTEGER);
            
            statement.execute();
            int resultCode = statement.getInt(4);
            ProcedureResult result = ProcedureResult.fromCode(resultCode);
            
            return handleProcedureResult(result, accountNumber, "deposit");
        } catch (SQLException e) {
            throw new BankingOperationException("Database error during deposit", e);
        }
    }

    @Override
    public Transaction withdraw(String accountNumber, BigDecimal amount, String description) {
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("{call sp_withdraw(?, ?, ?, ?)}");
            statement.setString(1, accountNumber);
            statement.setBigDecimal(2, amount);
            statement.setString(3, description);
            statement.registerOutParameter(4, Types.INTEGER);
            
            statement.execute();
            int resultCode = statement.getInt(4);
            ProcedureResult result = ProcedureResult.fromCode(resultCode);
            
            return handleProcedureResult(result, accountNumber, "withdrawal");
        } catch (SQLException e) {
            throw new BankingOperationException("Database error during withdrawal", e);
        }
    }

    @Override
    public Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) {
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("{call sp_transfer(?, ?, ?, ?, ?)}");
            statement.setString(1, fromAccountNumber);
            statement.setString(2, toAccountNumber);
            statement.setBigDecimal(3, amount);
            statement.setString(4, description);
            statement.registerOutParameter(5, Types.INTEGER);
            
            statement.execute();
            int resultCode = statement.getInt(5);
            ProcedureResult result = ProcedureResult.fromCode(resultCode);
            
            return handleProcedureResult(result, fromAccountNumber, "transfer");
        } catch (SQLException e) {
            throw new BankingOperationException("Database error during transfer", e);
        }
    }

    @Override
    public List<Transaction> getAccountTransactions(String accountNumber) {
        Account account = accountService.getAccount(accountNumber);
        return transactionRepository.findByAccountIdOrderByTransactionDateDesc(account.getId());
    }

    private Transaction handleProcedureResult(ProcedureResult result, String accountNumber, String operation) {
        switch (result) {
            case SUCCESS:
                Account account = accountService.getAccount(accountNumber);
                List<Transaction> transactions = transactionRepository.findByAccountIdOrderByTransactionDateDesc(account.getId());
                return transactions.isEmpty() ? null : transactions.get(0);
            case ACCOUNT_NOT_FOUND:
                throw new AccountNotFoundException("Account not found: " + accountNumber);
            case INSUFFICIENT_FUNDS:
                throw new InsufficientFundsException("Insufficient funds for " + operation);
            case DATABASE_ERROR:
            default:
                throw new BankingOperationException(operation + " failed");
        }
    }
} 