package video.movie.repository;

import video.common.Condition;
import video.jdbc.DBConnectionManager;
import video.movie.domain.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static video.common.Condition.*;

public class MovieRepository {

    private static final Map<Integer, Movie> movieDatabase = new HashMap<>();

    static {
        insertTestMovieData();
    }

    //테스트 데이터 생성 및 삽입
    private static void insertTestMovieData() {
        Movie movie1 = new Movie("인터스텔라", "미국", 2014);
        Movie movie2 = new Movie("포레스트 검프", "미국", 1994);
        Movie movie3 = new Movie("너의 이름은", "일본", 2017);
        Movie movie4 = new Movie("라라랜드", "미국", 2016);
        Movie movie5 = new Movie("레옹", "프랑스", 1994);
        Movie movie6 = new Movie("어바웃 타임", "영국", 2013);
        Movie movie7 = new Movie("타이타닉", "미국", 1998);
        Movie movie8 = new Movie("인생은 아름다워", "이탈리아", 1999);
        Movie movie9 = new Movie("쇼생크 탈출", "미국", 1995);
        Movie movie10 = new Movie("기생충", "대한민국", 2019);

        movieDatabase.put(movie1.getSerialNumber(), movie1);
        movieDatabase.put(movie2.getSerialNumber(), movie2);
        movieDatabase.put(movie3.getSerialNumber(), movie3);
        movieDatabase.put(movie4.getSerialNumber(), movie4);
        movieDatabase.put(movie5.getSerialNumber(), movie5);
        movieDatabase.put(movie6.getSerialNumber(), movie6);
        movieDatabase.put(movie7.getSerialNumber(), movie7);
        movieDatabase.put(movie8.getSerialNumber(), movie8);
        movieDatabase.put(movie9.getSerialNumber(), movie9);
        movieDatabase.put(movie10.getSerialNumber(), movie10);
    }

    public void addMovie(Movie movie) {
       String sql = "INSERT INTO movies " +
               "(serial_number, movie_name, nation, pub_year) " +
               "VALUES (movie_seq.NEXTVAL, ?, ?, ?)";

       try(Connection conn = DBConnectionManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)) {
           pstmt.setString(1, movie.getMovieName());
           pstmt.setString(2, movie.getNation());
           pstmt.setInt(3, movie.getPubYear());

           pstmt.executeUpdate();

       } catch (SQLException e) {
           e.printStackTrace();
       }

    }

    public List<Movie> searchMovieList(Condition condition, String keyword) throws Exception {
        List<Movie> searchedList = new ArrayList<>();

        String sql = "SELECT * FROM movies";
        if (condition == PUB_YEAR) {
            sql += " WHERE pub_year LIKE ?";
        } else if (condition == NATION) {
            sql += " WHERE nation LIKE ?";
        } else if (condition == TITLE) {
            sql += " WHERE movie_name LIKE ?";
        }
        try(Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (condition != ALL) {
                // LIKE 사용 시 %, _기호를 따옴표 안에 넣어줘야 합니다.
                // ?옆에 %를 쓰는게 아니라, ?를 채울 때 특정 단어에 %를 미리 세팅해서 채워야 합니다.
                pstmt.setString(1, "%" + keyword + "%");
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Movie movie = createMovieFromResultSet(rs);

                searchedList.add(movie);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return searchedList;
    }


    public void deleteMovie(int delMovieNum) {
        String sql = "DELETE FROM movies WHERE serial_number = ?";

        try(Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, delMovieNum);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Movie> searchByRental(boolean possible) {
        List<Movie> searchedList = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE rental = ?";
        try(Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 물음표 채울 때 possible이 true면 Y를, false면 N을 채우겠다.
            pstmt.setString(1, possible ? "Y" : "N");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Movie movie = createMovieFromResultSet(rs);
                searchedList.add(movie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return searchedList;
    }

    // ResultSet에서 추출한 결과를 Movie 객체로 포장해주는 헬퍼 메서드
    private static Movie createMovieFromResultSet(ResultSet rs) throws SQLException {
        Movie movie = new Movie(
                rs.getString("movie_name"),
                rs.getString("nation"),
                rs.getInt("pub_year")
        );
        movie.setRental(rs.getString("rental").equals("Y"));
        movie.setSerialNumber(rs.getInt("serial_number"));
        return movie;
    }

    // 번호에 맞는 영화 객체를 단 하나만 리턴하는 메서드.
    public Movie searchMovie(int movieNumber) {
        return movieDatabase.get(movieNumber);
    }




}



















