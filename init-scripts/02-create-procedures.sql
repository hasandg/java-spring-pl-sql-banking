
CREATE OR REPLACE PROCEDURE sp_deposit(
    p_account_number IN VARCHAR2,
    p_amount IN NUMBER,
    p_description IN VARCHAR2,
    p_result OUT NUMBER
) AS
    v_account_id NUMBER;
    v_current_balance NUMBER;
BEGIN
    SELECT id, balance INTO v_account_id, v_current_balance
    FROM accounts
    WHERE account_number = p_account_number;
    
    UPDATE accounts
    SET balance = balance + p_amount,
        updated_at = SYSTIMESTAMP
    WHERE id = v_account_id;
    
    INSERT INTO transactions (
        id, account_id, transaction_type, amount, description, 
        transaction_date, status
    ) VALUES (
        transaction_seq.NEXTVAL, v_account_id, 'DEPOSIT', p_amount, 
        p_description, SYSTIMESTAMP, 'COMPLETED'
    );
    
    COMMIT;
    p_result := 1;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_result := -1;
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := -2;
END sp_deposit;
/


CREATE OR REPLACE PROCEDURE sp_withdraw(
    p_account_number IN VARCHAR2,
    p_amount IN NUMBER,
    p_description IN VARCHAR2,
    p_result OUT NUMBER
) AS
    v_account_id NUMBER;
    v_current_balance NUMBER;
BEGIN
    SELECT id, balance INTO v_account_id, v_current_balance
    FROM accounts
    WHERE account_number = p_account_number;
    
    IF v_current_balance < p_amount THEN
        p_result := -3;
        RETURN;
    END IF;
    
    UPDATE accounts
    SET balance = balance - p_amount,
        updated_at = SYSTIMESTAMP
    WHERE id = v_account_id;
    
    INSERT INTO transactions (
        id, account_id, transaction_type, amount, description, 
        transaction_date, status
    ) VALUES (
        transaction_seq.NEXTVAL, v_account_id, 'WITHDRAWAL', p_amount, 
        p_description, SYSTIMESTAMP, 'COMPLETED'
    );
    
    COMMIT;
    p_result := 1;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_result := -1;
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := -2;
END sp_withdraw;
/


CREATE OR REPLACE PROCEDURE sp_transfer(
    p_from_account_number IN VARCHAR2,
    p_to_account_number IN VARCHAR2,
    p_amount IN NUMBER,
    p_description IN VARCHAR2,
    p_result OUT NUMBER
) AS
    v_from_account_id NUMBER;
    v_to_account_id NUMBER;
    v_from_balance NUMBER;
BEGIN
    SELECT id, balance INTO v_from_account_id, v_from_balance
    FROM accounts
    WHERE account_number = p_from_account_number;
    
    SELECT id INTO v_to_account_id
    FROM accounts
    WHERE account_number = p_to_account_number;
    
    IF v_from_balance < p_amount THEN
        p_result := -3;
        RETURN;
    END IF;
    
    UPDATE accounts
    SET balance = balance - p_amount,
        updated_at = SYSTIMESTAMP
    WHERE id = v_from_account_id;
    
    UPDATE accounts
    SET balance = balance + p_amount,
        updated_at = SYSTIMESTAMP
    WHERE id = v_to_account_id;
    
    INSERT INTO transactions (
        id, account_id, transaction_type, amount, description, 
        transaction_date, status
    ) VALUES (
        transaction_seq.NEXTVAL, v_from_account_id, 'TRANSFER', p_amount, 
        'Transfer to ' || p_to_account_number || ': ' || p_description, 
        SYSTIMESTAMP, 'COMPLETED'
    );
    
    INSERT INTO transactions (
        id, account_id, transaction_type, amount, description, 
        transaction_date, status
    ) VALUES (
        transaction_seq.NEXTVAL, v_to_account_id, 'DEPOSIT', p_amount, 
        'Transfer from ' || p_from_account_number || ': ' || p_description, 
        SYSTIMESTAMP, 'COMPLETED'
    );
    
    COMMIT;
    p_result := 1;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_result := -1;
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := -2;
END sp_transfer;
/ 