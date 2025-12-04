@echo off
REM Script para ejecutar tests en Windows

echo.
echo ============================================================
echo   SUITE DE TESTS UNITARIOS PARA PROYECTO JAVA
echo ============================================================
echo.

REM Verificar que pytest está instalado
python -m pytest --version >nul 2>&1
if errorlevel 1 (
    echo Error: pytest no está instalado
    echo Instala las dependencias con: pip install -r requirements.txt
    echo.
    pause
    exit /b 1
)

echo Elige una opción:
echo 1. Ejecutar todos los tests
echo 2. Ejecutar tests con reporte de cobertura
echo 3. Ejecutar tests de un módulo específico
echo 4. Salir
echo.

set /p choice="Opción (1-4): "

if "%choice%"=="1" (
    python -m pytest tests/ -v
) else if "%choice%"=="2" (
    python -m pytest tests/ --cov=tests --cov-report=html --cov-report=term-missing -v
    echo.
    echo Reporte generado en: htmlcov\index.html
) else if "%choice%"=="3" (
    echo.
    echo Módulos disponibles:
    echo 1. test_auth_registration.py - Tests de Registro
    echo 2. test_auth_login.py - Tests de Login
    echo 3. test_password_recovery.py - Tests de Recuperación
    echo 4. test_profile_management.py - Tests de Perfil
    echo 5. test_dto_validation.py - Tests de DTO
    echo 6. test_security_validation.py - Tests de Seguridad
    echo 7. test_api_responses.py - Tests de Respuestas
    echo 8. test_api_integration.py - Tests de Integración
    echo 9. test_email_notifications.py - Tests de Email
    echo 10. test_authorization_access_control.py - Tests de Autorización
    echo.
    set /p module="Opción (1-10): "
    
    if "%module%"=="1" (
        python -m pytest tests/test_auth_registration.py -v
    ) else if "%module%"=="2" (
        python -m pytest tests/test_auth_login.py -v
    ) else if "%module%"=="3" (
        python -m pytest tests/test_password_recovery.py -v
    ) else if "%module%"=="4" (
        python -m pytest tests/test_profile_management.py -v
    ) else if "%module%"=="5" (
        python -m pytest tests/test_dto_validation.py -v
    ) else if "%module%"=="6" (
        python -m pytest tests/test_security_validation.py -v
    ) else if "%module%"=="7" (
        python -m pytest tests/test_api_responses.py -v
    ) else if "%module%"=="8" (
        python -m pytest tests/test_api_integration.py -v
    ) else if "%module%"=="9" (
        python -m pytest tests/test_email_notifications.py -v
    ) else if "%module%"=="10" (
        python -m pytest tests/test_authorization_access_control.py -v
    ) else (
        echo Opción inválida
    )
) else if "%choice%"=="4" (
    echo Hasta luego!
) else (
    echo Opción inválida
)

echo.
pause
