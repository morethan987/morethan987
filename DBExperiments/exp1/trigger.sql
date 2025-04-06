USE BBSYS;

-- 触发器：删除某个民宿的时候，删除该民宿的所有房间
CREATE TRIGGER delete_property_rooms
BEFORE DELETE ON property
FOR EACH ROW
BEGIN
    DELETE FROM room WHERE property_id = OLD.property_id;
END;