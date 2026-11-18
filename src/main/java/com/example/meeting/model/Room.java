package com.example.meeting.model;

/**
 * 会议室实体
 */
public class Room {

    private String id;
    private String name;
    private String floor;
    private String location;
    private String capacity;

    public Room() {
    }

    public Room(String id, String name, String floor, String location, String capacity) {
        this.id = id;
        this.name = name;
        this.floor = floor;
        this.location = location;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }
}