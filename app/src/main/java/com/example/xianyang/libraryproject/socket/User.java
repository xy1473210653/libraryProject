package com.example.xianyang.libraryproject.socket;

public class User {
    String username;//名字
    String passWd;//密码
    String sex;//性别
    String telnumber;//电话
    String work;//工作
    int ftimes;//逾期次数
    int crdscore;//信用积分
    String time;//注册时间
    int books_count;//已借书籍
    boolean isbor;//是否可借

    public User(String username, String passWd, String sex, String telnumber, String work, int ftimes, int crdscore, String time, int books_count, boolean isbor, String faceId) {
        this.username = username;
        this.passWd = passWd;
        this.sex = sex;
        this.telnumber = telnumber;
        this.work = work;
        this.ftimes = ftimes;
        this.crdscore = crdscore;
        this.time = time;
        this.books_count = books_count;
        this.isbor = isbor;
        this.faceId = faceId;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassWd(String passWd) {
        this.passWd = passWd;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getTelnumber() {
        return telnumber;
    }

    public void setTelnumber(String telnumber) {
        this.telnumber = telnumber;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public int getFtimes() {
        return ftimes;
    }

    public void setFtimes(int ftimes) {
        this.ftimes = ftimes;
    }

    public int getCrdscore() {
        return crdscore;
    }

    public void setCrdscore(int crdscore) {
        this.crdscore = crdscore;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getBooks_count() {
        return books_count;
    }

    public void setBooks_count(int books_count) {
        this.books_count = books_count;
    }

    public boolean isIsbor() {
        return isbor;
    }

    public void setIsbor(boolean isbor) {
        this.isbor = isbor;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    String faceId;
    public User(String username, String passWd) {
        this.username = username;
        this.passWd = passWd;
    }

    public String getUsername() {
        return username;
    }

    public String getPassWd() {
        return passWd;
    }
}
