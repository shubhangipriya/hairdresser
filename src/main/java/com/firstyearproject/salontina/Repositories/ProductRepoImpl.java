package com.firstyearproject.salontina.Repositories;

import com.firstyearproject.salontina.Models.Item;
import com.firstyearproject.salontina.Models.Treatment;
import com.firstyearproject.salontina.Services.DatabaseLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductRepoImpl implements ProductRepo {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private PreparedStatement pstmt;
    private boolean repoTaskResult = false;
    private Statement stmt;

    @Autowired
    MySQLConnector mySQLConnector;

    @Autowired
    DatabaseLogger databaseLogger;

    //Asbjørn
    @Override
    public boolean createItem(Item item) {
        try {
            String statement =  "INSERT INTO salon_tina_database.items " +
                                "(items_name, items_price, items_description, items_active, items_quantity) " +
                                "VALUES (?, ?, ?, TRUE, ?)";

            Connection connection = mySQLConnector.openConnection();
            pstmt = null;
            pstmt = connection.prepareStatement(statement);
            pstmt.setString(1, item.getProductName());
            pstmt.setDouble(2, item.getProductPrice());
            pstmt.setString(3, item.getProductDescription());
            pstmt.setInt(4, item.getItemQuantity());

            log.info(String.valueOf(pstmt));

            pstmt.executeUpdate();
            mySQLConnector.closeConnection();
            repoTaskResult = true;

            databaseLogger.writeToLogFile(statement);
        } catch (SQLException e) {
            e.printStackTrace();
            repoTaskResult = false;
        }
        return repoTaskResult;
    }

    //Asbjørn
    @Override
    public boolean createTreatment(Treatment treatment) {
        try {
            String statement =  "INSERT INTO salon_tina_database.treatments " +
                                "(treatments_name, treatments_price, treatments_description, treatments_duration, treatments_active) " +
                                "VALUES (?, ?, ?, ?, TRUE)";

            Connection connection = mySQLConnector.openConnection();
            pstmt = null;
            pstmt = connection.prepareStatement(statement);
            pstmt.setString(1, treatment.getProductName());
            pstmt.setDouble(2, treatment.getProductPrice());
            pstmt.setString(3, treatment.getProductDescription());
            pstmt.setInt(4, treatment.getTreatmentDuration());

            log.info(String.valueOf(pstmt));

            pstmt.executeUpdate();
            mySQLConnector.closeConnection();
            repoTaskResult = true;

            databaseLogger.writeToLogFile(statement);
        } catch (SQLException e) {
            e.printStackTrace();
            repoTaskResult = false;
        }
        return repoTaskResult;
    }


    //Mike
    public List findAllTreatments() {
        try {
            String statement = "SELECT * FROM treatments";

            Connection connection = mySQLConnector.openConnection();
            pstmt = null;
            pstmt = connection.prepareStatement(statement);
            ResultSet rs = pstmt.executeQuery();
            ArrayList treatments = new ArrayList();
            while (rs.next()) {
                Treatment t = new Treatment();
                t.setProductId(rs.getInt(1));
                t.setProductName(rs.getString(2));
                t.setProductPrice(rs.getDouble(3));
                t.setProductDescription(rs.getString(4));
                t.setTreatmentDuration(rs.getInt(5));
                t.setProductActive(rs.getBoolean(6));
                treatments.add(t);
            }

            databaseLogger.writeToLogFile(statement);

            return treatments;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mySQLConnector.closeConnection();
        }
        return null;
    }

    //Asbjørn
    @Override
    public boolean createProductArrayLists(ArrayList<Item> itemArrayList, ArrayList<Treatment> treatmentArrayList) {
        stmt = null;
        String itemQuery =      "SELECT items_id, items_name, items_price, items_description, items_quantity, items_active " +
                                "FROM salon_tina_database.items";

        String treatmentQuery = "SELECT treatments_id, treatments_name, treatments_price, treatments_description, " +
                                "treatments_duration, treatments_active " +
                                "FROM salon_tina_database.treatments";
        try {
            Connection connection = mySQLConnector.openConnection();

            stmt = connection.createStatement();
            ResultSet rsItems = stmt.executeQuery(itemQuery);
            insertIntoItemArrayList(itemArrayList, rsItems);

            ResultSet rsTreatments = stmt.executeQuery(treatmentQuery);
            insertIntoTreatmentArrayList(treatmentArrayList, rsTreatments);

            databaseLogger.writeToLogFile(itemQuery);
            databaseLogger.writeToLogFile(treatmentQuery);

            repoTaskResult = true;
        } catch (SQLException e) {
            e.printStackTrace();
            repoTaskResult = false;
        }
        return repoTaskResult;
    }

    //Asbjørn
    @Override
    public void insertIntoItemArrayList(ArrayList<Item> itemArrayList, ResultSet rsItems) {
        try {
            itemArrayList.clear();
            while (rsItems.next()) {
                int productID = rsItems.getInt("items_id");
                String productName = rsItems.getString("items_name");
                String productDescription = rsItems.getString("items_description");
                double productPrice = rsItems.getDouble("items_price");
                boolean productActive = rsItems.getBoolean("items_active");
                int itemQuantity = rsItems.getInt("items_quantity");
                itemArrayList.add(new Item(productID, productName, productDescription, productPrice, productActive, itemQuantity));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertIntoTreatmentArrayList(ArrayList<Treatment> treatmentArrayList, ResultSet rsTreatments) {
        try {
            treatmentArrayList.clear();
            while (rsTreatments.next()) {
                int productID = rsTreatments.getInt("treatments_id");
                String productName = rsTreatments.getString("treatments_name");
                String productDescription = rsTreatments.getString("treatments_description");
                double productPrice = rsTreatments.getDouble("treatments_price");
                boolean productActive = rsTreatments.getBoolean("treatments_active");
                int treatmentDuration = rsTreatments.getInt("treatments_duration");
                treatmentArrayList.add(new Treatment(productID, productName, productDescription, productPrice, productActive, treatmentDuration));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Asbjørn
    @Override
    public boolean editItem(Item item) {
        try {
            String statement =  "UPDATE salon_tina_database.items " +
                                "SET items_name = ?, items_price = ?, items_quantity = ?, items_description = ?, items_active = ? " +
                                "WHERE items_id = ?";

            Connection connection = mySQLConnector.openConnection();
            pstmt = null;
            pstmt = connection.prepareStatement(statement);
            pstmt.setString(1, item.getProductName());
            pstmt.setDouble(2, item.getProductPrice());
            pstmt.setInt(3, item.getItemQuantity());
            pstmt.setString(4, item.getProductDescription());
            pstmt.setBoolean(5, item.isProductActive());
            pstmt.setInt(6, item.getProductId());

            pstmt.executeUpdate();
            mySQLConnector.closeConnection();
            repoTaskResult = true;

            databaseLogger.writeToLogFile(statement);
        } catch (SQLException e) {
            e.printStackTrace();
            repoTaskResult = false;
        }
        return repoTaskResult;
    }

    //Asbjørn
    @Override
    public boolean editTreatment(Treatment treatment) {
        try {
            String statement =  "UPDATE salon_tina_database.treatments " +
                                "SET treatments_name = ?, treatments_price = ?, treatments_duration, treatments_description = ?, " +
                                "treatments_active = ?, treatments_id = ? " +
                                "WHERE treatments_id = ?";

            Connection connection = mySQLConnector.openConnection();
            pstmt = null;
            pstmt = connection.prepareStatement(statement);
            pstmt.setString(1, treatment.getProductName());
            pstmt.setDouble(2, treatment.getProductPrice());
            pstmt.setInt(3, treatment.getTreatmentDuration());
            pstmt.setString(4, treatment.getProductDescription());
            pstmt.setBoolean(5, treatment.isProductActive());
            pstmt.setInt(6, treatment.getProductId());

            pstmt.executeUpdate();
            mySQLConnector.closeConnection();
            repoTaskResult = true;

            databaseLogger.writeToLogFile(statement);
        } catch (SQLException e) {
            e.printStackTrace();
            repoTaskResult = false;
        }
        return repoTaskResult;
    }
}