package video.order.repository;

import video.jdbc.DBConnectionManager;
import video.movie.domain.ChargePolicy;
import video.order.domain.Order;
import video.user.domain.Grade;
import video.user.domain.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Orders 테이블의 CRUD 연산을 담당하는 클래스
// CRUD: Create Read Update Delete
public class OrderRepository {

    // 대여 프로세스를 하나의 트랜잭션으로 묶어서 처리.
    public User processRental(Order order) {
        Connection conn = null;
        try {
            conn = DBConnectionManager.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작.

            // 1. 영화 대여 처리 (orders 테이블에 insert)
            addOrder(conn, order);

            // 2. 영화 상태 업데이트 (rental = 'N')
            updateMovieRentalStatus(conn, order.getMovie().getSerialNumber(), "N");

            // 3. 사용자의 총 결제금액 계산
            int totalCharges = calculateTotalCharges(conn, order);

            // 4. 필요하다면 사용자 등급 업데이트 (기준치에 충족되는 회원만)
            Grade before = order.getUser().getGrade(); // 기존 등급을 얻어옴.
            order.getUser().setTotalPaying(totalCharges); // 총 결제금액으로 등급을 재조정
            if (order.getUser().getGrade() != before) { // 재조정 후 등급에 변화가 생겼다면
                updateUserGrade(conn, order.getUser()); // DB에 update
            }

            conn.commit(); // 트랜잭션 커밋
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback(); // 오류 발생 시 롤백
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                // autoCommit 원래대로 활성화.
                if (conn != null)  conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return order.getUser();
    }

    public List<Map<String, Object>> showRentalListByUserNumber(int userNumber) {
        List<Map<String, Object>> rentalList = new ArrayList<>();
        String sql = "SELECT " +
                "o.order_id, m.serial_number, m.movie_name, o.order_date, o.return_date " +
                "FROM orders o " +
                "JOIN movies m ON o.serial_number = m.serial_number " +
                "WHERE o.user_number = ? " +
                "AND o.return_status = 'N' " +
                "ORDER BY o.order_date ASC";
        try(Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userNumber);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", rs.getInt("order_id"));
                map.put("serialNumber", rs.getInt("serial_number"));
                map.put("movieName", rs.getString("movie_name"));
                map.put("orderDate", rs.getDate("order_date"));
                map.put("returnDate", rs.getDate("return_date"));
                rentalList.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rentalList;
    }

    public List<Map<String, Object>> showImpossibleRentalList() {
        String sql = "SELECT " +
                "m.movie_name, u.user_name, u.phone_number, o.return_date " +
                "FROM movies m " +
                "JOIN orders o ON m.serial_number = o.serial_number " +
                "JOIN users u ON o.user_number = u.user_number " +
                "WHERE m.rental = 'N' " +
                "ORDER BY o.return_date ASC";

        List<Map<String, Object>> rentalList = new ArrayList<>();
        try(Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("movieName", rs.getString("movie_name"));
                row.put("userName", rs.getString("user_name"));
                row.put("phoneNumber", rs.getString("phone_number"));
                row.put("returnDate", rs.getDate("return_date"));
                rentalList.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rentalList;
    }

    private void updateUserGrade(Connection conn, User user) throws Exception {
        String sql = "UPDATE users SET grade = ? WHERE user_number = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getGrade().toString());
            pstmt.setInt(2, user.getUserNumber());
            pstmt.executeUpdate();
        }
    }

    private int calculateTotalCharges(Connection conn, Order order) throws Exception {
        String sql = "SELECT " +
                "u.user_number, u.user_name, " +
                "o.order_id, m.pub_year, o.order_date " +
                "FROM users u " +
                "JOIN orders o " +
                "ON u.user_number = o.user_number " +
                "JOIN movies m ON o.serial_number = m.serial_number " +
                "WHERE u.user_number = " + order.getUser().getUserNumber();
        int totalCharges = 0;
        try(PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int pubYear = rs.getInt("pub_year");
                LocalDate orderDate = rs.getDate("order_date").toLocalDate();
                int charge = ChargePolicy.calculateDvdCharge(pubYear, orderDate);
                totalCharges += charge;
            }
        }
        return totalCharges;
    }

    private void updateMovieRentalStatus(
            Connection conn,
            int serialNumber,
            String possible) throws Exception {
        String sql = "UPDATE movies SET rental = ? WHERE serial_number = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, possible);
            pstmt.setInt(2, serialNumber);
            pstmt.executeUpdate();
        }
    }

    public void updateReturnProcess(
            int orderId,
            int serialNumber,
            String possible) {
        Connection conn = null;
        try {
            conn = DBConnectionManager.getConnection();
            conn.setAutoCommit(false);
            // 영화 대여 가능 상태를 Y로 변경
            updateMovieRentalStatus(conn, serialNumber, possible);

            // 대여 반납 상태를 Y로 변경
            updateReturnStatus(conn, orderId, possible);

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateReturnStatus(
            Connection conn,
            int orderId,
            String possible) throws Exception {
        String sql = "UPDATE orders SET return_status = ? WHERE order_id = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, possible);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        }
    }

    // 새 대여 주문 추가
    private void addOrder(Connection conn, Order order) throws Exception {
        String sql = "INSERT INTO orders VALUES(order_seq.NEXTVAL, ?, ?, ?, ?, ?)";

        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, order.getUser().getUserNumber());
            pstmt.setInt(2, order.getMovie().getSerialNumber());
            pstmt.setDate(3, Date.valueOf(order.getOrderDate()));
            pstmt.setDate(4, Date.valueOf(order.getReturnDate()));
            pstmt.setString(5, "N");

            pstmt.executeUpdate();

        }
    }



}
