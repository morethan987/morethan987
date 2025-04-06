USE BBSYS;

-- 修改某个民宿房间的面积
UPDATE room
SET area = 150.0
WHERE property_id = 1 AND room_id = 101;

-- 删除某个民宿的所有房间
DELETE FROM room
WHERE property_id = 1;

-- 找出某个民宿所有房间的类型和单价
SELECT bed_type, price_per_night
FROM room
WHERE property_id = 1;

-- 查出所有可预定的民宿的名称
SELECT title
FROM property
WHERE property_id IN (SELECT property_id FROM room WHERE status = 'available');

-- 查询游客jane_smith的预定的房间的所有信息
SELECT r.*
FROM room r
WHERE r.property_id IN (SELECT property_id FROM orders WHERE user_id = (SELECT user_id FROM user WHERE username = 'jane_smith'));

-- 查询民宿1单价在80到120之间的房间的所有信息
SELECT *
FROM room
WHERE property_id = 1 AND price_per_night BETWEEN 80 AND 120;

-- 查询每一个民宿拥有的房间数量
SELECT property_id, COUNT(room_id)
FROM room
GROUP BY property_id;

-- 查询费用在650以上的订单的所有信息
SELECT *
FROM orders
WHERE total_price > 650;

-- 查询民宿1的平均房间单价
SELECT AVG(price_per_night) AS average_price
FROM room
WHERE property_id = 1;

--查询平均房间单价在120到130之间的民宿的所有信息
SELECT *
FROM property
WHERE property_id IN (SELECT property_id FROM room GROUP BY property_id HAVING AVG(price_per_night) BETWEEN 120 AND 130);