package edu.unh.cs.cs619.bulletzone.datalayer.user;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import edu.unh.cs.cs619.bulletzone.datalayer.BulletZoneData;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityType;
import edu.unh.cs.cs619.bulletzone.datalayer.core.Status;
import edu.unh.cs.cs619.bulletzone.datalayer.item.GameItemRepository;

public class GameUserRepository implements EntityRepository {
    HashMap<Integer, GameUser> userMap = new HashMap<>();
    HashMap<String, GameUser> usernameToUserMap = new HashMap<>();
    BulletZoneData data;

    final int iterations = 65536;
    final int keySize = 128;
    final int saltSize = 16;

    /**
     * @return A collection of all ItemCategories in the database
     */
    public Collection<GameUser> getUsers() { return userMap.values(); }

    /**
     * @param userID    ID of the user to get
     * @return  GameUser corresponding to passed ID
     */
    public GameUser getUser(int userID) { return userMap.get(userID); }

    public GameUser getUser(String username) { return usernameToUserMap.get(username); }

    /**
     * Returns a new user, or null if an active user with the passed username already exists.
     * @param name  New user's screen name
     * @param username  User's username for the purpose of logging-in/authorizing
     * @param password  User's password for the purpose of logging-in/authorizing
     * @return  New GameUser object corresponding to the newly created user, or null if already
     *          exists. Any database errors result in exceptions.
     */
    public GameUser createUser(String name, String username, String password) {
        if (getUser(username) != null)
            return null;

        GameUserRecord newRecord = new GameUserRecord(name, username);
        GameUser newUser = null;

        //The following is adapted from https://www.baeldung.com/java-password-hashing
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[saltSize];
        random.nextBytes(salt);

        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keySize);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            newRecord.passwordHash = hash;
            newRecord.passwordSalt = salt;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to attempt password creation!", e);
        }

        try {
            Connection dataConnection = data.getConnection();
            if (dataConnection == null)
                return null;

            // Create base item
            newRecord.insertInto(dataConnection);
            dataConnection.close();
            newUser = new GameUser(newRecord);
            userMap.put(newUser.getId(), newUser);
            usernameToUserMap.put(newRecord.username, newUser);
        } catch (SQLException e) {
            throw new IllegalStateException("Error while creating user!", e);
        }
        System.out.println("New user " + username + " added with ID " + newUser.getId());
        return newUser;
    }

    /**
     * Returns the GameUser associated with a given username if the password matches
     * @param username  Username for the desired user
     * @param password  Password for the desired user
     * @return  GameUser corresponding to the username/password, or
     *          null if not found or wrong password
     */
    public GameUser validateLogin(String username, String password) {
        Connection dataConnection = data.getConnection();
        if (dataConnection == null)
            return null;

        GameUserRecord userRecord = null;
        try {
            //use a PreparedStatement to avoid SQL injection via the username.
            String selectString =
                "SELECT * FROM User u, Entity e WHERE u.EntityID = e.EntityID AND e.StatusID != "
                    + Status.Deleted.ordinal() + " AND u.Username = ?";
            PreparedStatement statement = dataConnection.prepareStatement(selectString);
            // Read users that aren't deleted
            statement.setString(1, username);
            ResultSet userResult = statement.executeQuery();
            if (userResult.next()) //else, is empty result list
            {
                userRecord = new GameUserRecord(userResult);
            }
            dataConnection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to access user table for password validation!", e);
        }
        if (userRecord == null)
            return null;

        //The following is adapted from https://www.baeldung.com/java-password-hashing
        try {
            byte[] salt = userRecord.passwordSalt;
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keySize);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            if (Arrays.equals(hash, userRecord.passwordHash))
                return getUser(userRecord.getID()); //matches!
            //else fall through
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to attempt password validation!", e);
        }

        return null;
    }

    @Override
    public GameUser getTarget(int userID) { return getUser(userID); }

    @Override
    public EntityType getTargetType() { return EntityType.User; }

    @Override
    public AbstractMap<Integer, GameUser> getEntities() { return userMap; }

    //----------------------------------END OF PUBLIC METHODS--------------------------------------

    /**
     * Reads the database and fills the HashMaps as appropriate. Intended to be called once
     * at time of initialization.
     * @param bzData        reference to BulletZoneData class to use for SQL queries
     * @param gameItemRepo  reference to already-initialized GameItemRepository
     */
    public void refresh(BulletZoneData bzData, GameItemRepository gameItemRepo) {
        data = bzData;
        usernameToUserMap.clear();
        userMap.clear();
        Connection dataConnection = data.getConnection();
        if (dataConnection == null)
            return;

        try {
            Statement statement = dataConnection.createStatement();
            // Read users that aren't deleted
            ResultSet userResult = statement.executeQuery(
                    "SELECT * FROM User u, Entity e WHERE u.EntityID = e.EntityID AND e.StatusID != " + Status.Deleted.ordinal());
            while (userResult.next()) {
                GameUserRecord rec = new GameUserRecord(userResult);
                GameUser user = new GameUser(rec);
                userMap.put(user.getId(), user);
                usernameToUserMap.put(rec.username, user);
            }
            dataConnection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read static info!", e);
        }
    }

}
