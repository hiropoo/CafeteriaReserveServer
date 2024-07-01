package test.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Reservation {
    /* インスタンス変数 */
    private static LocalDateTime startTime; // 予約開始時間
    private static LocalDateTime endTime; // 予約終了時間
    private static int cafeNum; // 予約した学食の番号
    private static List<Integer> seatNums; // 予約した席の番号
    private static List<String> members; // 予約メンバー
    private static boolean isArrived; // 予約時間までに来たかどうか
    
    
    public static LocalDateTime getStartTime() {
        return startTime;
    }
    
    public static void setStartTime(LocalDateTime date) {
        Reservation.startTime = date;
    }
    
    public static LocalDateTime getEndTime() {
        return endTime;
    }

    public static void setEndTime(LocalDateTime endTime) {
        Reservation.endTime = endTime;
    }

    public static int getCafeNum() {
        return cafeNum;
    }

    public static void setCafeNum(int cafeNum) {
        Reservation.cafeNum = cafeNum;
    }

    public static List<Integer> getSeatNums() {
        return seatNums;
    }

    public static void setSeatNums(List<Integer> seatNums) {
        Reservation.seatNums = seatNums;
    }

    public static boolean isArrived() {
        return isArrived;
    }

    public static void setArrived(boolean isArrived) {
        Reservation.isArrived = isArrived;
    }

    public static List<String> getMembers() {
        return members;
    }

    public static void setMembers(List<String> members) {
        Reservation.members = members;
    }

    /* 予約情報をクリア */
    public static void clear() {
        Reservation.startTime = null;
        Reservation.endTime = null;
        Reservation.cafeNum = -1;
        Reservation.seatNums = null;
        Reservation.members = null;
        Reservation.isArrived = false;
    }

    /*
     * サーバからのレスポンスデータから予約情報を設定
     * response = userID,userID,... cafeNum seatNum,seatNum,...
     * startTime endTime went
     */
    public static void fromResponse(String response) throws ParseException {
        String[] splitData = response.split(" ");
        List<String> members = Arrays.asList(splitData[0].split(","))
                .stream()
                .map(member -> member.split(":")[1])
                .toList();

        int cafeNum = Integer.parseInt(splitData[1]);
        List<Integer> seatNums = Arrays.asList(splitData[2].split(","))
                .stream()
                .map(seatNum -> Integer.parseInt(seatNum))
                .toList();
        boolean went = Boolean.parseBoolean(splitData[5]);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(splitData[3], dateFormat);
        LocalDateTime endTime = LocalDateTime.parse(splitData[4], dateFormat);

        Reservation.members = members;
        Reservation.cafeNum = cafeNum;
        Reservation.seatNums = seatNums;
        Reservation.isArrived = went;
        Reservation.startTime = startTime;
        Reservation.endTime = endTime;
    }

}
