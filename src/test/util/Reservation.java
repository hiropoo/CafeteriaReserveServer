package test.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Reservation {
    /* インスタンス変数 */
    private Date startTime; // 予約開始時間
    private Date endTime; // 予約終了時間
    private int cafeNum; // 予約した学食の番号
    private List<Integer> seatNums; // 予約した席の番号
    private List<String> members; // 予約メンバー
    private boolean isArrived; // 予約時間までに来たかどうか

    /* コンストラクタ */
    public Reservation(Date startTime, Date endTime, int cafeNum, List<Integer> seatNums, List<String> members) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.cafeNum = cafeNum;
        this.seatNums = seatNums;
        this.members = members;
        this.isArrived = false;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date date) {
        this.startTime = date;
    }

    public int getCafeNum() {
        return cafeNum;
    }

    public void setCafeNum(int cafeNum) {
        this.cafeNum = cafeNum;
    }

    public List<Integer> getSeatNums() {
        return seatNums;
    }

    public void setSeatNums(List<Integer> seatNums) {
        this.seatNums = seatNums;
    }

    public boolean isArrived() {
        return isArrived;
    }

    public void setArrived(boolean isArrived) {
        this.isArrived = isArrived;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
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

        this.members = members;
        this.cafeNum = cafeNum;
        this.seatNums = seatNums;
        this.isArrived = went;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}
