package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static Connection con = null;
    private static ConnectDB instance = new ConnectDB();

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() {
        try {
            // Thay đổi "your_password" thành mật khẩu SQL Server của bạn
            String url = "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";
            String user = "sa";
            String password = "123456";
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() {
        return con;
    }

    public static void main(String[] args) {
    ConnectDB.getInstance().connect();
    if (ConnectDB.getConnection() != null) {
        System.out.println("Kết nối CSDL thành công tuyệt đối!");
    } else {
        System.out.println("Kết nối thất bại, hãy kiểm tra lại cấu hình!");
    }
}

    

}

