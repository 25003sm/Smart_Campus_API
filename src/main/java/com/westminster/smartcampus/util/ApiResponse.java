package com.westminster.smartcampus.util;

import com.westminster.smartcampus.model.ApiLink;

import java.util.List;

public class ApiResponse<T> {
    private String status;
    private String message;
    private String path;
    private long timestamp;
    private T data;
    private List<ApiLink> links;

    public ApiResponse() {
    }

    public ApiResponse(String status, String message, String path, T data, List<ApiLink> links) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = System.currentTimeMillis();
        this.data = data;
        this.links = links;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ApiLink> getLinks() {
        return links;
    }

    public void setLinks(List<ApiLink> links) {
        this.links = links;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
