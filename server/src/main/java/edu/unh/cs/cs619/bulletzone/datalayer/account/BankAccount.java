package edu.unh.cs.cs619.bulletzone.datalayer.account;

import edu.unh.cs.cs619.bulletzone.datalayer.permission.OwnableEntity;

public class BankAccount extends OwnableEntity {
    protected double balance;

    public double getBalance() { return balance; }

    public BankAccount(int userId) {
        super(new BankAccountRecord());
        this.balance = 1000.0; // Initial balance of 1000 credits
    }

    BankAccount(BankAccountRecord rec) {
        super(rec);
        if (rec != null) {
            this.balance = rec.credits;
            if (this.balance == 0) {
                this.balance = 1000.0;
                rec.credits = this.balance;
            }
        } else {
            this.balance = 1000.0;
        }
    }

    /**
     * Modifies the credit balance for the account
     * @param amount Positive or negative amount to add to the credit balance
     * @return true if successful
     */
    public boolean modifyBalance(double amount) {
        balance += amount;
        BankAccountRecord record = (BankAccountRecord) getRecord();
        if (record != null) {
            record.credits = balance;
        }
        return true;
    }

    /**
     * Gets the underlying record for this account
     * @return The BankAccountRecord associated with this account
     */
    protected BankAccountRecord getRecord() {
        return (BankAccountRecord) record;
    }
}