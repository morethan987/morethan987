USE BBSYS

-- 1. 用户表数据
INSERT INTO `user` (user_id, username, password_hash, email, role, created_at) VALUES
(1, 'john_doe', '5f4dcc3b5aa766d61d8327deb882cf99', 'john@example.com', 'user', '2023-01-01 10:00:00'),
(2, 'jane_smith', '5f4dcc3b5aa865d61d8327deb882cf99', 'jane@example.com', 'user', '2023-01-02 11:00:00'),
(3, 'mike_jones', '5f4dcc3b5al765d61d8327deb882cf99', 'mike@example.com', 'host', '2023-01-03 12:00:00'),
(4, 'sarah_wilson', '5f4dcc3b5aa765d61d8377deb882cf99', 'sarah@example.com', 'host', '2023-01-04 13:00:00'),
(5, 'david_brown', '5f4dcc3b5aa765d61d8328deb882cf99', 'david@example.com', 'user', '2023-01-05 14:00:00'),
(6, 'lisa_taylor', '5f4dcc3b5aa765d61d8527deb882cf99', 'lisa@example.com', 'user', '2023-01-06 15:00:00'),
(7, 'robert_garcia', '5f4dcc3a5aa765d61d8327deb882cf99', 'robert@example.com', 'host', '2023-01-07 16:00:00'),
(8, 'emily_martinez', '5f4dcc3b5aa765d61d8327deb882cf89', 'emily@example.com', 'user', '2023-01-08 17:00:00'),
(9, 'admin_user', '5f4dcc3b5aa765d61d8327deb782cf99', 'admin@example.com', 'admin', '2023-01-09 18:00:00'),
(10, 'tom_anderson', '5f4dcc3b5aa765d61d8327deb88dcf99', 'tom@example.com', 'user', '2023-01-10 19:00:00');

-- 2. 房东信息表数据
INSERT INTO `host_info` (host_id, user_id, real_name, bank_account) VALUES
(1, 3, 'Michael Jones', '1234567890123456'),
(2, 4, 'Sarah Wilson', '2345678901234567'),
(3, 7, 'Robert Garcia', '3456789012345678'),
(4, 9, 'Admin User', '4567890123456789'),
(5, 10, 'Tom Anderson', '5678901234567890'),
(6, 2, 'Jane Smith', '6789012345678901'),
(7, 5, 'David Brown', '7890123456789012'),
(8, 6, 'Lisa Taylor', '8901234567890123'),
(9, 8, 'Emily Martinez', '9012345678901234'),
(10, 1, 'John Doe', '0123456789012345');

-- 3. 民宿表数据
INSERT INTO `property` (property_id, host_id, title, address) VALUES
(1, 1, 'Cozy Downtown Apartment', '123 Main St, New York, NY'),
(2, 1, 'Luxury Penthouse', '456 High Ave, New York, NY'),
(3, 2, 'Beachfront Villa', '789 Ocean Dr, Miami, FL'),
(4, 2, 'Mountain Cabin', '101 Forest Rd, Aspen, CO'),
(5, 3, 'City Center Loft', '202 Urban Ave, Chicago, IL'),
(6, 3, 'Quiet Suburban House', '303 Peace Ln, Chicago, IL'),
(7, 4, 'Historic Townhouse', '404 Heritage Blvd, Boston, MA'),
(8, 5, 'Modern Studio', '505 Design St, San Francisco, CA'),
(9, 6, 'Lakeview Cottage', '606 Waterside Dr, Seattle, WA'),
(10, 7, 'Desert Oasis', '707 Sunny Way, Phoenix, AZ');

-- 4. 设施表数据
INSERT INTO `amenity` (amenity_id, name) VALUES
(1, 'WiFi'),
(2, 'Air Conditioning'),
(3, 'Heating'),
(4, 'Kitchen'),
(5, 'TV'),
(6, 'Washer'),
(7, 'Dryer'),
(8, 'Parking'),
(9, 'Pool'),
(10, 'Hot Tub');

-- 5. 民宿-设施关联表数据
INSERT INTO `property_amenity` (property_id, amenity_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9),
(3, 1), (3, 2), (3, 4), (3, 5), (3, 9),
(4, 1), (4, 3), (4, 4), (4, 10),
(5, 1), (5, 2), (5, 3), (5, 5),
(6, 1), (6, 3), (6, 4), (6, 5), (6, 6), (6, 7),
(7, 1), (7, 3), (7, 5), (7, 8),
(8, 1), (8, 2), (8, 3), (8, 4), (8, 5),
(9, 1), (9, 3), (9, 4), (9, 5), (9, 10),
(10, 1), (10, 2), (10, 9);

