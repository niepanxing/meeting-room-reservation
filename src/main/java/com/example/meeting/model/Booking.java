package com.example.meeting.model;

import java.time.LocalDateTime;

/**
 * 预约记录实体
 */
public class Booking {

    /** 预约唯一ID */
    private String id;
    /** 会议室ID */
    private String roomId;
    /** 预约日期 yyyy-MM-dd */
    private String date;
    /** 开始时间 HH:mm */
    private String startTime;
    /** 结束时间 HH:mm */
    private String endTime;
    /** 会议主题 */
    private String title;
    /** 预约人 */
    private String organizer;
    /** 联系电话 */
    private String phone;
    /** 备注 */
    private String note;
    /** 创建时间 */
    private LocalDateTime createTime;

    public Booking() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}