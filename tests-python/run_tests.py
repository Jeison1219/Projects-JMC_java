#!/usr/bin/env python3
"""
Script para ejecutar todos los tests y generar reportes
"""

import subprocess
import sys
import os


def run_command(cmd, description):
    """Ejecuta un comando y maneja errores"""
    print(f"\n{'='*60}")
    print(f"  {description}")
    print(f"{'='*60}\n")
    
    try:
        result = subprocess.run(cmd, shell=True, check=True)
        print(f"\n✅ {description} - EXITOSO\n")
        return True
    except subprocess.CalledProcessError as e:
        print(f"\n❌ {description} - FALLÓ\n")
        return False


def main():
    """Función principal"""
    print("\n" + "="*60)
    print("  SUITE DE TESTS UNITARIOS PARA PROYECTO JAVA")
    print("="*60)

    # Verificar que pytest está instalado
    try:
        import pytest
    except ImportError:
        print("\n❌ pytest no está instalado")
        print("   Instala las dependencias con: pip install -r requirements.txt\n")
        sys.exit(1)

    # Menú de opciones
    print("\nElige una opción:")
    print("1. Ejecutar todos los tests")
    print("2. Ejecutar tests con reporte de cobertura")
    print("3. Ejecutar tests específicos por módulo")
    print("4. Ejecutar tests en modo watch (reexecución automática)")
    print("5. Salir")

    choice = input("\nOpción (1-5): ").strip()

    if choice == "1":
        run_command("pytest -v", "Ejecutando todos los tests")

    elif choice == "2":
        print("\nSelecciona formato de cobertura:")
        print("1. HTML (reporte completo)")
        print("2. Terminal (resumen)")
        print("3. XML (para CI/CD)")
        cov_choice = input("\nOpción (1-3): ").strip()

        if cov_choice == "1":
            run_command(
                "pytest --cov=tests --cov-report=html -v",
                "Generando reporte de cobertura HTML"
            )
            print("📊 Reporte generado en: htmlcov/index.html")

        elif cov_choice == "2":
            run_command(
                "pytest --cov=tests --cov-report=term-missing -v",
                "Generando reporte de cobertura en terminal"
            )

        elif cov_choice == "3":
            run_command(
                "pytest --cov=tests --cov-report=xml -v",
                "Generando reporte de cobertura XML"
            )

    elif choice == "3":
        modules = {
            "1": ("tests/test_auth_registration.py", "Tests de Registro"),
            "2": ("tests/test_auth_login.py", "Tests de Login"),
            "3": ("tests/test_password_recovery.py", "Tests de Recuperación"),
            "4": ("tests/test_profile_management.py", "Tests de Perfil"),
            "5": ("tests/test_dto_validation.py", "Tests de DTO"),
            "6": ("tests/test_security_validation.py", "Tests de Seguridad"),
            "7": ("tests/test_api_responses.py", "Tests de Respuestas"),
            "8": ("tests/test_api_integration.py", "Tests de Integración"),
            "9": ("tests/test_email_notifications.py", "Tests de Email"),
            "10": ("tests/test_authorization_access_control.py", "Tests de Autorización"),
        }

        print("\nMódulos disponibles:")
        for key, (path, name) in modules.items():
            print(f"{key}. {name}")

        mod_choice = input("\nOpción (1-10): ").strip()

        if mod_choice in modules:
            path, name = modules[mod_choice]
            run_command(f"pytest {path} -v", f"Ejecutando {name}")

    elif choice == "4":
        print("\n⏱️  Ejecutando tests en modo watch...")
        print("   Los tests se ejecutarán automáticamente al cambiar archivos")
        print("   Presiona Ctrl+C para salir\n")
        try:
            run_command("pytest-watch tests/ -v", "Tests en modo watch")
        except Exception:
            print("\nℹ️  pytest-watch no está instalado")
            print("   Instálalo con: pip install pytest-watch\n")

    elif choice == "5":
        print("\n👋 Hasta luego!\n")
        sys.exit(0)

    else:
        print("\n❌ Opción inválida\n")
        sys.exit(1)


if __name__ == "__main__":
    main()
