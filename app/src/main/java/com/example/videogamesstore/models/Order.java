package com.example.videogamesstore.models;

public class Order {
    private String userId, userEmail, gameId, name, imgurl, platform, postcode, orderDateTime;
    private int qty;
    private double price;

    public Order() {

    }

    public Order(String userId, String userEmail, String gameId, String name, int qty, double price, String platform,
                 String postcode, String orderDateTime, String imgurl) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.gameId = gameId;
        this.name = name;
        this.qty = qty;
        this.price = price;
        this.platform = platform;
        this.postcode = postcode;
        this.orderDateTime = orderDateTime;
        this.imgurl = imgurl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getOrderDateTime() {
        return orderDateTime;
    }

    public void setOrderDateTime(String orderDateTime) {
        this.orderDateTime = orderDateTime;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }
}
