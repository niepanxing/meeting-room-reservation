package com.example.meeting.service;

import com.example.meeting.model.Booking;
import com.example.meeting.model.Room;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会议室预约业务服务：会议室列表、预约增删查、冲突检测、JSON持久化
 */
@Service
public class MeetingService {

    /** 会议室列表 */
    private static final List<Room> ROOMS = new ArrayList<>();
    static {
        ROOMS.add(new Room("room-001", "星辰厅", "3F", "3楼东侧", "40人"));
        ROOMS.add(new Room("room-002", "创新厅", "5F", "5楼中厅", "25人"));
        ROOMS.add(new Room("room-003", "灵感厅", "2F", "2楼西侧", "12人"));
    }

    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    /** 内存中的预约记录 */
    private final List<Booking> bookings = new ArrayList<>();
    /** 数据文件路径 */
    private File dataFile;

    public MeetingService(ObjectMapper objectMapper, AppProperties appProperties) {
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    /**
     * 初始化：加载已有预约数据
     */
    @PostConstruct
    public void init() {
        try {
            String dataDir = appProperties.getDataDir();
            File dir = new File(dataDir);
            if (!dir.isAbsolute()) {
                dir = new File(System.getProperty("user.dir"), dataDir);
            }
            Files.createDirectories(Paths.get(dir.toURI()));
            dataFile = new File(dir, "bookings.json");

            if (dataFile.exists()) {
                List<Booking> loaded = objectMapper.readValue(dataFile, new TypeReference<List<Booking>>() {
                });
                bookings.addAll(loaded);
            }
        } catch (IOException e) {
            // 初始化失败不影响启动，下次写入会重建
            System.out.println("[MeetingService] 加载预约数据失败: " + e.getMessage());
        }
    }

    /** 获取会议室列表 */
    public List<Room> listRooms() {
        return new ArrayList<>(ROOMS);
    }

    /**
     * 查询某日某会议室的预约
     */
    public synchronized List<Booking> listBookings(String roomId, String date) {
        return bookings.stream()
                .filter(b -> b.getRoomId().equals(roomId) && b.getDate().equals(date))
                .sorted(Comparator.comparing(Booking::getStartTime))
                .collect(Collectors.toList());
    }

    /**
     * 创建预约（带冲突检测）
     */
    public synchronized Booking createBooking(Booking request) throws IllegalArgumentException {
        // ---- 参数校验 ----
        String roomId = trim(request.getRoomId());
        String date = trim(request.getDate());
        String startTime = trim(request.getStartTime());
        String endTime = trim(request.getEndTime());
        String title = trim(request.getTitle());

        if (roomId == null || date == null || startTime == null || endTime == null || title == null) {
            throw new IllegalArgumentException("会议室、日期、起止时间、会议主题均为必填项");
        }
        boolean roomExists = ROOMS.stream().anyMatch(r -> r.getId().equals(roomId));
        if (!roomExists) {
            throw new IllegalArgumentException("会议室不存在");
        }
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("日期格式不正确（应为 yyyy-MM-dd）");
        }
        if (!startTime.matches("\\d{2}:\\d{2}") || !endTime.matches("\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("时间格式不正确（应为 HH:mm）");
        }
        if (startTime.compareTo(endTime) >= 0) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }

        // ---- 冲突检测：同一会议室、同一日期下时间段重叠 ----
        for (Booking b : bookings) {
            boolean sameRoom = b.getRoomId().equals(roomId);
            boolean sameDate = b.getDate().equals(date);
            boolean overlap = startTime.compareTo(b.getEndTime()) < 0
                    && endTime.compareTo(b.getStartTime()) > 0;
            if (sameRoom && sameDate && overlap) {
                throw new IllegalArgumentException("该时间段已被预约（" + b.getStartTime()
                        + " - " + b.getEndTime() + "，主题：" + b.getTitle() + "）");
            }
        }

        // ---- 组装并保存 ----
        Booking booking = new Booking();
        booking.setId("bk-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        booking.setRoomId(roomId);
        booking.setDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setTitle(title);
        booking.setOrganizer(trim(request.getOrganizer()));
        booking.setPhone(trim(request.getPhone()));
        booking.setNote(trim(request.getNote()));
        booking.setCreateTime(LocalDateTime.now());

        bookings.add(booking);
        save();
        return booking;
    }

    /**
     * 取消预约
     */
    public synchronized boolean deleteBooking(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        boolean removed = bookings.removeIf(b -> b.getId().equals(id));
        if (removed) {
            save();
        }
        return removed;
    }

    /**
     * 查询某会议室某日某时间点是否已被预约
     */
    public synchronized boolean isOccupied(String roomId, String date, String time) {
        return bookings.stream().anyMatch(b -> b.getRoomId().equals(roomId)
                && b.getDate().equals(date)
                && b.getStartTime().compareTo(time) < 0
                && b.getEndTime().compareTo(time) > 0);
    }

    /** 根据时间获取对应预约（用于格子着色） */
    public synchronized Booking findBookingAt(String roomId, String date, String time) {
        return bookings.stream().filter(b -> b.getRoomId().equals(roomId)
                && b.getDate().equals(date)
                && b.getStartTime().compareTo(time) < 0
                && b.getEndTime().compareTo(time) > 0).findFirst().orElse(null);
    }

    private String trim(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** 持久化到 JSON 文件 */
    private synchronized void save() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, bookings);
        } catch (IOException e) {
            System.out.println("[ERROR] 保存预约数据失败: " + e.getMessage());
        }
    }
}