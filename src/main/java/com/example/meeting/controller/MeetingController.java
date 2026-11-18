package com.example.meeting.controller;

import com.example.meeting.model.Booking;
import com.example.meeting.model.Room;
import com.example.meeting.service.AppProperties;
import com.example.meeting.service.MeetingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会议室预约 REST 接口
 */
@RestController
@RequestMapping("/api")
public class MeetingController {

    private final MeetingService meetingService;
    private final AppProperties appProperties;

    public MeetingController(MeetingService meetingService, AppProperties appProperties) {
        this.meetingService = meetingService;
        this.appProperties = appProperties;
    }

    /** 会议室列表 */
    @GetMapping("/rooms")
    public ApiResponse<List<Room>> listRooms() {
        return ApiResponse.ok(meetingService.listRooms());
    }

    /** 系统配置（开放时间等） */
    @GetMapping("/config")
    public ApiResponse<Map<String, String>> config() {
        Map<String, String> cfg = new HashMap<>();
        cfg.put("openTime", appProperties.getOpenTime());
        cfg.put("closeTime", appProperties.getCloseTime());
        return ApiResponse.ok(cfg);
    }

    /** 某日某会议室的预约列表 */
    @GetMapping("/bookings")
    public ApiResponse<List<Booking>> listBookings(@RequestParam String roomId, @RequestParam String date) {
        return ApiResponse.ok(meetingService.listBookings(roomId, date));
    }

    /** 创建预约（含冲突检测） */
    @PostMapping("/bookings")
    public ApiResponse<Booking> createBooking(@RequestBody Booking request) {
        try {
            Booking booking = meetingService.createBooking(request);
            return ApiResponse.ok(booking);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /** 取消预约 */
    @DeleteMapping("/bookings/{id}")
    public ApiResponse<Boolean> deleteBooking(@PathVariable String id) {
        boolean ok = meetingService.deleteBooking(id);
        if (ok) {
            return ApiResponse.ok(true);
        }
        return ApiResponse.error(404, "预约记录不存在");
    }
}