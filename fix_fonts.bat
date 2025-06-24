@echo off
cd /d "d:\Document\LoveMatch\app\src\main\res\layout"

REM Replace poppins_bold with sans-serif-medium
powershell -Command "(Get-Content activity_signin.xml) -replace '@font/poppins_bold', 'sans-serif-medium' | Set-Content activity_signin.xml"
powershell -Command "(Get-Content activity_signin.xml) -replace '@font/poppins_regular', 'sans-serif' | Set-Content activity_signin.xml"
powershell -Command "(Get-Content activity_signup.xml) -replace '@font/poppins_bold', 'sans-serif-medium' | Set-Content activity_signup.xml"
powershell -Command "(Get-Content activity_signup.xml) -replace '@font/poppins_regular', 'sans-serif' | Set-Content activity_signup.xml"

echo Font replacements completed!
pause
