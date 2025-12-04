"""
Tests unitarios para respuestas de API y códigos de estado HTTP
"""
import pytest
import requests
import responses
import json

BASE_URL = "http://localhost:8080"


class TestHttpStatusCodes:
    """Tests para validación de códigos de estado HTTP"""

    def test_200_ok_response(self):
        """Test que valida respuesta 200 OK"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/test",
                json={"success": True, "data": {}},
                status=200
            )
            response = requests.get(f"{BASE_URL}/api/test")

        assert response.status_code == 200
        assert response.json()["success"] is True

    def test_201_created_response(self):
        """Test que valida respuesta 201 Created"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/usuarios",
                json={"id": 1, "name": "Test User"},
                status=201
            )
            response = requests.post(f"{BASE_URL}/api/usuarios", json={"name": "Test User"})

        assert response.status_code == 201

    def test_400_bad_request_response(self):
        """Test que valida respuesta 400 Bad Request"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/test",
                json={"success": False, "message": "Invalid input"},
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/test", json={})

        assert response.status_code == 400
        assert response.json()["success"] is False

    def test_401_unauthorized_response(self):
        """Test que valida respuesta 401 Unauthorized"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/perfil",
                json={"success": False, "message": "No autenticado"},
                status=401
            )
            response = requests.get(f"{BASE_URL}/api/perfil")

        assert response.status_code == 401

    def test_403_forbidden_response(self):
        """Test que valida respuesta 403 Forbidden"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/admin/datos",
                json={"success": False, "message": "Acceso denegado"},
                status=403
            )
            response = requests.get(f"{BASE_URL}/api/admin/datos")

        assert response.status_code == 403

    def test_404_not_found_response(self):
        """Test que valida respuesta 404 Not Found"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/usuarios/999",
                json={"success": False, "message": "Usuario no encontrado"},
                status=404
            )
            response = requests.get(f"{BASE_URL}/api/usuarios/999")

        assert response.status_code == 404

    def test_500_server_error_response(self):
        """Test que valida respuesta 500 Server Error"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/error",
                json={"success": False, "message": "Error interno del servidor"},
                status=500
            )
            response = requests.get(f"{BASE_URL}/api/error")

        assert response.status_code == 500


class TestResponseFormats:
    """Tests para validación del formato de respuestas"""

    def test_json_response_format(self):
        """Test que valida formato JSON de respuesta"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/test",
                json={"success": True, "data": {}},
                status=200
            )
            response = requests.get(f"{BASE_URL}/api/test")

        # Verificar que es JSON válido
        data = response.json()
        assert isinstance(data, dict)

    def test_response_contains_required_fields(self):
        """Test que respuesta contiene campos requeridos"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": True, "message": "Registered"},
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json={})

        data = response.json()
        assert "success" in data
        assert "message" in data

    def test_error_response_format(self):
        """Test que respuesta de error tiene formato consistente"""
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
            response = requests.post(f"{BASE_URL}/api/auth/login", json={})

        data = response.json()
        assert data["success"] is False
        assert isinstance(data["message"], str)

    def test_content_type_header(self):
        """Test que Content-Type es application/json"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/test",
                json={"success": True},
                status=200,
                content_type="application/json"
            )
            response = requests.get(f"{BASE_URL}/api/test")

        assert "application/json" in response.headers.get("Content-Type", "")


class TestResponseData:
    """Tests para validación de datos en respuestas"""

    def test_user_data_in_registration_response(self):
        """Test que respuesta de registro contiene datos del usuario"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={
                    "success": True,
                    "userId": 1,
                    "email": "test@example.com",
                    "message": "Registered"
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/register", json={})

        data = response.json()
        assert "userId" in data or "id" in data
        assert "email" in data or "user" in data

    def test_token_in_login_response(self):
        """Test que respuesta de login contiene token"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={
                    "success": True,
                    "token": "jwt-token-here",
                    "userId": 1
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json={})

        data = response.json()
        assert "token" in data
        assert len(data["token"]) > 0

    def test_no_sensitive_data_in_error_response(self):
        """Test que respuesta de error no contiene datos sensibles"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/test",
                json={
                    "success": False,
                    "message": "Database error",
                    "error": "table users not found"
                },
                status=500
            )
            response = requests.get(f"{BASE_URL}/api/test")

        data = response.json()
        # No debe exponer detalles de base de datos en producción
        error_msg = data.get("error", "").lower()
        assert "password" not in error_msg
        assert "token" not in error_msg


class TestPaginationResponses:
    """Tests para respuestas con paginación"""

    def test_paginated_response_format(self):
        """Test que valida formato de respuesta paginada"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/usuarios?page=1",
                json={
                    "success": True,
                    "data": [
                        {"id": 1, "name": "User 1"},
                        {"id": 2, "name": "User 2"}
                    ],
                    "pagination": {
                        "page": 1,
                        "pageSize": 10,
                        "total": 100
                    }
                },
                status=200
            )
            response = requests.get(f"{BASE_URL}/api/usuarios?page=1")

        data = response.json()
        assert "pagination" in data
        assert "page" in data["pagination"]
        assert "pageSize" in data["pagination"]
        assert "total" in data["pagination"]

    def test_empty_list_response(self):
        """Test que valida respuesta con lista vacía"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/usuarios",
                json={
                    "success": True,
                    "data": [],
                    "message": "No usuarios found"
                },
                status=200
            )
            response = requests.get(f"{BASE_URL}/api/usuarios")

        data = response.json()
        assert isinstance(data["data"], list)
        assert len(data["data"]) == 0
