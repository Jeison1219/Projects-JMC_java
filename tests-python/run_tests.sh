#!/bin/bash

# Script para ejecutar tests en Linux/Mac

echo ""
echo "============================================================"
echo "  SUITE DE TESTS UNITARIOS PARA PROYECTO JAVA"
echo "============================================================"
echo ""

# Verificar que pytest está instalado
if ! python -m pytest --version > /dev/null 2>&1; then
    echo "Error: pytest no está instalado"
    echo "Instala las dependencias con: pip install -r requirements.txt"
    echo ""
    exit 1
fi

echo "Elige una opción:"
echo "1. Ejecutar todos los tests"
echo "2. Ejecutar tests con reporte de cobertura"
echo "3. Ejecutar tests de un módulo específico"
echo "4. Ejecutar tests con output detallado"
echo "5. Salir"
echo ""

read -p "Opción (1-5): " choice

case $choice in
    1)
        python -m pytest tests/ -v
        ;;
    2)
        python -m pytest tests/ --cov=tests --cov-report=html --cov-report=term-missing -v
        echo ""
        echo "📊 Reporte generado en: htmlcov/index.html"
        ;;
    3)
        echo ""
        echo "Módulos disponibles:"
        echo "1. test_auth_registration.py - Tests de Registro"
        echo "2. test_auth_login.py - Tests de Login"
        echo "3. test_password_recovery.py - Tests de Recuperación"
        echo "4. test_profile_management.py - Tests de Perfil"
        echo "5. test_dto_validation.py - Tests de DTO"
        echo "6. test_security_validation.py - Tests de Seguridad"
        echo "7. test_api_responses.py - Tests de Respuestas"
        echo "8. test_api_integration.py - Tests de Integración"
        echo "9. test_email_notifications.py - Tests de Email"
        echo "10. test_authorization_access_control.py - Tests de Autorización"
        echo ""
        read -p "Opción (1-10): " module

        case $module in
            1) python -m pytest tests/test_auth_registration.py -v ;;
            2) python -m pytest tests/test_auth_login.py -v ;;
            3) python -m pytest tests/test_password_recovery.py -v ;;
            4) python -m pytest tests/test_profile_management.py -v ;;
            5) python -m pytest tests/test_dto_validation.py -v ;;
            6) python -m pytest tests/test_security_validation.py -v ;;
            7) python -m pytest tests/test_api_responses.py -v ;;
            8) python -m pytest tests/test_api_integration.py -v ;;
            9) python -m pytest tests/test_email_notifications.py -v ;;
            10) python -m pytest tests/test_authorization_access_control.py -v ;;
            *) echo "Opción inválida" ;;
        esac
        ;;
    4)
        python -m pytest tests/ -vv -s
        ;;
    5)
        echo "¡Hasta luego!"
        ;;
    *)
        echo "Opción inválida"
        exit 1
        ;;
esac

echo ""
