package edu.unh.cs.cs619.bulletzone.datalayer;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;

import edu.unh.cs.cs619.bulletzone.datalayer.account.AccountTransferHistoryRecord;
import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccount;
import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUser;

public class BankAccountRepositoryTest {
    static BulletZoneData db;
    static GameUser basicUser, otherUser;
    static BankAccount basicAccount, otherAccount;

    @BeforeClass
    static public void setup() {
        db = new BulletZoneData();
        db.rebuildData();
        basicUser = db.users.createUser("BasicUser", "BasicUser", "password");
        otherUser = db.users.createUser("OtherUser", "OtherUser", "password");
        basicAccount = db.accounts.create();
        db.permissions.setOwner(basicAccount, basicUser);
        otherAccount = db.accounts.create();
        db.permissions.setOwner(otherAccount, otherUser);
    }

    @Test
    public void getAccount_onExistingAccount_returnsCorrectAccount() {
        assertEquals(db.accounts.getAccount(basicAccount.getId()), basicAccount);
        assertEquals(db.accounts.getAccount(otherAccount.getId()), otherAccount);
    }

    @Test
    public void getAccounts_withExistingAccounts_returnsWithKnownAccounts() {
        Collection<BankAccount> list = db.accounts.getAccounts();
        assertTrue(list.contains(basicAccount));
        assertTrue(list.contains(otherAccount));
        assertThat(list.size(), is(2));
    }

    @Test
    public void modifyAccountBalance_byPositiveThenNegativeAmount_updatesBalanceAndTransferHistory() {
        int amount = 500;
        double startAmount = basicAccount.getBalance();
        Collection<AccountTransferHistoryRecord> startList = db.accounts.getTransactions(basicAccount);

        db.accounts.modifyBalance(basicAccount, amount);
        assertThat(basicAccount.getBalance(), is(startAmount + amount));

        db.accounts.modifyBalance(basicAccount, -amount);
        assertThat(basicAccount.getBalance(), is(startAmount));
        Collection<AccountTransferHistoryRecord> endList = db.accounts.getTransactions(basicAccount);
        assertThat(endList.size(), is(startList.size() + 2));
    }

    @Test
    public void transfer_betweenAccountsWithEnough_isSuccessfulAndGeneratesHistoryForBothAccounts() {
        int start = 1000, amount = 700;
        db.accounts.modifyBalance(basicAccount, start);
        double basicAmount = basicAccount.getBalance();
        double otherAmount = otherAccount.getBalance();
        Collection<AccountTransferHistoryRecord> basicList = db.accounts.getTransactions(basicAccount);
        Collection<AccountTransferHistoryRecord> otherList = db.accounts.getTransactions(otherAccount);

        assertTrue(db.accounts.transfer(basicAccount, otherAccount, amount));

        assertThat(basicAccount.getBalance(), is(basicAmount - amount));
        assertThat(otherAccount.getBalance(), is(otherAmount + amount));
        Collection<AccountTransferHistoryRecord> endList = db.accounts.getTransactions(basicAccount);
        assertThat(endList.size(), is (basicList.size() + 1));
        endList = db.accounts.getTransactions(otherAccount);
        assertThat(endList.size(), is (otherList.size() + 1));
    }
}