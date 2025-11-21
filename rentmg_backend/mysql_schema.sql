CREATE DATABASE IF NOT EXISTS rentmg CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'rentmg_user'@'%' IDENTIFIED BY 'rentmg_pass';
GRANT ALL PRIVILEGES ON rentmg.* TO 'rentmg_user'@'%';
FLUSH PRIVILEGES;

USE rentmg;
-- Tables will be created by Flask-Migrate, but you can bootstrap with the ORM by running 'flask db init && flask db migrate && flask db upgrade'
