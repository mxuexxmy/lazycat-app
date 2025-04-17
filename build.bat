@echo off
if not exist "..\dist\web" mkdir "..\dist\web"
xcopy /E /I /Y ".\front-end\dist" "..\dist\web" 