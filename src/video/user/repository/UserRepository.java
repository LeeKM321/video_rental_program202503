package video.user.repository;

import video.jdbc.DBConnectionManager;
import video.movie.domain.Movie;
import video.order.domain.Order;
import video.user.domain.Grade;
import video.user.domain.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {

    private static final Map<Integer, User> userDatabase = new HashMap<>();

    // 회원 추가 기능
    public void addUser(User user) {
        // 2. 실행하고자 하는 SQL을 문자열로 작성
        // sql에 특정 변수의 값이나 객체의 필드값이 들어가야 한다면 ?로 자리를 표시해 놓으세요.
        String sql = "INSERT INTO users VALUES(user_seq.NEXTVAL,?,?,?)";

        // try with resources
        // 자바 8버전부터 제공되는 문법.
        // AutoCloseable 인터페이스를 구현한 객체를 대상으로 자동 close를 처리.
        // try 옆에 괄호를 열고, try에서 사용할 객체를 생성하면 된다.
        try(Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 1. DB에 접속을 할 수 있게 해주는 객체를 받아오자.

            // 3. SQL 실행 객체 받아오기 (PreparedStatement)

            // 4. sql이 미완성이면 물음표를 채우세요. 앞에서부터 순차적으로 채우세요.
            // 물음표를 채울 때는 타입을 잘 확인하세요.
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPhoneNumber());
            pstmt.setString(3, user.getGrade().toString());

            // 5. sql 실행 명령을 내리세요.
            // executeUpdate(): INSERT, UPDATE, DELETE
            // executeQuery(): SELECT
            pstmt.executeUpdate();

            // 6. pstmt, conn 객체를 해제하자
//            pstmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 이름을 통해 회원 검색 -> 동명이인이 있다면 모두 리턴하기 위해 List 리턴 타입 설정
    public List<User> findUserByName(String userName) {
        List<User> userList = new ArrayList<>();

        String sql = "SELECT * FROM users WHERE user_name = ?";

        try (Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userName);

            // SELECT의 조회 결과를 들고 있는 ResultSet
            // ResultSet의 next()를 통해 한 행을 지목.
            ResultSet rs = pstmt.executeQuery();

            // rs.next()는 조회된 행이 존재하면 true를 주면서 해당 행을 지목합니다.
            // 한 행씩 지목하며, 더이상 조회된 행이 없다면 false를 리턴합니다.
            while (rs.next()) {
                User user = new User(
                        rs.getString("user_name"),
                        rs.getString("phone_number"),
                        Grade.valueOf(rs.getString("grade"))
                );
                user.setUserNumber(rs.getInt("user_number"));

                userList.add(user);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return userList;
    }

    public void deleteUser(int delUserNum) {
        String sql = "DELETE FROM users WHERE user_number = ?";

        try(Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, delUserNum);

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public User findUserByNumber(int userNumber) {
        return userDatabase.get(userNumber);
    }
}

















