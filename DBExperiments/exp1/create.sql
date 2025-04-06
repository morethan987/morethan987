-- Active: 1743866907532@@127.0.0.1@3306
-- 建库
CREATE DATABASE BBSYS
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 验证库是否存在
USE BBSYS; -- 使用数据库

-- 建表

-- 1. 用户表
CREATE TABLE `user` (
    user_id INT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role ENUM('user','host','admin') NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 房东信息表
CREATE TABLE `host_info` (
    host_id INT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    bank_account VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

-- 3. 民宿表
CREATE TABLE `property` (
    property_id INT PRIMARY KEY,
    host_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    address VARCHAR(200) NOT NULL,
    FOREIGN KEY (host_id) REFERENCES host_info(host_id)
);

-- 4. 设施表
CREATE TABLE `amenity` (
    amenity_id INT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- 5. 民宿-设施关联表
CREATE TABLE `property_amenity` (
    property_id INT NOT NULL,
    amenity_id INT NOT NULL,
    PRIMARY KEY (property_id, amenity_id),
    FOREIGN KEY (property_id) REFERENCES property(property_id),
    FOREIGN KEY (amenity_id) REFERENCES amenity(amenity_id)
);

-- 6. 订单表
CREATE TABLE `orders` (
    order_id INT PRIMARY KEY,
    user_id INT NOT NULL,
    property_id INT NOT NULL,
    room_id INT NOT NULL,
    checkin_date DATE NOT NULL,
    checkout_date DATE NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status ENUM('pending','confirmed','canceled','completed') DEFAULT 'pending',
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (property_id, room_id) REFERENCES room(property_id, room_id)
);

-- 7. 支付表
CREATE TABLE `payment` (
    payment_id INT PRIMARY KEY,
    order_id INT UNIQUE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    method ENUM('credit_card','alipay','wechat') NOT NULL,
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- 8. 房间表
CREATE TABLE `room` (
    room_id INT,
    area INT NOT NULL,
    status ENUM('available','booked','maintenance') DEFAULT 'available',
    bed_type ENUM('single','double','queen','king') NOT NULL,
    property_id INT NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (room_id, property_id),
    FOREIGN KEY (property_id) REFERENCES property(property_id)
);
