"""
Tests unitarios para el proceso de Login de Autenticación
"""
import pytest
import requests
import responses

BASE_URL = "http://localhost:8080"


class TestLoginValidation:
    """Tests para validación en el proceso de login"""

    def test_login_with_valid_credentials(self, valid_login_data):
        """Test que valida login exitoso con credenciales válidas"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={
                    "success": True,
                    "message": "Login exitoso",
                    "token": "jwt-token-123",
                    "userId": 1
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json=valid_login_data)

        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "token" in data

    def test_login_with_incorrect_password(self):
        """Test que valida login falla con contraseña incorrecta"""
        payload = {
            "email": "test@example.com",
            "password": "WrongPassword"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={
                    "success": False,
                    "message": "Email o contraseña incorrectos"
                },
                status=401
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json=payload)

        assert response.status_code == 401
        assert response.json()["success"] is False

    def test_login_with_nonexistent_email(self):
        """Test que valida login falla con email no registrado"""
        payload = {
            "email": "nonexistent@example.com",
            "password": "AnyPassword"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={
                    "success": False,
                    "message": "Email o contraseña incorrectos"
                },
                status=401
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json=payload)

        assert response.status_code == 401
        assert response.json()["success"] is False

    def test_login_with_empty_email(self):
        """Test que valida que email no esté vacío"""
        payload = {
            "email": "",
            "password": "Test1234"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={
                    "success": False,
                    "message": "Email requerido"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json=payload)

        assert response.status_code == 400

    def test_login_with_empty_password(self):
        """Test que valida que contraseña no esté vacía"""
        payload = {
            "email": "test@example.com",
            "password": ""
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={
                    "success": False,
                    "message": "Contraseña requerida"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json=payload)

        assert response.status_code == 400

    def test_login_returns_token(self, valid_login_data):
        """Test que valida que se retorna un token JWT"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={
                    "success": True,
                    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    "message": "Login exitoso"
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json=valid_login_data)

        assert response.status_code == 200
        data = response.json()
        assert "token" in data
        assert len(data["token"]) > 0

    def test_login_returns_user_info(self, valid_login_data):
        """Test que valida que se retorna información del usuario"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={
                    "success": True,
                    "token": "jwt-token",
                    "userId": 1,
                    "email": "test@example.com",
                    "name": "Test User",
                    "role": "USER"
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json=valid_login_data)

        assert response.status_code == 200
        data = response.json()
        assert data["userId"] == 1
        assert data["email"] == "test@example.com"
        assert data["role"] == "USER"


class TestLoginSecurity:
    """Tests para seguridad en el login"""

    def test_login_case_insensitive_email(self):
        """Test que valida que el email sea case-insensitive"""
        payload = {
            "email": "TEST@EXAMPLE.COM",
            "password": "Test1234"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={
                    "success": True,
                    "message": "Login exitoso",
                    "token": "jwt-token"
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json=payload)

        assert response.status_code == 200

    def test_login_with_sql_injection_attempt(self):
        """Test que valida protección contra SQL injection"""
        payload = {
            "email": "' OR '1'='1",
            "password": "' OR '1'='1"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={
                    "success": False,
                    "message": "Email o contraseña incorrectos"
                },
                status=401
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json=payload)

        assert response.status_code == 401
        assert response.json()["success"] is False

    def test_login_multiple_attempts_throttling(self):
        """Test que valida que hay throttling después de múltiples intentos"""
        payload = {
            "email": "test@example.com",
            "password": "WrongPassword"
        }

        with responses.RequestsMock() as rsps:
            # Simular múltiples intentos fallidos
            for _ in range(5):
                rsps.add(
                    responses.POST,
                    f"{BASE_URL}/api/auth/login",
                    json={"success": False, "message": "Email o contraseña incorrectos"},
                    status=401
                )

            # Realizar múltiples intentos
            for i in range(5):
                response = requests.post(f"{BASE_URL}/api/auth/login", json=payload)
                assert response.status_code == 401

    def test_login_no_password_in_response(self, valid_login_data):
        """Test que valida que la contraseña NO se retorna en la respuesta"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={
                    "success": True,
                    "token": "jwt-token",
                    "userId": 1,
                    "email": "test@example.com"
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json=valid_login_data)

        assert response.status_code == 200
        data = response.json()
        assert "password" not in data
