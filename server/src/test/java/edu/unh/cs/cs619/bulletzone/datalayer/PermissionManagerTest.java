package edu.unh.cs.cs619.bulletzone.datalayer;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;

import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccount;
import edu.unh.cs.cs619.bulletzone.datalayer.item.GameItem;
import edu.unh.cs.cs619.bulletzone.datalayer.item.GameItemContainer;
import edu.unh.cs.cs619.bulletzone.datalayer.permission.Permission;
import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUser;

public class PermissionManagerTest {

    static BulletZoneData db, bzdata;
    static GameUser basicUser, otherUser;

    @BeforeClass
    static public void setup() {
        bzdata = db = new BulletZoneData();
        db.rebuildData();
        basicUser = db.users.createUser("BasicUser", "BasicUser", "password");
        otherUser = db.users.createUser("OtherUser", "OtherUser", "password");
    }

    @Test
    public void setOwner_onValidItem_updatesInternalStructures() {
        GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
        GameItem cannon = db.items.create(db.types.TankCannon);
        BankAccount account = db.accounts.create();
        db.permissions.setOwner(tank, basicUser);
        db.permissions.setOwner(cannon, basicUser);
        db.permissions.setOwner(account, basicUser);

        assertThat(basicUser.getOwnedEntities().contains(tank), is(true));
        assertThat(basicUser.getOwnedEntities().contains(cannon), is(true));
        assertThat(basicUser.getOwnedEntities().contains(account), is(true));
        assertThat(db.permissions.getUserPermissions(basicUser).getItems().size(), is(3));

        assertThat(basicUser.getOwnedAccounts().contains(account), is(true));
        assertThat(basicUser.getOwnedEntities().size(), is(3));

        db.permissions.removeOwner(tank);
        db.permissions.removeOwner(cannon);
        db.permissions.removeOwner(account);
    }

    @Test
    public void removeOwner_onValidItem_updatesInternalStructures() {
        GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
        GameItem cannon = db.items.create(db.types.TankCannon);
        BankAccount account = db.accounts.create();
        db.permissions.setOwner(tank, basicUser);
        db.permissions.setOwner(cannon, basicUser);
        db.permissions.setOwner(account, basicUser);

        db.permissions.removeOwner(tank);
        db.permissions.removeOwner(cannon);
        db.permissions.removeOwner(account);
        assertThat(basicUser.getOwnedItems().contains(tank), is(false));
        assertThat(basicUser.getOwnedEntities().contains(cannon), is(false));
        assertThat(basicUser.getOwnedEntities().contains(account), is(false));
        assertThat(db.permissions.getUserPermissions(basicUser).getItems().size(), is(0));
    }

    @Test
    public void grant_OnValidPermission_updatesInternalStructures() {
        GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
        db.permissions.grant(tank, basicUser, Permission.Add);
        assertThat(db.permissions.check(tank, basicUser, Permission.Add), is(true));
        db.permissions.revoke(tank, basicUser, Permission.Add);

    }

    @Test
    public void revoke_OnValidPermission_updatesInternalStructures() {
        GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
        db.permissions.grant(tank, basicUser, Permission.Add);

        db.permissions.revoke(tank, basicUser, Permission.Add);
        assertThat(db.permissions.check(tank, basicUser, Permission.Add), is(false));

        assertThat(db.permissions.getUserPermissions(basicUser).getItems().size(), is(0));
    }

    @Test
    public void getUsersWithPermissionsOn_withTwoPermittedUsers_returnsThoseUsers() {
        GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
        GameUser owner = db.users.createUser("Owner", "Owner", "password");
        db.permissions.setOwner(tank, owner);
        db.permissions.grant(tank, otherUser, Permission.Add);
        Collection<GameUser> users = db.permissions.getUsersWithPermissionsOn(tank);
        assertThat(users.size(), is(2));
        assertTrue(users.contains(owner));
        assertTrue(users.contains(otherUser));
        db.permissions.revoke(tank, otherUser, Permission.Add);
        assertThat(users.size(), is(1));
        assertTrue(users.contains(owner));
        assertFalse(users.contains(otherUser));
    }
}