package video.order.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class OverduePolicy {

    private static final int BASE_OVERDUE_CHARGE = 300;

    // 연체 일자 계산
    public static int calculateOverdueDay(LocalDate returnDate) {
        LocalDate now = LocalDate.now(); // 현재 날짜
        if (returnDate.isBefore(now)) { // 반납 날짜가 오늘보다 이르다 -> 반납이 늦음 (연체!)
            return (int) ChronoUnit.DAYS.between(returnDate, now);
        }
        return 0;
    }

    // 연체료 계산
    public static int calculateOverdueCharge(LocalDate returnDate) {
        int overdueDay = calculateOverdueDay(returnDate);
        return overdueDay * BASE_OVERDUE_CHARGE;
    }


}

















