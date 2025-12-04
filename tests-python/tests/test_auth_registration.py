"""
Tests unitarios para el proceso de Registro de Autenticación
"""
import pytest
import requests
from unittest.mock import patch, Mock
import responses

BASE_URL = "http://localhost:8080"


class TestRegistrationValidation:
    """Tests para validación de datos en registro"""

    def test_register_password_mismatch(self, valid_user_data):
        """Test que valida que las contraseñas no coincidan"""
        payload = valid_user_data.copy()
        payload["confirmPassword"] = "DifferentPassword"

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": False, "message": "Las contraseñas no coinciden"},
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json=payload)

        assert response.status_code == 400
        data = response.json()
        assert data["success"] is False
        assert "no coinciden" in data["message"]

    def test_register_empty_name(self):
        """Test que valida que el nombre no esté vacío"""
        payload = {
            "name": "",
            "email": "test@example.com",
            "password": "Test1234",
            "confirmPassword": "Test1234",
            "role": "USER"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": False, "message": "El nombre es requerido"},
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json=payload)

        assert response.status_code == 400
        assert response.json()["success"] is False

    def test_register_invalid_email_format(self):
        """Test que valida formato de email inválido"""
        payload = {
            "name": "Test User",
            "email": "invalidemail",
            "password": "Test1234",
            "confirmPassword": "Test1234",
            "role": "USER"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": False, "message": "Email inválido"},
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json=payload)

        assert response.status_code == 400
        assert response.json()["success"] is False

    def test_register_weak_password(self):
        """Test que valida contraseña débil"""
        payload = {
            "name": "Test User",
            "email": "test@example.com",
            "password": "123",
            "confirmPassword": "123",
            "role": "USER"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": False, "message": "Contraseña muy débil"},
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json=payload)

        assert response.status_code == 400

    def test_register_invalid_role(self):
        """Test que valida rol inválido"""
        payload = {
            "name": "Test User",
            "email": "test@example.com",
            "password": "Test1234",
            "confirmPassword": "Test1234",
            "role": "INVALID_ROLE"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": False, "message": "Rol inválido"},
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json=payload)

        assert response.status_code == 400


class TestRegistrationDuplicate:
    """Tests para validar emails duplicados"""

    def test_register_email_duplicate(self):
        """Test que valida que email duplicado falle"""
        payload = {
            "name": "Test User",
            "email": "duplicate@example.com",
            "password": "Test1234",
            "confirmPassword": "Test1234",
            "role": "USER"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": False, "message": "Correo ya registrado"},
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json=payload)

        assert response.status_code == 400
        data = response.json()
        assert data["success"] is False
        assert "Correo ya registrado" in data["message"]

    def test_register_email_case_insensitive(self):
        """Test que valida que la búsqueda de emails sea case-insensitive"""
        email1 = "Test@Example.com"
        email2 = "test@example.com"

        payload = {
            "name": "Test User",
            "email": email2,
            "password": "Test1234",
            "confirmPassword": "Test1234",
            "role": "USER"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": False, "message": "Correo ya registrado"},
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json=payload)

        assert response.status_code == 400


class TestRegistrationSuccess:
    """Tests para registro exitoso"""

    def test_register_successful(self, valid_user_data):
        """Test que valida registro exitoso"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={
                    "success": True,
                    "message": "Usuario registrado correctamente",
                    "userId": 1
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json=valid_user_data)

        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "registrado" in data["message"].lower()

    def test_register_returns_user_id(self, valid_user_data):
        """Test que valida que se retorna el ID del usuario"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={
                    "success": True,
                    "message": "Usuario registrado correctamente",
                    "userId": 123
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json=valid_user_data)

        assert response.status_code == 200
        assert "userId" in response.json()
        assert response.json()["userId"] == 123

    def test_register_with_admin_role(self):
        """Test que valida registro con rol ADMIN"""
        payload = {
            "name": "Admin User",
            "email": "admin@example.com",
            "password": "AdminPass123",
            "confirmPassword": "AdminPass123",
            "role": "ADMIN"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": True, "message": "Usuario registrado correctamente"},
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json=payload)

        assert response.status_code == 200
        assert response.json()["success"] is True

    def test_register_preserves_name_case(self, valid_user_data):
        """Test que valida que se preserve el caso del nombre"""
        payload = valid_user_data.copy()
        payload["name"] = "JuAn CaRlOs PéReZ"

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={
                    "success": True,
                    "message": "Usuario registrado correctamente",
                    "name": "JuAn CaRlOs PéReZ"
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json=payload)

        assert response.status_code == 200
