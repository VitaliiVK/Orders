package dataBase;

import java.sql.*;
import java.util.Scanner;

/*
Создать проект «База данных заказов». Создать таблицы «Товары» , «Клиенты» и «Заказы».
Написать код для добавления новых клиентов, товаров и оформления заказов.
 */
public class Main {

    static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/ordersDb";
    static final String DB_USER = "root"; //пользователь
    static final String DB_PASSWORD = "testpass"; // пароль

    static Connection conn;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            try {
                // create connection
                conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
                initDB();

                while (true) {
                    System.out.println("1: add product");
                    System.out.println("2: add client");
                    System.out.println("3: add order");
                    System.out.println("4: view all");
                    System.out.print("-> ");

                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            addProduct(sc);
                            break;
                        case "2":
                            addClient(sc);
                            break;
                        case "3":
                            addOrder(sc);
                            break;
                        case "4":
                            viewAll();
                            break;
                        default:
                            return;
                    }
                }
            } finally {
                sc.close();
                if (conn != null) conn.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }
    }

    private static void initDB() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS Products");
            st.execute("CREATE TABLE Products " +
                    "(prod_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "prod_name VARCHAR(30) NOT NULL UNIQUE KEY, " +
                    "prod_prise INT NOT NULL, " +
                    "prod_balance INT NOT NULL" +
                    ")");

            st.execute("DROP TABLE IF EXISTS Clients");
            st.execute("CREATE TABLE Clients" +
                    "(cli_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "cli_name VARCHAR(30) DEFAULT NULL, " +
                    "cli_company VARCHAR(30) DEFAULT NULL, " +
                    "cli_email VARCHAR(30) NOT NULL UNIQUE KEY, " +
                    "cli_phone INT NOT NULL UNIQUE KEY" +
                    ")");

            st.execute("DROP TABLE IF EXISTS Orders");
            st.execute("CREATE TABLE Orders" +
                    "(ord_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "client_id INT NOT NULL " +
                                "REFERENCES Clients(cli_id) " + //при добалении заказа будет проверено наличие такого клиента
                                "ON UPDATE CASCADE " + //при изменении cli_id изменися и client_id
                                "ON DELETE RESTRICT, " + //удалить запись с cli_id нельзя пока есть запись с client_id
                    "product_id INT NOT NULL " +
                                "REFERENCES Products(prod_id) " + //при добалении заказа будет проверено наличие такого продукта
                                "ON UPDATE CASCADE " + //при изменении prod_id изменися и product_id
                                "ON DELETE RESTRICT," + //удалить запись с prod_id нельзя пока есть запись с product_id
                    "product_count INT NOT NULL, " +
                    "ord_prise INT DEFAULT NULL, " +
                    "ord_date TIMESTAMP NOT NULL" +
                    ")");
        }
    }

    private static void addProduct(Scanner sc) throws SQLException{
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        System.out.print("Enter prise: ");
        String sPrise = sc.nextLine();
        int prise = Integer.parseInt(sPrise);
        System.out.print("Enter balance: ");
        String sBalance = sc.nextLine();
        int balance = Integer.parseInt(sBalance);

        try(PreparedStatement ps = conn.prepareStatement("INSERT INTO Products (prod_name, prod_prise, prod_balance)" +
                                                            " VALUES(?, ?, ?)");) {
            ps.setString(1, name);
            ps.setInt(2, prise);
            ps.setInt(3, balance);
            ps.executeUpdate();
        }
    }

    private static void addClient(Scanner sc) throws SQLException{
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        System.out.print("Enter company: ");
        String company = sc.nextLine();
        System.out.print("Enter email: ");
        String email = sc.nextLine();
        System.out.print("Enter phone: ");
        String sPhone = sc.nextLine();
        int phone = Integer.parseInt(sPhone);

        try(PreparedStatement ps = conn.prepareStatement("INSERT INTO Clients (cli_name, cli_company, cli_email, cli_phone)" +
                                                            " VALUES(?, ?, ?, ?)");) {
            ps.setString(1, name);
            ps.setString(2, company);
            ps.setString(3, email);
            ps.setInt(4, phone);
            ps.executeUpdate();
        }
    }

    private static void addOrder(Scanner sc) throws SQLException{
        System.out.print("Enter client phone: ");
        String phone = sc.nextLine();
        System.out.print("Enter product name: ");
        String name = sc.nextLine();
        System.out.print("Enter product count: ");
        String sProductCount = sc.nextLine();
        int product_count = Integer.parseInt(sProductCount);

        int client_id = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT cli_id FROM Clients WHERE cli_phone = \""+phone+"\"")) {
            try (ResultSet rs = ps.executeQuery()) {//курсор
                if(rs.next()) {
                    client_id = Integer.parseInt(rs.getString(1));
                }
            }
        }
        int product_id = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT prod_id FROM Products WHERE prod_name = \""+name+"\"")) {
            try (ResultSet rs = ps.executeQuery()) {//курсор
                if(rs.next()) {
                    product_id = Integer.parseInt(rs.getString(1));
                }
            }
        }
        int ord_prise = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT prod_prise FROM Products WHERE prod_name = \""+name+"\"")) {
            try (ResultSet rs = ps.executeQuery()) {//курсор
                if(rs.next()) {
                    ord_prise = Integer.parseInt(rs.getString(1)) * product_count;
                }
            }
        }
        try(PreparedStatement ps = conn.prepareStatement("INSERT INTO Orders (client_id, product_id, product_count, ord_prise)" +
                                                        " VALUES(?, ?, ?, ?)");) {
            ps.setInt(1, client_id);
            ps.setInt(2, product_id);
            ps.setInt(3, product_count);
            ps.setInt(4, ord_prise);
            ps.executeUpdate();
        }
    }

    private static void viewAll()throws SQLException{
        printSelect("SELECT * FROM Clients");
        printSelect("SELECT * FROM Products");
        printSelect("SELECT * FROM Orders");
    }

    private static void printSelect(String select) throws SQLException{
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            // table of data representing a database result set,
            try (ResultSet rs = ps.executeQuery()) {//курсор
                // can be used to get information about the types and properties of the columns in a ResultSet object
                ResultSetMetaData md = rs.getMetaData(); //описание курсора, с оинформацией о таблице

                for (int i = 1; i <= md.getColumnCount(); i++)
                    System.out.printf("%15s", md.getColumnName(i));
                System.out.println();

                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        System.out.printf("%15s", rs.getString(i));
                    }
                    System.out.println();
                }
            }
        }
    }
}
