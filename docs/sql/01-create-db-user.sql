-- RoomMates: veritabanı ve uygulama kullanıcısı.
-- Kullanım: mysql -u root -p < docs/sql/01-create-db-user.sql
-- 'BURAYA_PAROLA' yer tutucusunu çalıştırmadan önce kendi parolanızla değiştirin
-- ve aynı değeri .env.local içindeki DB parolasına yazın. Gerçek parolayı repoya koymayın.

CREATE DATABASE IF NOT EXISTS roommates
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'roommates'@'localhost' IDENTIFIED BY 'BURAYA_PAROLA';

GRANT ALL PRIVILEGES ON roommates.* TO 'roommates'@'localhost';
FLUSH PRIVILEGES;
