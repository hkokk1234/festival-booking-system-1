@echo off
setlocal

REM ==== SET BASE PATHS ====
set BASE_BACKEND=C:\Users\User\Downloads\festival-management\festival-management
set BASE_FRONTEND=C:\tools\festival-management2\festival-management\src\main\resources\static

REM ==== BACKEND STRUCTURE ====
mkdir "%BASE_BACKEND%\src\main\java\com\example\festival_management\controller"
mkdir "%BASE_BACKEND%\src\main\java\com\example\festival_management\service\impl"
mkdir "%BASE_BACKEND%\src\main\java\com\example\festival_management\service"
mkdir "%BASE_BACKEND%\src\main\java\com\example\festival_management\repository"
mkdir "%BASE_BACKEND%\src\main\java\com\example\festival_management\entity"
mkdir "%BASE_BACKEND%\src\main\java\com\example\festival_management\config"
mkdir "%BASE_BACKEND%\src\main\java\com\example\festival_management\security"
mkdir "%BASE_BACKEND%\src\main\java\com\example\festival_management\exception"
mkdir "%BASE_BACKEND%\src\main\resources"
mkdir "%BASE_BACKEND%\src\test\java\com\example\festival_management"

REM ==== FRONTEND STRUCTURE ====
mkdir "%BASE_FRONTEND%\src\pages"
mkdir "%BASE_FRONTEND%\src\components"
mkdir "%BASE_FRONTEND%\src\services"

REM ==== BACKEND FILES ====

REM pom.xml
echo <!-- Dummy pom.xml --> > "%BASE_BACKEND%\pom.xml"

REM application.yml
echo server:^ > "%BASE_BACKEND%\src\main\resources\application.yml"
echo   port: 8080 >> "%BASE_BACKEND%\src\main\resources\application.yml"

REM FestivalManagementApplication.java
(
echo package com.example.festival_management;
echo.
echo import org.springframework.boot.SpringApplication;
echo import org.springframework.boot.autoconfigure.SpringBootApplication;
echo.
echo @SpringBootApplication
echo public class FestivalManagementApplication {
echo     public static void main(String[] args) {
echo         SpringApplication.run(FestivalManagementApplication.class, args);
echo     }
echo }
) > "%BASE_BACKEND%\src\main\java\com\example\festival_management\FestivalManagementApplication.java"

REM User.java (entity)
(
echo package com.example.festival_management.entity;
echo.
echo import jakarta.persistence.*;
echo import java.util.*;
echo.
echo @Entity
echo public class User {
echo     @Id
echo     @GeneratedValue(strategy = GenerationType.IDENTITY)
echo     private Long id;
echo     private String username;
echo     private String email;
echo     private String password;
echo.
echo     // Getters & Setters
echo }
) > "%BASE_BACKEND%\src\main\java\com\example\festival_management\entity\User.java"

REM Festival.java (entity)
(
echo package com.example.festival_management.entity;
echo.
echo import jakarta.persistence.*;
echo import java.time.LocalDate;
echo import java.util.*;
echo.
echo @Entity
echo public class Festival {
echo     @Id
echo     @GeneratedValue(strategy = GenerationType.IDENTITY)
echo     private Long id;
echo     private String name;
echo     private String description;
echo     private LocalDate startDate;
echo     private LocalDate endDate;
echo     private String venue;
echo.
echo     // Getters & Setters
echo }
) > "%BASE_BACKEND%\src\main\java\com\example\festival_management\entity\Festival.java"

REM Performance.java (entity)
(
echo package com.example.festival_management.entity;
echo.
echo import jakarta.persistence.*;
echo import java.util.*;
echo.
echo @Entity
echo public class Performance {
echo     @Id
echo     @GeneratedValue(strategy = GenerationType.IDENTITY)
echo     private Long id;
echo     private String name;
echo     private String genre;
echo     private String description;
echo.
echo     @ManyToOne
echo     private Festival festival;
echo.
echo     @ManyToOne
echo     private User mainArtist;
echo.
echo     // Getters & Setters
echo }
) > "%BASE_BACKEND%\src\main\java\com\example\festival_management\entity\Performance.java"

