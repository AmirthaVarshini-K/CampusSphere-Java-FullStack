@echo off
setlocal

set "SPRING_PROFILES_ACTIVE=local"
call mvnw.cmd spring-boot:run

endlocal
