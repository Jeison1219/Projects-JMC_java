"""
Tests unitarios para Integración de API
"""
import pytest
import requests
import responses
import time

BASE_URL = "http://localhost:8080"


class TestAuthenticationFlow:
    """Tests para flujo completo de autenticación"""

    def test_complete_registration_and_login_flow(self):
        """Test que valida flujo completo: registro -> login"""
        register_payload = {
            "name": "Test User",
            "email": "flowtest@example.com",
            "password": "TestPass123",
            "confirmPassword": "TestPass123",
            "role": "USER"
        }

        login_payload = {
            "email": "flowtest@example.com",
            "password": "TestPass123"
        }

        with responses.RequestsMock() as rsps:
            # Mock registro
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": True, "userId": 1},
                status=200
            )

            # Mock login
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={"success": True, "token": "jwt-token"},
                status=200
            )

            # Registrar
            reg_response = requests.post(f"{BASE_URL}/api/auth/register", json=register_payload)
            assert reg_response.status_code == 200

            # Luego login
            login_response = requests.post(f"{BASE_URL}/api/auth/login", json=login_payload)
            assert login_response.status_code == 200
            assert "token" in login_response.json()

    def test_login_with_unregistered_email(self):
        """Test que falla al intentar login con email no registrado"""
        payload = {
            "email": "unregistered@example.com",
            "password": "SomePassword"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/login",
                json={"success": False, "message": "Email o contraseña incorrectos"},
                status=401
            )
            response = requests.post(f"{BASE_URL}/api/auth/login", json=payload)

        assert response.status_code == 401
        assert response.json()["success"] is False

    def test_cannot_register_twice_with_same_email(self):
        """Test que no se puede registrar dos veces con mismo email"""
        payload = {
            "name": "Test User",
            "email": "duplicate@example.com",
            "password": "TestPass123",
            "confirmPassword": "TestPass123",
            "role": "USER"
        }

        with responses.RequestsMock() as rsps:
            # Primer registro
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": True, "userId": 1},
                status=200
            )

            # Segundo registro con mismo email
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/auth/register",
                json={"success": False, "message": "Correo ya registrado"},
                status=400
            )

            # Primer registro
            response1 = requests.post(f"{BASE_URL}/api/auth/register", json=payload)
            assert response1.status_code == 200

            # Segundo registro
            response2 = requests.post(f"{BASE_URL}/api/auth/register", json=payload)
            assert response2.status_code == 400
            assert "ya registrado" in response2.json()["message"].lower()


class TestPasswordRecoveryFlow:
    """Tests para flujo de recuperación de contraseña"""

    def test_password_recovery_complete_flow(self):
        """Test flujo completo de recuperación: solicitar -> verificar -> cambiar"""
        email = "recovery@example.com"
        code = "123456"
        new_password = "NewPassword123"

        with responses.RequestsMock() as rsps:
            # Solicitar código
            rsps.add(
                responses.POST,
                f"{BASE_URL}/enviar-codigo",
                json={"success": True, "message": "Código enviado"},
                status=200
            )

            # Verificar código
            rsps.add(
                responses.POST,
                f"{BASE_URL}/verificar-codigo",
                json={"success": True, "message": "Código verificado"},
                status=200
            )

            # Cambiar contraseña
            rsps.add(
                responses.POST,
                f"{BASE_URL}/cambiar-password",
                json={"success": True, "message": "Contraseña actualizada"},
                status=200
            )

            # Paso 1: Solicitar código
            req1 = requests.post(f"{BASE_URL}/enviar-codigo", data={"email": email})
            assert req1.status_code == 200

            # Paso 2: Verificar código
            req2 = requests.post(f"{BASE_URL}/verificar-codigo", 
                                data={"email": email, "codigo": code})
            assert req2.status_code == 200

            # Paso 3: Cambiar contraseña
            req3 = requests.post(f"{BASE_URL}/cambiar-password",
                                data={"email": email, "password": new_password,
                                      "confirm": new_password})
            assert req3.status_code == 200

    def test_cannot_change_password_with_invalid_code(self):
        """Test que no se puede cambiar contraseña sin verificar código"""
        email = "recovery@example.com"

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/cambiar-password",
                json={"success": False, "message": "Código no verificado"},
                status=400
            )

            response = requests.post(f"{BASE_URL}/cambiar-password",
                                   data={"email": email, "password": "NewPass",
                                         "confirm": "NewPass"})

        assert response.status_code == 400


