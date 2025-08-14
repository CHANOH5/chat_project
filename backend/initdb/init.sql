-- MySQL dump 10.13  Distrib 8.0.25, for Win64 (x86_64)
--
-- Host: localhost    Database: chatstudy01
-- ------------------------------------------------------
-- Server version	8.0.25

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `tb_chatroom`
--

CREATE USER 'study01'@'%' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON chatstudy01.* TO 'study01'@'%';
FLUSH PRIVILEGES;

DROP TABLE IF EXISTS `tb_chatroom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_chatroom` (
  `id` bigint NOT NULL COMMENT '채팅방 고유ID',
  `name` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '채팅방 이름',
  `created_at` datetime NOT NULL COMMENT '생성일',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_chatroom`
--

LOCK TABLES `tb_chatroom` WRITE;
/*!40000 ALTER TABLE `tb_chatroom` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_chatroom` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_chatroom_user`
--

DROP TABLE IF EXISTS `tb_chatroom_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_chatroom_user` (
  `id` bigint NOT NULL COMMENT '고유ID',
  `chatroom_id` bigint NOT NULL COMMENT '채팅방 고유ID',
  `user_id` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '사용자ID',
  `joined_at` datetime NOT NULL COMMENT '입장 시각',
  PRIMARY KEY (`id`),
  KEY `tb_chatroom_user_room_FK` (`chatroom_id`),
  KEY `tb_chatroom_user_user_FK` (`user_id`),
  CONSTRAINT `tb_chatroom_user_room_FK` FOREIGN KEY (`chatroom_id`) REFERENCES `tb_chatroom` (`id`),
  CONSTRAINT `tb_chatroom_user_user_FK` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_chatroom_user`
--

LOCK TABLES `tb_chatroom_user` WRITE;
/*!40000 ALTER TABLE `tb_chatroom_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_chatroom_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_message`
--

DROP TABLE IF EXISTS `tb_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_message` (
  `id` bigint NOT NULL COMMENT '메시지ID',
  `chatroom_id` bigint NOT NULL COMMENT '채팅방 ID',
  `sender_id` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '보낸사람ID',
  `content` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '메시지 내용',
  `type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '텍스트(기본)',
  `sent_at` datetime NOT NULL COMMENT '보낸 시각',
  PRIMARY KEY (`id`),
  KEY `tb_message_room_FK` (`chatroom_id`),
  KEY `tb_message_user_FK` (`sender_id`),
  CONSTRAINT `tb_message_room_FK` FOREIGN KEY (`chatroom_id`) REFERENCES `tb_chatroom` (`id`),
  CONSTRAINT `tb_message_user_FK` FOREIGN KEY (`sender_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_message`
--

LOCK TABLES `tb_message` WRITE;
/*!40000 ALTER TABLE `tb_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_message_status`
--

DROP TABLE IF EXISTS `tb_message_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_message_status` (
  `id` bigint NOT NULL COMMENT '고유ID',
  `message_id` bigint NOT NULL COMMENT '메시지ID',
  `user_id` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '사용자ID',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '읽음 여부',
  `read_at` datetime NOT NULL COMMENT '읽은 시각',
  PRIMARY KEY (`id`),
  KEY `tb_message_status_message_FK` (`message_id`),
  KEY `tb_message_status_user_FK` (`user_id`),
  CONSTRAINT `tb_message_status_message_FK` FOREIGN KEY (`message_id`) REFERENCES `tb_message` (`id`),
  CONSTRAINT `tb_message_status_user_FK` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_message_status`
--

LOCK TABLES `tb_message_status` WRITE;
/*!40000 ALTER TABLE `tb_message_status` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_message_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_refresh_token`
--

DROP TABLE IF EXISTS `tb_refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_refresh_token` (
  `id` bigint NOT NULL COMMENT '고유 ID',
  `user_id` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '사용자ID',
  `refresh_toke` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '리프레시 토큰',
  `expires_at` datetime NOT NULL COMMENT '만료일',
  `created_at` datetime NOT NULL COMMENT '생성일',
  PRIMARY KEY (`id`),
  KEY `tb_refresh_token_FK` (`user_id`),
  CONSTRAINT `tb_refresh_token_FK` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_refresh_token`
--

LOCK TABLES `tb_refresh_token` WRITE;
/*!40000 ALTER TABLE `tb_refresh_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_refresh_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_user`
--

DROP TABLE IF EXISTS `tb_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user` (
  `id` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '아이디',
  `password` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '비밀번호',
  `name` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '이름',
  `nickname` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '별명',
  `email` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '이메일',
  `is_anonymous` int NOT NULL COMMENT '회원가입 여부 (회원 - 0, 비회원 - 1)',
  `role` int NOT NULL COMMENT '권한 (관리자 - 0, 사용자 - 1',
  `profile_image` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '프로필 이미지 경로',
  `status` int NOT NULL DEFAULT '1' COMMENT '계정 상태 (1: 활성, 0: 탈퇴/비활성)',
  `created_at` datetime NOT NULL COMMENT '생성일',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_user`
--

LOCK TABLES `tb_user` WRITE;
/*!40000 ALTER TABLE `tb_user` DISABLE KEYS */;
INSERT INTO `tb_user` VALUES ('admin','cksehf55','오찬석','찬돌','dhcksehf1@naver.com',0,0,NULL,1,'2025-07-16 12:52:43');
/*!40000 ALTER TABLE `tb_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-21 10:24:41
