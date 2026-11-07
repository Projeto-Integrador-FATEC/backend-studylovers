@echo off
setlocal

set "PROJECT_ROOT=%~dp0"

if not exist "%PROJECT_ROOT%\.env" (
    echo Arquivo .env nao encontrado na raiz do projeto.
    exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in (`type "%PROJECT_ROOT%\.env"`) do (
    if "%%A"=="GOOGLE_API_KEY" set "GOOGLE_API_KEY=%%B"
)

if not defined GOOGLE_API_KEY (
    echo GOOGLE_API_KEY nao definida no .env.
    exit /b 1
)

if "%~1"=="" (
    echo Uso: env.bat comando [argumentos]
    echo Exemplo: env.bat java -jar app.jar
    exit /b 2
)

call %*
set "APP_EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %APP_EXIT_CODE%
