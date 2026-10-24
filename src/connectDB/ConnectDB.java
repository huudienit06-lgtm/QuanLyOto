
package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {

    private static Connection con = null;
    private static final ConnectDB instance = new ConnectDB();

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        return instance;
    }

    // Mở kết nối SQL Server
    public void connect() {
        try {
            if (con != null && !con.isClosed()) {
                return;
            }

            String url = "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=a1;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;";

            String user = "sa";
            String password = "123456";

            con = DriverManager.getConnection(url, user, password);

            System.out.println("Kết nối SQL Server thành công!");

        } catch (SQLException e) {
            con = null;
            System.out.println("Lỗi kết nối SQL Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Lấy kết nối, tự mở nếu chưa kết nối
    public static Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                instance.connect();
            }
        } catch (SQLException e) {
            System.out.println("Không kiểm tra được kết nối: " + e.getMessage());
        }

        return con;
    }

    // Đóng kết nối
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                con = null;
            }
        }
    }

    public static void main(String[] args) {
        Connection connection = ConnectDB.getConnection();

        if (connection != null) {
            System.out.println("Kết nối CSDL thành công!");
        } else {
            System.out.println("Kết nối thất bại, hãy kiểm tra cấu hình!");
        }
    }
}

