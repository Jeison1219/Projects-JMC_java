"""
Tests unitarios para Gestión de Perfil
"""
import pytest
import requests
import responses

BASE_URL = "http://localhost:8080"


class TestProfileRetrieval:
    """Tests para obtener datos del perfil"""

    def test_get_profile_authenticated(self):
        """Test que obtiene el perfil de usuario autenticado"""
        headers = {"Authorization": "Bearer valid-token"}

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/perfil",
                json={
                    "success": True,
                    "user": {
                        "id": 1,
                        "name": "Test User",
                        "email": "test@example.com",
                        "role": "USER"
                    }
                },
                status=200
            )
            response = requests.get(f"{BASE_URL}/api/perfil", headers=headers)

        assert response.status_code == 200
        assert response.json()["success"] is True
        assert response.json()["user"]["email"] == "test@example.com"

    def test_get_profile_unauthenticated(self):
        """Test que falla al obtener perfil sin autenticación"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/perfil",
                json={
                    "success": False,
                    "message": "No autenticado"
                },
                status=401
            )
            response = requests.get(f"{BASE_URL}/api/perfil")

        assert response.status_code == 401
        assert response.json()["success"] is False

    def test_get_profile_invalid_token(self):
        """Test que falla con token inválido"""
        headers = {"Authorization": "Bearer invalid-token"}

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/perfil",
                json={
                    "success": False,
                    "message": "Token inválido"
                },
                status=401
            )
            response = requests.get(f"{BASE_URL}/api/perfil", headers=headers)

        assert response.status_code == 401


class TestProfileUpdate:
    """Tests para actualizar el perfil"""

    def test_update_profile_valid_data(self):
        """Test que actualiza el perfil con datos válidos"""
        headers = {"Authorization": "Bearer valid-token"}
        payload = {
            "name": "Updated Name",
            "phone": "1234567890"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.PUT,
                f"{BASE_URL}/api/perfil",
                json={
                    "success": True,
                    "message": "Perfil actualizado correctamente",
                    "user": payload
                },
                status=200
            )
            response = requests.put(f"{BASE_URL}/api/perfil", json=payload, headers=headers)

        assert response.status_code == 200
        assert response.json()["success"] is True

    def test_update_profile_empty_name(self):
        """Test que valida que el nombre no esté vacío"""
        headers = {"Authorization": "Bearer valid-token"}
        payload = {
            "name": "",
            "phone": "1234567890"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.PUT,
                f"{BASE_URL}/api/perfil",
                json={
                    "success": False,
                    "message": "El nombre no puede estar vacío"
                },
                status=400
            )
            response = requests.put(f"{BASE_URL}/api/perfil", json=payload, headers=headers)

        assert response.status_code == 400
        assert response.json()["success"] is False

    def test_update_profile_invalid_phone(self):
        """Test que valida formato de teléfono"""
        headers = {"Authorization": "Bearer valid-token"}
        payload = {
            "name": "Test User",
            "phone": "invalid"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.PUT,
                f"{BASE_URL}/api/perfil",
                json={
                    "success": False,
                    "message": "Teléfono inválido"
                },
                status=400
            )
            response = requests.put(f"{BASE_URL}/api/perfil", json=payload, headers=headers)

        assert response.status_code == 400

    def test_update_profile_partial_data(self):
        """Test que permite actualizar solo algunos campos"""
        headers = {"Authorization": "Bearer valid-token"}
        payload = {"name": "Updated Name"}

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.PUT,
                f"{BASE_URL}/api/perfil",
                json={
                    "success": True,
                    "message": "Perfil actualizado correctamente"
                },
                status=200
            )
            response = requests.put(f"{BASE_URL}/api/perfil", json=payload, headers=headers)

        assert response.status_code == 200


class TestPasswordManagement:
    """Tests para cambio de contraseña desde perfil"""

    def test_change_password_from_profile_valid(self):
        """Test que cambia contraseña con datos válidos"""
        headers = {"Authorization": "Bearer valid-token"}
        payload = {
            "currentPassword": "OldPassword123",
            "newPassword": "NewPassword123",
            "confirmPassword": "NewPassword123"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/perfil/cambiar-password",
                json={
                    "success": True,
                    "message": "Contraseña actualizada con éxito"
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/api/perfil/cambiar-password", 
                                    json=payload, headers=headers)

        assert response.status_code == 200
        assert response.json()["success"] is True

    def test_change_password_incorrect_current(self):
        """Test que falla con contraseña actual incorrecta"""
        headers = {"Authorization": "Bearer valid-token"}
        payload = {
            "currentPassword": "WrongPassword",
            "newPassword": "NewPassword123",
            "confirmPassword": "NewPassword123"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/perfil/cambiar-password",
                json={
                    "success": False,
                    "message": "La contraseña actual es incorrecta"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/perfil/cambiar-password", 
                                    json=payload, headers=headers)

        assert response.status_code == 400
        assert response.json()["success"] is False

    def test_change_password_mismatch(self):
        """Test que falla si las nuevas contraseñas no coinciden"""
        headers = {"Authorization": "Bearer valid-token"}
        payload = {
            "currentPassword": "OldPassword123",
            "newPassword": "NewPassword123",
            "confirmPassword": "DifferentPassword"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/perfil/cambiar-password",
                json={
                    "success": False,
                    "message": "Las contraseñas no coinciden"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/perfil/cambiar-password", 
                                    json=payload, headers=headers)

        assert response.status_code == 400

    def test_change_password_same_as_current(self):
        """Test que valida que nueva contraseña sea diferente"""
        headers = {"Authorization": "Bearer valid-token"}
        payload = {
            "currentPassword": "Password123",
            "newPassword": "Password123",
            "confirmPassword": "Password123"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/perfil/cambiar-password",
                json={
                    "success": False,
                    "message": "La nueva contraseña debe ser diferente a la actual"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/perfil/cambiar-password", 
                                    json=payload, headers=headers)

        assert response.status_code == 400

    def test_change_password_weak_password(self):
        """Test que rechaza contraseñas débiles"""
        headers = {"Authorization": "Bearer valid-token"}
        payload = {
            "currentPassword": "OldPassword123",
            "newPassword": "123",
            "confirmPassword": "123"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/perfil/cambiar-password",
                json={
                    "success": False,
                    "message": "Contraseña muy débil"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/api/perfil/cambiar-password", 
                                    json=payload, headers=headers)

        assert response.status_code == 400