-- 6. 订单表数据
INSERT INTO `orders` (order_id, user_id, property_id, room_id, checkin_date, checkout_date, total_price, status) VALUES
(1, 1, 3, 102, '2023-02-01', '2023-02-05', 1120.00, 'completed'),
(2, 2, 5, 104, '2023-02-10', '2023-02-12', 320.00, 'completed'),
(3, 5, 1, 102, '2023-03-15', '2023-03-20', 600.00, 'confirmed'),
(4, 6, 7, 102, '2023-04-01', '2023-04-03', 440.00, 'pending'),
(5, 8, 2, 103, '2023-05-10', '2023-05-15', 1750.00, 'confirmed'),
(6, 1, 4, 102, '2023-06-01', '2023-06-07', 1170.00, 'pending'),
(7, 2, 6, 103, '2023-07-05', '2023-07-10', 675.00, 'confirmed'),
(8, 5, 8, 102, '2023-08-12', '2023-08-14', 360.00, 'completed'),
(9, 6, 9, 103, '2023-09-01', '2023-09-05', 840.00, 'canceled'),
(10, 8, 10, 103, '2023-10-10', '2023-10-15', 875.00, 'completed');

-- 7. 支付表数据
INSERT INTO `payment` (payment_id, order_id, amount, method, transaction_time) VALUES
(1, 1, 1120.00, 'credit_card', '2023-01-25 14:30:00'),
(2, 2, 320.00, 'alipay', '2023-02-01 10:15:00'),
(3, 3, 600.00, 'credit_card', '2023-03-01 09:45:00'),
(4, 5, 1750.00, 'wechat', '2023-04-15 16:20:00'),
(5, 7, 675.00, 'credit_card', '2023-06-20 11:30:00'),
(6, 8, 360.00, 'alipay', '2023-08-01 13:10:00'),
(7, 10, 875.00, 'wechat', '2023-09-25 15:45:00'),
(8, 4, 440.00, 'credit_card', '2023-03-20 10:00:00'),
(9, 6, 1170.00, 'alipay', '2023-05-15 14:30:00'),
(10, 9, 840.00, 'credit_card', '2023-08-20 12:00:00');

-- 8. 房间表数据
INSERT INTO `room` (room_id, area, status, bed_type, property_id, price_per_night) VALUES
(101, 25, 'available', 'single', 1, 80.00),-- Property 1 rooms
(102, 30, 'available', 'double', 1, 120.00),
(103, 35, 'booked', 'queen', 1, 150.00),
(104, 40, 'available', 'king', 1, 180.00),
(105, 28, 'maintenance', 'double', 1, 110.00),
(101, 22, 'available', 'single', 2, 75.00),-- Property 2 rooms
(102, 32, 'booked', 'double', 2, 125.00),
(103, 38, 'available', 'queen', 2, 160.00),
(104, 42, 'available', 'king', 2, 190.00),
(105, 30, 'available', 'double', 2, 115.00),
(101, 20, 'available', 'single', 3, 70.00),-- Property 3 rooms
(102, 28, 'available', 'double', 3, 110.00),
(103, 36, 'booked', 'queen', 3, 145.00),
(104, 45, 'maintenance', 'king', 3, 200.00),
(105, 32, 'available', 'double', 3, 120.00),
(101, 24, 'booked', 'single', 4, 78.00),-- Property 4 rooms
(102, 31, 'available', 'double', 4, 122.00),
(103, 37, 'available', 'queen', 4, 155.00),
(104, 41, 'available', 'king', 4, 185.00),
(105, 29, 'booked', 'double', 4, 118.00),
(101, 26, 'available', 'single', 5, 82.00),-- Property 5 rooms
(102, 33, 'available', 'double', 5, 128.00),
(103, 39, 'maintenance', 'queen', 5, 165.00),
(104, 43, 'available', 'king', 5, 195.00),
(105, 31, 'available', 'double', 5, 125.00),
(101, 23, 'available', 'single', 6, 76.00),-- Property 6 rooms
(102, 29, 'booked', 'double', 6, 115.00),
(103, 34, 'available', 'queen', 6, 140.00),
(104, 44, 'available', 'king', 6, 205.00),
(105, 27, 'available', 'double', 6, 105.00),
(101, 21, 'booked', 'single', 7, 72.00),-- Property 7 rooms
(102, 30, 'available', 'double', 7, 118.00),
(103, 35, 'available', 'queen', 7, 152.00),
(104, 40, 'maintenance', 'king', 7, 182.00),
(105, 28, 'available', 'double', 7, 112.00),
(101, 27, 'available', 'single', 8, 85.00),-- Property 8 rooms
(102, 34, 'available', 'double', 8, 130.00),
(103, 38, 'booked', 'queen', 8, 158.00),
(104, 42, 'available', 'king', 8, 192.00),
(105, 33, 'available', 'double', 8, 128.00),
(101, 25, 'available', 'single', 9, 79.00),-- Property 9 rooms
(102, 31, 'maintenance', 'double', 9, 121.00),
(103, 36, 'available', 'queen', 9, 148.00),
(104, 41, 'booked', 'king', 9, 188.00),
(105, 29, 'available', 'double', 9, 119.00),
(101, 24, 'available', 'single', 10, 77.00),-- Property 10 rooms
(102, 32, 'available', 'double', 10, 124.00),
(103, 37, 'available', 'queen', 10, 153.00),
(104, 43, 'available', 'king', 10, 198.00),
(105, 30, 'booked', 'double', 10, 122.00);