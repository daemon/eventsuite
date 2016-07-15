CREATE TABLE IF NOT EXISTS arena
  (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL,
  name VARCHAR(32) NOT NULL,
  world_id INT UNSIGNED NOT NULL,
  server_id INT UNSIGNED NOT NULL,
  base_region_id INT NOT NULL,
  UNIQUE INDEX aname_i(name)) ENGINE=MyISAM;
CREATE TABLE IF NOT EXISTS region
  (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL,
  name VARCHAR(32) NOT NULL,
  xz1 POINT NOT NULL,
  xz2 POINT NOT NULL,
  y1 POINT NOT NULL,
  y2 POINT NOT NULL,
  SPATIAL INDEX xz1_i(xz1),
  SPATIAL INDEX xz2_i(xz2)) ENGINE=MyISAM;
CREATE TABLE IF NOT EXISTS `point`
  (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL,
  name VARCHAR(32) NOT NULL,
  xz POINT NOT NULL,
  y INT NOT NULL,
  SPATIAL INDEX xz_i(xz)) ENGINE=MyISAM;
CREATE TABLE IF NOT EXISTS arena_point_assoc
  (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL,
  arena_id INT UNSIGNED NOT NULL,
  point_id INT UNSIGNED NOT NULL,
  INDEX arena_id_i(arena_id)) ENGINE=MyISAM;
CREATE TABLE IF NOT EXISTS arena_region_assoc
  (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL,
  arena_id INT UNSIGNED NOT NULL,
  region_id INT UNSIGNED NOT NULL,
  INDEX arena_id_i(arena_id)) ENGINE=MyISAM;
CREATE TABLE IF NOT EXISTS server
  (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL,
  name VARCHAR(32) NOT NULL,
  UNIQUE INDEX sname_i(name)) ENGINE=MyISAM;
CREATE TABLE IF NOT EXISTS world
  (id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL,
  name VARCHAR(32) NOT NULL,
  UNIQUE INDEX wname_i(name)) ENGINE=MyISAM;
DROP FUNCTION IF EXISTS CreateEnvelope;
DROP PROCEDURE IF EXISTS InsertArena;
DROP PROCEDURE IF EXISTS InsertArenaPoint;
DROP PROCEDURE IF EXISTS InsertArenaRegion;
DELIMITER $$
CREATE FUNCTION CreateEnvelope(p1 POINT, p2 POINT)
  RETURNS POLYGON DETERMINISTIC
  BEGIN
    DECLARE bounding POLYGON;
    SET bounding = Envelope(LineString(p1, p2));
    RETURN bounding;
  END$$
CREATE PROCEDURE InsertArena(arena_name VARCHAR(32), world_name VARCHAR(32), server_name VARCHAR(32),
  xz1 POINT, xz2 POINT, y1 POINT, y2 POINT, OUT status INT)
  begin_proc:
  BEGIN
    DECLARE s_id INT UNSIGNED;
    DECLARE w_id INT UNSIGNED;
    DECLARE r_id INT UNSIGNED;
    DECLARE a_id INT UNSIGNED;
    SET a_id = (SELECT id FROM arena WHERE name=arena_name);
    SET status = 1;
    IF (a_id IS NOT NULL) THEN
      LEAVE begin_proc;
    END IF;
    INSERT INTO server(name) VALUES (server_name) ON DUPLICATE KEY UPDATE name=server_name;
    INSERT INTO world(name) VALUES (world_name) ON DUPLICATE KEY UPDATE name=world_name;
    INSERT INTO region(name, xz1, xz2, y1, y2) VALUES ('base', xz1, xz2, y1, y2);
    SET s_id = (SELECT id FROM server WHERE name=server_name);
    SET w_id = (SELECT id FROM world WHERE name=world_name);
    SET r_id = (SELECT id FROM region WHERE name='base');
    INSERT INTO arena(name, world_id, server_id, base_region_id) VALUES (arena_name, w_id, s_id, r_id);
    SET a_id = (SELECT id FROM arena WHERE name=arena_name);
    INSERT INTO arena_region_assoc(arena_id, region_id) VALUES (a_id, r_id);
    SET status = 0;
  END$$
CREATE PROCEDURE InsertArenaPoint(arena_name VARCHAR(32), point POINT, name VARCHAR(32), y INT, OUT status INT)
  begin_proc:
  BEGIN
    DECLARE a_id INT UNSIGNED;
    DECLARE p_id INT UNSIGNED;
    SET status = 1;
    SET a_id = (SELECT id FROM arena WHERE arena.name=arena_name);
    IF (a_id IS NULL) THEN
      LEAVE begin_proc;
    END IF;
    SET p_id = (SELECT point_id FROM point INNER JOIN arena_point_assoc ON arena_point_assoc.point_id=point.id WHERE
      arena_id=a_id AND point.name=name);
    IF (p_id IS NULL) THEN
      INSERT INTO point(name, xz, y) VALUES(name, point, y);
      SET p_id = LAST_INSERT_ID();
      INSERT INTO arena_point_assoc(point_id, arena_id) VALUES (p_id, a_id);
      SET status = 0;
    ELSE
      SET status = 2;
    END IF;
  END$$
CREATE PROCEDURE InsertArenaRegion(arena_name VARCHAR(32), region_name VARCHAR(32), xz1 POINT, xz2 POINT, y1 POINT, y2 POINT, OUT status INT)
  begin_proc:
  BEGIN
    DECLARE a_id INT UNSIGNED;
    DECLARE r_id INT UNSIGNED;
    DECLARE xz12 POINT;
    DECLARE xz22 POINT;
    DECLARE y12 POINT;
    DECLARE y22 POINT;
    DECLARE polyXZ1 POLYGON;
    DECLARE polyXZ2 POLYGON;
    DECLARE polyXY1 POLYGON;
    DECLARE polyXY2 POLYGON;
    DECLARE within BOOL;
    DECLARE try_id INT UNSIGNED;
    SET status = 1;
    SELECT id, base_region_id FROM arena WHERE name=arena_name INTO a_id, r_id;
    IF (a_id IS NULL) THEN
      LEAVE begin_proc;
    END IF;
    SELECT region.id FROM region INNER JOIN arena_region_assoc ON arena_region_assoc.region_id=region.id
      WHERE arena_id=a_id AND region.name=region_name INTO try_id;
    IF (try_id IS NOT NULL) THEN
      SET status = 3;
      LEAVE begin_proc;
    END IF;
    SELECT region.xz1, region.xz2, region.y1, region.y2 FROM region WHERE id=r_id INTO xz12, xz22, y12, y22;
    IF (xz12 IS NULL) THEN
      LEAVE begin_proc;
    END IF;
    SET polyXZ1 = CreateEnvelope(xz1, xz2);
    SET polyXZ2 = CreateEnvelope(xz12, xz22);
    SET polyXY1 = CreateEnvelope(y1, y2);
    SET polyXY2 = CreateEnvelope(y12, y22);
    SET within = MBRWithin(polyXZ1, polyXZ2) && MBRWithin(polyXY1, polyXY2);
    IF (within = 1) THEN
      INSERT INTO region(name, xz1, xz2, y1, y2) VALUES (region_name, xz1, xz2, y1, y2);
      INSERT INTO arena_region_assoc(arena_id, region_id) VALUES (a_id, LAST_INSERT_ID());
      SET status = 0;
    ELSE
      SET status = 2;
    END IF;
  END$$