REM UserRepository.java
(
echo package com.example.festival_management.repository;
echo.
echo import com.example.festival_management.entity.User;
echo import org.springframework.data.jpa.repository.JpaRepository;
echo.
echo public interface UserRepository extends JpaRepository<User, Long> {
echo     Optional<User> findByUsername(String username);
echo }
) > "%BASE_BACKEND%\src\main\java\com\example\festival_management\repository\UserRepository.java"

REM FestivalController.java
(
echo package com.example.festival_management.controller;
echo.
echo import org.springframework.web.bind.annotation.*;
echo.
echo @RestController
echo @RequestMapping(\"/api/festivals\")
echo public class FestivalController {
echo.
echo }
) > "%BASE_BACKEND%\src\main\java\com\example\festival_management\controller\FestivalController.java"

REM FestivalService.java
(
echo package com.example.festival_management.service;
echo.
echo public interface FestivalService {
echo.
echo }
) > "%BASE_BACKEND%\src\main\java\com\example\festival_management\service\FestivalService.java"

REM FestivalServiceImpl.java
(
echo package com.example.festival_management.service.impl;
echo.
echo import org.springframework.stereotype.Service;
echo.
echo @Service
echo public class FestivalServiceImpl implements com.example.festival_management.service.FestivalService {
echo.
echo }
) > "%BASE_BACKEND%\src\main\java\com\example\festival_management\service\impl\FestivalServiceImpl.java"

REM GlobalExceptionHandler.java
(
echo package com.example.festival_management.exception;
echo.
echo import org.springframework.web.bind.annotation.ControllerAdvice;
echo.
echo @ControllerAdvice
echo public class GlobalExceptionHandler {
echo.
echo }
) > "%BASE_BACKEND%\src\main\java\com\example\festival_management\exception\GlobalExceptionHandler.java"

REM ==== FRONTEND FILES ====

REM package.json
echo { } > "%BASE_FRONTEND%\package.json"

REM index.js
echo import React from 'react'; > "%BASE_FRONTEND%\src\index.js"
echo import ReactDOM from 'react-dom/client'; >> "%BASE_FRONTEND%\src\index.js"
echo import App from './App'; >> "%BASE_FRONTEND%\src\index.js"
echo const root = ReactDOM.createRoot(document.getElementById('root')); >> "%BASE_FRONTEND%\src\index.js"
echo root.render(^<App /^>); >> "%BASE_FRONTEND%\src\index.js"

REM App.js
echo import React from 'react'; > "%BASE_FRONTEND%\src\App.js"
echo function App() { return ^<div^>Festival Management App^</div^>; } export default App; >> "%BASE_FRONTEND%\src\App.js"

REM api.js
(
echo import axios from 'axios';
echo.
echo const api = axios.create({
echo     baseURL: 'http://localhost:8080/api'
echo });
echo.
echo export default api;
) > "%BASE_FRONTEND%\src\services\api.js"

REM FestivalsPage.js
echo import React from 'react'; > "%BASE_FRONTEND%\src\pages\FestivalsPage.js"
echo function FestivalsPage() { return ^<div^>Festivals^</div^>; } export default FestivalsPage; >> "%BASE_FRONTEND%\src\pages\FestivalsPage.js"

REM LoginPage.js
echo import React from 'react'; > "%BASE_FRONTEND%\src\pages\LoginPage.js"
echo function LoginPage() { return ^<div^>Login^</div^>; } export default LoginPage; >> "%BASE_FRONTEND%\src\pages\LoginPage.js"

REM PerformanceForm.js
echo import React from 'react'; > "%BASE_FRONTEND%\src\components\PerformanceForm.js"
echo function PerformanceForm() { return ^<div^>Performance Form^</div^>; } export default PerformanceForm; >> "%BASE_FRONTEND%\src\components\PerformanceForm.js"

REM FestivalList.js
echo import React from 'react'; > "%BASE_FRONTEND%\src\components\FestivalList.js"
echo function FestivalList() { return ^<div^>Festival List^</div^>; } export default FestivalList; >> "%BASE_FRONTEND%\src\components\FestivalList.js"

echo ✅ Φάκελοι και αρχεία backend/frontend δημιουργήθηκαν με επιτυχία!

endlocal
