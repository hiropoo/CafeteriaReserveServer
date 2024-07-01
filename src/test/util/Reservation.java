package test.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Reservation {
    /* インスタンス変数 */
    private static Date startTime; // 予約開始時間
    private static Date endTime; // 予約終了時間
    private static int cafeNum; // 予約した学食の番号
    private static List<Integer> seatNums; // 予約した席の番号
    private static List<String> members; // 予約メンバー
    private static boolean isArrived; // 予約時間までに来たかどうか
    
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date date) {
        Reservation.startTime = date;
    }
    
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        Reservation.endTime = endTime;
    }

    public int getCafeNum() {
        return cafeNum;
    }

    public void setCafeNum(int cafeNum) {
        Reservation.cafeNum = cafeNum;
    }

    public List<Integer> getSeatNums() {
        return seatNums;
    }

    public void setSeatNums(List<Integer> seatNums) {
        Reservation.seatNums = seatNums;
    }

    public boolean isArrived() {
        return isArrived;
    }

    public void setArrived(boolean isArrived) {
        Reservation.isArrived = isArrived;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        Reservation.members = members;
    }

    /* 予約情報をクリア */
    public void clear() {
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
    public void fromResponse(String response) throws ParseException {
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = dateFormat.parse(splitData[3]);
        Date endTime = dateFormat.parse(splitData[4]);

        Reservation.members = members;
        Reservation.cafeNum = cafeNum;
        Reservation.seatNums = seatNums;
        Reservation.isArrived = went;
        Reservation.startTime = startTime;
        Reservation.endTime = endTime;
    }

}
