package video.order.service;

import video.common.AppService;
import video.movie.domain.Movie;
import video.movie.repository.MovieRepository;
import video.order.domain.Order;
import video.ui.AppUi;
import video.user.domain.User;
import video.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static video.ui.AppUi.*;

public class OrderService implements AppService {

    private final MovieRepository movieRepository = new MovieRepository();
    private final UserRepository userRepository = new UserRepository();

    @Override
    public void start() {
        while (true) {
            orderManagementScreen();
            int selection = inputInteger(">>> ");

            switch (selection) {
                case 1:
                    processOrderDvd();
                    break;
                case 2:
                    processReturnDvd();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("\n### 메뉴를 다시 입력하세요.");
            }


        }
    }


    // DVD 대여 서비스 비즈니스 로직
    private void processOrderDvd() {
        while (true) {
            System.out.println("\n============================ 대여관리 시스템을 실행합니다. ============================");
            System.out.println("[ 1. 대여 가능 DVD 목록 보기 | 2. 대여중인 영화 반납예정일 확인하기 | 3. 이전으로 돌아가기 ]");
            int selection = inputInteger(">>> ");

            switch (selection) {
                case 1:
                    showRentalPossibleList();
                    break;
                case 2:
                    showRentalImpossibleList();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("\n### 메뉴를 다시 입력하세요.");
            }

        }
    }



    // 대여 가능한 DVD 목록 보기
    private void showRentalPossibleList() {
        List<Movie> movieList = movieRepository.searchByRental(true);
        int count = movieList.size();

        List<Integer> movieNums = new ArrayList<>();

        if (count > 0) {
            System.out.printf("\n==================================== 검색 결과 (총 %d건) ====================================\n", count);
            for (Movie movie : movieList) {
                System.out.println(movie);
                movieNums.add(movie.getSerialNumber());
            }
            System.out.println("==========================================================================================");
            System.out.println("### 대여할 DVD의 번호를 입력하세요.");
            int movieNumber = inputInteger(">>> ");

            if (movieNums.contains(movieNumber)) {
                rentalProcess(movieNumber);
            } else {
                System.out.println("\n### 대여가 가능한 영화 목록중에 선택해야 합니다.");
            }
        } else {
            System.out.println("\n### 대여 가능한 DVD가 없습니다 ㅠㅠ");
        }
    }

    private void rentalProcess(int movieNumber) {

        Movie rentalMovie = movieRepository.searchMovie(movieNumber);
        System.out.printf("\n### [%s] DVD를 대여합니다.\n", rentalMovie.getMovieName());
        System.out.println("### 대여자의 이름을 입력하세요.");
        String name = inputString(">>> ");

        List<User> users = userRepository.findUserByName(name);

        if(users.size() > 0) {
            List<Integer> userNums = new ArrayList<>();
            System.out.println("\n===================================== 회원 조회 결과 =====================================");
            for (User user : users) {
                System.out.println(user);
                userNums.add(user.getUserNumber());
            }
            System.out.println("========================================================================================");
            System.out.println("## 대여할 회원의 회원번호를 입력하세요.");
            int userNumber = inputInteger(">>> ");

            if (userNums.contains(userNumber)) {
                // 대여 완료 처리
                User rentalUser = userRepository.findUserByNumber(userNumber); // 렌탈 유저 정보 획득.
                rentalMovie.setRental(true); // 대여상태를 대여중으로 변경
                rentalMovie.setRentalUser(rentalUser); // 영화 객체에 렌탈 유저 등록

                rentalUser.setTotalPaying(rentalMovie.getCharge()); // 영화 대여 금액을 회원 총 결제금액에 누적 갱신

                // 새로운 주문 생성
                Order newOrder = new Order(rentalUser, rentalMovie);
                rentalUser.addOrder(newOrder); // 회원 대여 목록에 주문을 추가.

                String phoneNumber = rentalUser.getPhoneNumber(); // 출력문을 위해 얻은 전화번호

                // lastIndexOf(str): 해당 문자열의 위치를 뒤에서부터 탐색.
                // 뒤에서부터 탐색을 시작해서 "-"을 찾아라 -> 그 "-" 이후로부터 끝까지 추출해라.
                System.out.printf("\n### [%s(%s) 회원님] 대여 처리가 완료되었습니다. 감사합니다!\n"
                        , rentalUser.getUserName(), rentalUser.getPhoneNumber().substring(phoneNumber.lastIndexOf("-") + 1));

                System.out.printf("### 현재 등급: [%s], 총 누적 결제금액: %d원\n", rentalUser.getGrade(), rentalUser.getTotalPaying());

            } else {
                System.out.println("\n### 검색된 회원의 번호를 입력하세요.");
            }

        } else {
            System.out.println("\n### 대여자 정보가 없습니다.");
        }

    }

