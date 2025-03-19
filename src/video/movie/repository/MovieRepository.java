package video.movie.repository;

import video.common.Condition;
import video.jdbc.DBConnectionManager;
import video.movie.domain.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
        if (condition == PUB_YEAR) {
            return searchByPubYear(keyword);
        } else if (condition == NATION) {
            return searchByNation(keyword);
        } else if (condition == TITLE) {
            return searchByTitle(keyword);
        } else {
            return searchAll();
        }
    }

    private List<Movie> searchAll() {
        List<Movie> searchedList = new ArrayList<>();
        for (int key : movieDatabase.keySet()) {
            Movie movie = movieDatabase.get(key);
            searchedList.add(movie);
        }
        return searchedList;
    }

    // 문자열을 숫자로 변환하는 과정에서 예외 발생 가능성이 있기 때문에 throws 추가.
    private List<Movie> searchByPubYear(String keyword) throws NumberFormatException {
        List<Movie> searchedList = new ArrayList<>();
        
        // 입력값을 String으로 받았기 때문에 int로 변환해서 비교
        int targetYear = Integer.parseInt(keyword);

        for (int key : movieDatabase.keySet()) {
            Movie movie = movieDatabase.get(key);
            if (movie.getPubYear() == targetYear) {
                searchedList.add(movie);
            }
        }
        return searchedList;
    }

    private List<Movie> searchByNation(String keyword) {
        List<Movie> searchedList = new ArrayList<>();

        for (int key : movieDatabase.keySet()) {
            Movie movie = movieDatabase.get(key);
            if (movie.getNation().equals(keyword)) {
                searchedList.add(movie);
            }
        }
        return searchedList;
    }

    private List<Movie> searchByTitle(String keyword) {
        List<Movie> searchedList = new ArrayList<>();

        for (int key : movieDatabase.keySet()) {
            Movie movie = movieDatabase.get(key);
            if (movie.getMovieName().equals(keyword)) {
                searchedList.add(movie);
            }
        }
        return searchedList;
    }


    public Movie deleteMovie(int delMovieNum) {
        return movieDatabase.remove(delMovieNum);
    }

    public List<Movie> searchByRental(boolean possible) {
        List<Movie> searchedList = new ArrayList<>();

        if (possible) { // 대여 가능한 Movie들만 거르기
            for (int key : movieDatabase.keySet()) {
                Movie movie = movieDatabase.get(key);
                if (!movie.isRental()) {
                    searchedList.add(movie);
                }
            }

        } else { // 이미 대여중인 Movie들만 거르기
            for (int key : movieDatabase.keySet()) {
                Movie movie = movieDatabase.get(key);
                if (movie.isRental()) {
                    searchedList.add(movie);
                }
            }
        }

        return searchedList;
    }

    // 번호에 맞는 영화 객체를 단 하나만 리턴하는 메서드.
    public Movie searchMovie(int movieNumber) {
        return movieDatabase.get(movieNumber);
    }




}



















