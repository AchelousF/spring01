package com.achelous.spring.transaction;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @Auther: fanJiang
 * @Date: Create in 20:47 2018/5/10
 */
public class TransactionManager {

    private Connection connection;

    private DataSource dataSource;

    public TransactionManager() {
        this.dataSource = getDataSource();
    }

    private Connection getConnection() throws SQLException {
        synchronized (this) {
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection;
            }
            this.connection = createConnection();
        }
        return this.connection;
    }
    private Connection createConnection() {
        try {
            Class.forName(dataSource.getDriver());
            return DriverManager.getConnection(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void beginTransaction() throws SQLException {
        Connection connection = getConnection();
        //  获取当前数据库连接对象   若自动提交为关闭   则开启自动提交
        if (connection.getAutoCommit()) {
            connection.setAutoCommit(false);
        }
    }

    public void commit() throws SQLException {
        Connection connection = getConnection();
        connection.commit();
        close(connection);
    }

    public void rollback() throws SQLException {
        Connection connection = getConnection();
        connection.rollback();
        close(connection);
    }

    // TODO   如何关闭与之相关的   statement 及 resultSet
    private void close(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }


    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setDriver("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/gp");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        return dataSource;
    }

}
