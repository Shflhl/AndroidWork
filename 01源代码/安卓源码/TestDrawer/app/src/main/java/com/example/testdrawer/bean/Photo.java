package com.example.testdrawer.bean;

public class Photo {
    private String _id;
    private String name;
    private int size;
    private String content;
    private String detect_info;

    public String getDetect_info() {
        return detect_info;
    }

    public void setDetect_info(String detect_info) {
        this.detect_info = detect_info;
    }

    public Photo() {
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }



    private String createDate;

    public int get__v() {
        return __v;
    }

    public void set__v(int __v) {
        this.__v = __v;
    }

    private int __v;

    public Photo(String name, int size, String content, String createDate) {
        this.name = name;
        this.size = size;
        this.content = content;
        this.createDate = createDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public int getSize() {
        return size;
    }

    public String getContent() {
        return content;
    }

    public String getCreateDate() {
        return createDate;
    }
}
