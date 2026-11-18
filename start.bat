@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ==========================================
echo    会议室预约系统 - Spring Boot
echo    浏览器访问: http://localhost:8080
echo ==========================================
echo.

echo [1/2] 编译打包中，请稍候...
call mvn -q package -DskipTests
if errorlevel 1 (
    echo.
    echo [错误] 打包失败，请确认已安装 Maven 且网络可用。
    pause
    exit /b 1
)

echo [2/2] 启动服务...
echo 启动完成前请勿关闭本窗口，按 Ctrl+C 可停止服务。
echo.
java -jar target\meeting-room-reservation-1.0.0.jar --server.port=8080
pause