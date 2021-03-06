package com.firstyearproject.salontina.Repositories;

import com.firstyearproject.salontina.Models.*;

import com.firstyearproject.salontina.Tools.DatabaseLogger;
import com.firstyearproject.salontina.Tools.MySQLConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepoImpl implements UserRepo{

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private boolean userRepoTaskResult;

    @Autowired
    MySQLConnector mySQLConnector;

    @Autowired
    DatabaseLogger databaseLogger;

    /**
     * Luca
     * @return List of phonenumbers on newsletterlist as Strings.
     */
    @Override
    public List<String> getNewsletterList(){
        log.info("getNewsletterList method started...");

        List<String> phonenumbers = new ArrayList<>();

        String statement =
                "SELECT " +
                "(SELECT users_phonenumber " +
                "FROM users " +
                "WHERE users_id = newsletter.users_id) " +
                "AS phonenumber FROM newsletter;";

        try {
            PreparedStatement pstmt = mySQLConnector.openConnection().prepareStatement(statement);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){
                phonenumbers.add(rs.getString(1));
            }

            databaseLogger.writeToLogFile(statement);

            return phonenumbers;
        } catch (SQLException e) {
            e.printStackTrace();
        }  finally {
            mySQLConnector.closeConnection();
        }
        return null;
    }

    /**
     * Luca
     * @return List of Reminders for next day.
     */
    @Override
    public List<Reminder> getReminderList(){
        log.info("getReminderList method started...");

        List<Reminder> reminderList = new ArrayList<>();

        String statement =
                "SELECT (SELECT users.users_phonenumber FROM users WHERE users.users_id = bookings.users_id) " +
                "AS booking_phonenumber, (SELECT users.users_fullName FROM users WHERE users.users_id = bookings.users_id) " +
                "AS booking_name, bookings_date, bookings_time " +
                "FROM bookings " +
                "WHERE bookings_date BETWEEN DATE_ADD(CURDATE(), INTERVAL 1 day) AND DATE_ADD(CURDATE(), INTERVAL 1 day);";

        try {
            PreparedStatement pstmt = mySQLConnector.openConnection().prepareStatement(statement);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){
                Reminder r = new Reminder();
                r.setReminderPhonenumber(rs.getString(1));
                r.setReminderUsername(rs.getString(2));
                r.setReminderDate(rs.getDate(3));
                r.setReminderTime(rs.getString(4));
                reminderList.add(r);
            }

            databaseLogger.writeToLogFile(statement);

            return reminderList;
        } catch (SQLException e) {
            e.printStackTrace();
        }  finally {
            mySQLConnector.closeConnection();
        }
        return null;
    }

    /**
     * Jonathan
     */
    @Override
    public boolean addUser(User user){
        log.info("addUser method started...");

        String statement =
                "INSERT INTO users " +
                "(users_fullName, users_phonenumber, users_email, users_preferences, users_password) " +
                "VALUES (?, ?, ?, ?, ?)";
        try{
            PreparedStatement pstms = mySQLConnector.openConnection().prepareStatement(statement);
            pstms.setString(1, user.getUsername());
            pstms.setInt(2, user.getUserPhonenumber());
            pstms.setString(3, user.getUserEmail());
            pstms.setString(4, user.getUserPreference());
            pstms.setString(5, user.getUserPassword());
            pstms.executeUpdate();

            databaseLogger.writeToLogFile(statement);

            if(user.isUserNewsLetter()){
                addToNewsletter(user);
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mySQLConnector.closeConnection();
        }
        return false;
    }

    /**
     * Luca
     */
    private void addToNewsletter(User user){
        log.info("addToNewsletter method started...");
        User foundUser = authenticateUser(new LoginToken(user.getUserEmail(), user.getUserPassword()));
        subscribeNewsletter(foundUser.getUserId());
    }

    /**
     * Asbjørn
     */
    @Override
    public boolean subscribeNewsletter(int userId) {
        log.info("subscribeNewsletter method started...");
        String statement = "INSERT INTO newsletter (users_id) VALUES (?)";
        userRepoTaskResult = newsletterQueries(userId, statement);
        return userRepoTaskResult;
    }

    /**
     * Asbjørn
     */
    @Override
    public boolean unsubscribeNewsletter(int userId) {
        log.info("unsubscribeNewsletter method started...");
        String statement = "DELETE FROM newsletter WHERE users_id = ?";
        userRepoTaskResult = newsletterQueries(userId, statement);
        return userRepoTaskResult;
    }

    /**
     * Asbjørn
     * Both subscribe and unsubscribe use the same execute code
     */
    private boolean newsletterQueries(int userId, String statement) {
        log.info("newsletterQueries method started...");
        try{
            PreparedStatement pstmt = mySQLConnector.openConnection().prepareStatement(statement);
            pstmt.setInt(1, userId);

            pstmt.executeUpdate();

            userRepoTaskResult = true;
            databaseLogger.writeToLogFile(statement);

        } catch (SQLException e) {
            e.printStackTrace();
            userRepoTaskResult = false;
        }  finally {
            mySQLConnector.closeConnection();
        }
        return userRepoTaskResult;
    }

    /**
     * Mike
     */
    @Override
    public List<User> findAllUsers() {
        log.info("findAllUsers method started...");
        List<User> users = new ArrayList();
        String statement =
                "SELECT * " +
                "FROM users";
        try {
            PreparedStatement pstmt = mySQLConnector.openConnection().prepareStatement(statement);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("users_id"));
                u.setUsername(rs.getString("users_fullName"));
                u.setUserPassword(rs.getString("users_password"));
                u.setUserPhonenumber(rs.getInt("users_phonenumber"));
                u.setUserEmail(rs.getString("users_email"));
                u.setUserPreference(rs.getString("users_preferences"));
                users.add(u);
            }

            databaseLogger.writeToLogFile(statement);

            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mySQLConnector.closeConnection();
        }
        return null;
    }

    /**
     * Mike
     */
    @Override
    public User findUserById(int userid) {
        log.info("findUserById method started...");
        User user = null;
        String statement =
                "SELECT * " +
                "FROM users " +
                "WHERE users_id = ?";
        try {
            PreparedStatement pstmt = mySQLConnector.openConnection().prepareStatement(statement);
            pstmt.setInt(1, userid);
            ResultSet rs = pstmt.executeQuery();

            user = generateUserFromResultSet(rs);

            databaseLogger.writeToLogFile(statement);

            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mySQLConnector.closeConnection();
        }
        return null;
    }

    /**
     * Luca
     */
    private void generateUserFromResultSet(User user, ResultSet rs) throws SQLException {
        user.setUserId(rs.getInt(1));
        user.setUsername(rs.getString(2));
        user.setUserPassword(rs.getString(3));
        user.setUserPhonenumber(rs.getInt(4));
        user.setUserEmail(rs.getString(5));
        user.setUserPreference(rs.getString(6));
    }

    /**
     * Jonathan
     */
    @Override
    public boolean editUser(User user) {
        log.info("editUser method started...");
        String statement;

        if(!user.getUserPassword().equals("")) {
            statement =  "UPDATE users " +
                    "SET users_fullName = ?, " +
                    "users_phonenumber = ?, " +
                    "users_email = ?, " +
                    "users_preferences = ?, " +
                    "users_password = ? " +
                    "WHERE users_id = ?;";
            try{

                Connection connection = mySQLConnector.openConnection();
                PreparedStatement pstms = connection.prepareStatement(statement);
                pstms.setString(1, user.getUsername());
                pstms.setInt(2,user.getUserPhonenumber());
                pstms.setString(3,user.getUserEmail());
                pstms.setString(4,user.getUserPreference());
                pstms.setString(5, user.getUserPassword());
                pstms.setInt(6,user.getUserId());
                pstms.executeUpdate();

                databaseLogger.writeToLogFile(statement);

                return true;
            } catch (Exception E){
                E.printStackTrace();
            }
        } else {
            statement =  "UPDATE users " +
                    "SET users_fullName = ?, " +
                    "users_phonenumber = ?, " +
                    "users_email = ?, " +
                    "users_preferences = ? " +
                    "WHERE users_id = ?;";
            try{

                Connection connection = mySQLConnector.openConnection();
                PreparedStatement pstms = connection.prepareStatement(statement);
                pstms.setString(1, user.getUsername());
                pstms.setInt(2,user.getUserPhonenumber());
                pstms.setString(3,user.getUserEmail());
                pstms.setString(4,user.getUserPreference());
                pstms.setInt(5,user.getUserId());
                pstms.executeUpdate();

                databaseLogger.writeToLogFile(statement);

                return true;
            } catch (Exception E){
                E.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Mike
     */
    @Override
    public boolean editUserHistory(User user) {
        log.info("editUserHistory method started...");
        String statement =
                "UPDATE users " +
                "SET users_preferences = ? " +
                "WHERE users_id = ?";
        try{
            PreparedStatement pstmt = mySQLConnector.openConnection().prepareStatement(statement);
            pstmt.setString(1,user.getUserPreference());
            pstmt.setInt(2,user.getUserId());
            pstmt.executeUpdate();

            databaseLogger.writeToLogFile(statement);

            return true;
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            mySQLConnector.closeConnection();
        }
        return false;
    }

    /**
     * Luca
     * @return User if either phonenumber or email and password is correct.
     */
    @Override
    public User authenticateUser(LoginToken loginToken){
        log.info("authenticateUser method started...");

        String statement =
                "SELECT * " +
                "FROM users " +
                "WHERE users_phonenumber = ? " +
                "OR users_email = ? " +
                "AND users_password = ?;";

        try {
            PreparedStatement pstmt = mySQLConnector.openConnection().prepareStatement(statement);

            pstmt.setString(1, loginToken.getLoginTokenUsername());
            pstmt.setString(2, loginToken.getLoginTokenUsername());
            pstmt.setString(3, loginToken.getLoginTokenPassword());

            ResultSet rs = pstmt.executeQuery();

            databaseLogger.writeToLogFile(statement);

            return generateUserFromResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mySQLConnector.closeConnection();
        }
        return null;
    }

    /**
     * Luca
     * @return List of user roles for specific user as Strings.
     */
    private List<String> getUserRoles(int userId){
        log.info("getUserRoles method started...");

        List<String> userRoles = new ArrayList<>();

        String statement =
                "SELECT * " +
                "FROM users_roles " +
                "WHERE users_id = ?;";

        try {
            PreparedStatement pstmt = mySQLConnector.openConnection().prepareStatement(statement);

            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){
                userRoles.add(rs.getString(2));
            }

            databaseLogger.writeToLogFile(statement);

            return userRoles;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Luca
     * @return List of previous bookings for specific user as Bookings.
     */
    private List<Booking> getUserHistory(int userId){
        log.info("getUserHistory method started...");

        List<Booking> userHistory = new ArrayList<>();

        String statement =
                "SELECT * " +
                "FROM bookings " +
                "WHERE users_id = ?;";

        try {
            PreparedStatement pstmt = mySQLConnector.openConnection().prepareStatement(statement);

            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){
                Booking booking = new Booking();

                booking.setBookingId(rs.getInt(1));
                booking.setBookingUserId(rs.getInt(2));
                booking.setBookingDate(rs.getDate(4));
                booking.setBookingComment(rs.getString(5));
                booking.setBookingTreatmentList(getTreatmentsForBooking(booking.getBookingId()));

                userHistory.add(booking);
            }

            databaseLogger.writeToLogFile(statement);

            return userHistory;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Mike
     */
    public boolean isNewsletter(int userId) {
        String statement = "SELECT * FROM newsletter WHERE users_id = ?";

        try {
            PreparedStatement pstmt = mySQLConnector.openConnection().prepareStatement(statement);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Luca
     */
    private List<Treatment> getTreatmentsForBooking(int bookingId){
        log.info("getTreatmentsForBooking method started...");

        List<Treatment> treatmentList = new ArrayList<>();

        String statement =
                "SELECT " +
                "(SELECT treatments.treatments_name " +
                "FROM treatments WHERE treatments_id = bookings_treatment.treatments_id) " +
                "AS treatment_name " +
                "FROM bookings_treatment " +
                "WHERE bookings_id = ?;";

        try {
            PreparedStatement pstmt = mySQLConnector.openConnection().prepareStatement(statement);

            pstmt.setInt(1, bookingId);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){
                Treatment treatment = new Treatment();

                treatment.setProductName(rs.getString(1));

                treatmentList.add(treatment);
            }

            databaseLogger.writeToLogFile(statement);

            return treatmentList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Luca
     */
    private User generateUserFromResultSet(ResultSet rs) throws SQLException{
        if(!rs.next()){
            return null;
        }
        User user = new User();

        generateUserFromResultSet(user, rs);
        user.setUserRoles(getUserRoles(user.getUserId()));
        user.setUserHistory(getUserHistory(user.getUserId()));
        user.setUserNewsLetter(isNewsletter(user.getUserId()));

        return user;
    }

    /**
     * Mike & Asbjørn
     */
    @Override
    public boolean deleteUser(int userId) {
        log.info("deleteUser method started...");
        String statement =
                "INSERT INTO users_archive " +
                "SELECT users_id, users_fullName, users_phonenumber, users_email, users_preferences " +
                "FROM users " +
                "WHERE users_id = ?";
        try {
            Connection connection = mySQLConnector.openConnection();
            PreparedStatement pstm = connection.prepareStatement(statement);
            pstm.setInt(1, userId);
            pstm.executeUpdate();

            pstm = connection.prepareStatement("DELETE FROM users WHERE users_id = ?");
            pstm.setInt(1, userId);
            pstm.executeUpdate();
            pstm.close();

            databaseLogger.writeToLogFile(statement);

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mySQLConnector.closeConnection();
        }
        return false;
    }
}
