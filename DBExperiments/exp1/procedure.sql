-- 查询目前可预订的民宿和房间信息
DELIMITER //
USE BBSYS; -- 使用数据库

CREATE PROCEDURE GetAvailableRooms()
BEGIN
    -- 查询可预订的房间信息，关联property表获取民宿详情
    SELECT 
        p.property_id,
        p.title,
        p.address,
        r.room_id,
        r.area,
        r.bed_type,
        r.price_per_night,
        r.status
    FROM 
        room r
    JOIN 
        property p ON r.property_id = p.property_id
    WHERE 
        r.status = 'available'
    ORDER BY 
        p.property_id, r.room_id;
END //

DELIMITER ;