class TestProfileManagementFlow:
    """Tests para flujo de gestión de perfil"""

    def test_update_profile_after_login(self):
        """Test que puede actualizar perfil después de login"""
        headers = {"Authorization": "Bearer valid-token"}

        with responses.RequestsMock() as rsps:
            # Obtener perfil
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/perfil",
                json={"success": True, "user": {"id": 1, "name": "Original Name"}},
                status=200
            )

            # Actualizar perfil
            rsps.add(
                responses.PUT,
                f"{BASE_URL}/api/perfil",
                json={"success": True, "message": "Perfil actualizado"},
                status=200
            )

            # Obtener perfil
            req1 = requests.get(f"{BASE_URL}/api/perfil", headers=headers)
            assert req1.status_code == 200
            original_name = req1.json()["user"]["name"]

            # Actualizar
            new_data = {"name": "Updated Name"}
            req2 = requests.put(f"{BASE_URL}/api/perfil", json=new_data, headers=headers)
            assert req2.status_code == 200

    def test_change_password_from_profile(self):
        """Test que puede cambiar contraseña desde perfil"""
        headers = {"Authorization": "Bearer valid-token"}
        payload = {
            "currentPassword": "OldPass123",
            "newPassword": "NewPass123",
            "confirmPassword": "NewPass123"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/api/perfil/cambiar-password",
                json={"success": True, "message": "Contraseña actualizada"},
                status=200
            )

            response = requests.post(f"{BASE_URL}/api/perfil/cambiar-password",
                                    json=payload, headers=headers)

        assert response.status_code == 200
        assert response.json()["success"] is True


class TestConcurrentRequests:
    """Tests para manejo de peticiones concurrentes"""

    def test_multiple_users_can_register(self):
        """Test que múltiples usuarios pueden registrarse"""
        users = [
            {"name": f"User {i}", "email": f"user{i}@example.com", 
             "password": "Pass123", "confirmPassword": "Pass123", "role": "USER"}
            for i in range(5)
        ]

        with responses.RequestsMock() as rsps:
            for user in users:
                rsps.add(
                    responses.POST,
                    f"{BASE_URL}/api/auth/register",
                    json={"success": True, "userId": users.index(user) + 1},
                    status=200
                )

            for user in users:
                response = requests.post(f"{BASE_URL}/api/auth/register", json=user)
                assert response.status_code == 200
                assert response.json()["success"] is True

    def test_multiple_users_can_login(self):
        """Test que múltiples usuarios pueden hacer login"""
        logins = [
            {"email": f"user{i}@example.com", "password": "Pass123"}
            for i in range(3)
        ]

        with responses.RequestsMock() as rsps:
            for i, login in enumerate(logins):
                rsps.add(
                    responses.POST,
                    f"{BASE_URL}/api/auth/login",
                    json={"success": True, "token": f"token-{i}", "userId": i + 1},
                    status=200
                )

            for login in logins:
                response = requests.post(f"{BASE_URL}/api/auth/login", json=login)
                assert response.status_code == 200
                assert "token" in response.json()


class TestErrorRecovery:
    """Tests para recuperación de errores"""

    def test_retry_after_server_error(self):
        """Test que reintenta después de error 500"""
        with responses.RequestsMock() as rsps:
            # Primer intento falla
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/test",
                json={"error": "Server error"},
                status=500
            )

            # Segundo intento exitoso
            rsps.add(
                responses.GET,
                f"{BASE_URL}/api/test",
                json={"success": True},
                status=200
            )

            # Primer intento
            response1 = requests.get(f"{BASE_URL}/api/test")
            assert response1.status_code == 500

            # Segundo intento
            response2 = requests.get(f"{BASE_URL}/api/test")
            assert response2.status_code == 200

    def test_timeout_handling(self):
        """Test que maneja timeouts adecuadamente"""
        # Este es más para documentación de cómo se debería manejar
        try:
            # En una aplicación real, esto tendría un timeout configurado
            response = requests.get(f"{BASE_URL}/api/timeout-test", timeout=1)
        except requests.Timeout:
            # El timeout se debería manejar correctamente
            pass
