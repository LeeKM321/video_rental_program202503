package video.order.domain;

import video.movie.domain.Movie;
import video.user.domain.User;

import java.time.LocalDate;

public class Order {

    private User user;
    private Movie movie;
    private LocalDate orderDate;
    private LocalDate returnDate;

    // 연체료 속성 추가해야됨.
    private int overdueCharge;
    private int overdueDay;

    public Order(User user, Movie movie) {
        this.user = user;
        this.movie = movie;
        this.orderDate = LocalDate.now();
        this.returnDate = this.orderDate.plusDays(3);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public int getOverdueCharge() {
        return overdueCharge;
    }

    public void setOverdueCharge(int overdueCharge) {
        this.overdueCharge = overdueCharge;
    }

    public int getOverdueDay() {
        return overdueDay;
    }

    public void setOverdueDay(int overdueDay) {
        this.overdueDay = overdueDay;
    }

    @Override
    public String toString() {
        this.overdueDay = OverduePolicy.calculateOverdueDay(this);
        this.overdueCharge = OverduePolicy.calculateOverdueCharge(this);
        return
                "## 대여영화번호: " + movie.getSerialNumber() +
                        ", 대여영화명: " + movie.getMovieName() +
                        ", 회원명: " + user.getUserName() + "(" + user.getPhoneNumber() + ")" +
                        ", 대여일자: " + orderDate +
                        ", 반납일자: " + returnDate +
                        ", 연체금액(" + overdueDay + "일): "  + overdueCharge + "원"
                ;
    }

}