    // 대여중(대여 불가능한)인 DVD 목록 보기
    private void showRentalImpossibleList() {
        List<Movie> movieList = movieRepository.searchByRental(false);
        int count = movieList.size();

        if (count > 0) {
            System.out.printf("\n==================================== 검색 결과 (총 %d건) ====================================\n", count);
            for (Movie movie : movieList) {
                User rentalUser = movie.getRentalUser();
                String phoneNumber = rentalUser.getPhoneNumber();
                String lastPhoneNumber = phoneNumber.substring(phoneNumber.lastIndexOf("-") + 1);
                System.out.printf("### 영화명: %s, 현재 대여자: %s(%s), 반납예정일: %s\n"
                , movie.getMovieName(), rentalUser.getUserName(), lastPhoneNumber,
                        rentalUser.getOrderList().get(movie.getSerialNumber()).getReturnDate());

            }
            System.out.println("==========================================================================================");

        } else {
            System.out.println("\n### 대여 불가능한 DVD가 없습니다.");
        }

    }

    // DVD 반납 서비스 비즈니스 로직
    private void processReturnDvd() {
        System.out.println("\n============================ 반납 관리 시스템을 실행합니다. ============================");
        System.out.println("### 반납자의 이름을 입력하세요.");
        String name = inputString(">>> ");

        List<User> users = userRepository.findUserByName(name);
        int count = users.size();

        if (count > 0) {
            List<Integer> userNums = new ArrayList<>();
            System.out.printf("\n===================================== 조회 결과(총 %d건) =====================================\n", count);
            for (User user : users) {
                System.out.println(user);
                userNums.add(user.getUserNumber());
            }
            System.out.println("========================================================================================");

            System.out.println("### 반납자의 회원 번호를 입력하세요.");
            int userNumber = inputInteger(">>> ");

            if (userNums.contains(userNumber)) {
                returnProcess(userNumber);
            } else {
                System.out.println("\n### 조회된 회원 번호를 입력하셔야 합니다.");
            }
        } else {
            System.out.println("\n### 반납자의 정보가 없습니다.");
        }
    }

    private void returnProcess(int userNumber) {
        // 매개값으로 전달된 회원 번호를 통해 회원 객체를 받아야 한다. o
        // "XXX 회원님의 대여 목록입니다" 라고 하면서 orderList 내의 모든 객체를 보여주어야 한다. o
        // 반납할 DVD의 번호를 입력받아야 한다. o
        // 입력한 번호가 대여중인 DVD인지 검증해야 한다. (아무 번호나 입력하지 않았는지 확인) o
        // 대여중인 DVD가 맞다면 반납처리를 본격적으로 진행한다. o
        // 영화 객체에서 회원정보를 삭제한다. -> rentalUser 필드 값을 null로 세팅 o
        // 영화 객체의 대여 가능 여부를 변경해야 한다. o
        // 연체료 발생 여부를 확인하여 연체료가 존재한다면 추가로 얼마를 결제하라고 출력문을 띄워야 하고, o
        // 연체료가 없다면 반납이 완료되었다는 출력문을 보여주어야 한다. o
        User returnUser = userRepository.findUserByNumber(userNumber);
        System.out.printf("\n### 현재 [%s] 회원님의 대여 목록입니다.\n", returnUser.getUserName());
        System.out.println("======================================================================================");
        Map<Integer, Order> orderList = returnUser.getOrderList();
        for (int key : orderList.keySet()) {
            System.out.println(orderList.get(key));
        }
        System.out.println("======================================================================================");
        System.out.println("### 반납할 DVD의 번호를 입력하세요.");
        int returnMovieNumber = inputInteger(">>> ");

        if (orderList.containsKey(returnMovieNumber)) {
            // 반납 처리
            Movie returnMovie = movieRepository.searchMovie(returnMovieNumber);
            returnMovie.setRentalUser(null);
            returnMovie.setRental(false);

            Order returnOrder = orderList.get(returnMovieNumber);
            int overdueCharge = returnOrder.getOverdueCharge();
            if (overdueCharge > 0) {
                System.out.printf("\n### 반납이 완료되었습니다. %d원을 추가로 결제해 주세요!\n", overdueCharge);
            } else {
                System.out.println("\n### 반납이 완료되었습니다. 즐거운 하루 되세요!");
            }

        } else {
            System.out.println("\n### 해당 DVD는 반납 대상이 아닙니다.");
        }


    }


}








