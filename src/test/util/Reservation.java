package test.util;

import java.util.Date;

public class Reservation {
    /* インスタンス変数 */
    private Date date;          // 予約日
    private int cafeNum;        // 予約した学食の番号
    private int[] seatNums;     // 予約した席の番号
    private String[] members;   // 予約メンバー
    private boolean isArrived; // 予約時間までに来たかどうか

    /* コンストラクタ */
    public Reservation(Date date, int cafeNum, int[] seatNums, String[] members) {
        this.date = date;
        this.cafeNum = cafeNum;
        this.seatNums = seatNums;
        this.members = members;
        this.isArrived = false;
    }


    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public int getCafeNum() {
        return cafeNum;
    }
    public void setCafeNum(int cafeNum) {
        this.cafeNum = cafeNum;
    }
    public int[] getSeatNums() {
        return seatNums;
    }
    public void setSeatNums(int[] seatNums) {
        this.seatNums = seatNums;
    }
    public String[] getMembers() {
        return members;
    }
    public void setMembers(String[] members) {
        this.members = members;
    }
    public boolean isArrived() {
        return isArrived;
    }
    public void setArrived(boolean isArrived) {
        this.isArrived = isArrived;
    }
    

}
