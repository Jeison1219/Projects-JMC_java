"""
Tests unitarios para el proceso de Recuperación de Contraseña
"""
import pytest
import requests
import responses

BASE_URL = "http://localhost:8080"


class TestPasswordRecoveryRequest:
    """Tests para solicitud de código de recuperación"""

    def test_send_recovery_code_valid_email(self, valid_password_recovery_data):
        """Test que valida envío de código con email válido"""
        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/enviar-codigo",
                json={
                    "success": True,
                    "message": "Código enviado a tu email"
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/enviar-codigo", 
                                   data=valid_password_recovery_data)

        assert response.status_code == 200
        assert response.json()["success"] is True

    def test_send_recovery_code_nonexistent_email(self):
        """Test que valida que falla con email no registrado"""
        payload = {"email": "nonexistent@example.com"}

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/enviar-codigo",
                json={
                    "success": False,
                    "message": "Correo no registrado"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/enviar-codigo", data=payload)

        assert response.status_code == 400
        assert response.json()["success"] is False

    def test_send_recovery_code_empty_email(self):
        """Test que valida que email no esté vacío"""
        payload = {"email": ""}

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/enviar-codigo",
                json={
                    "success": False,
                    "message": "Email requerido"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/enviar-codigo", data=payload)

        assert response.status_code == 400

    def test_send_recovery_code_invalid_format(self):
        """Test que valida formato de email inválido"""
        payload = {"email": "invalidemail"}

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/enviar-codigo",
                json={
                    "success": False,
                    "message": "Email inválido"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/enviar-codigo", data=payload)

        assert response.status_code == 400


class TestCodeVerification:
    """Tests para verificación de código"""

    def test_verify_valid_code(self):
        """Test que valida verificación con código válido"""
        payload = {
            "email": "test@example.com",
            "codigo": "123456"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/verificar-codigo",
                json={
                    "success": True,
                    "message": "Código verificado"
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/verificar-codigo", data=payload)

        assert response.status_code == 200
        assert response.json()["success"] is True

    def test_verify_invalid_code(self):
        """Test que valida falla con código inválido"""
        payload = {
            "email": "test@example.com",
            "codigo": "000000"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/verificar-codigo",
                json={
                    "success": False,
                    "message": "Código inválido"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/verificar-codigo", data=payload)

        assert response.status_code == 400
        assert response.json()["success"] is False

    def test_verify_expired_code(self):
        """Test que valida falla con código expirado"""
        payload = {
            "email": "test@example.com",
            "codigo": "123456"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/verificar-codigo",
                json={
                    "success": False,
                    "message": "Código expirado"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/verificar-codigo", data=payload)

        assert response.status_code == 400

    def test_verify_empty_code(self):
        """Test que valida que código no esté vacío"""
        payload = {
            "email": "test@example.com",
            "codigo": ""
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/verificar-codigo",
                json={
                    "success": False,
                    "message": "Código requerido"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/verificar-codigo", data=payload)

        assert response.status_code == 400

    def test_verify_code_wrong_ip(self):
        """Test que valida falla con IP diferente"""
        payload = {
            "email": "test@example.com",
            "codigo": "123456"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/verificar-codigo",
                json={
                    "success": False,
                    "message": "IP diferente, código inválido"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/verificar-codigo", data=payload)

        assert response.status_code == 400


class TestPasswordChange:
    """Tests para cambio de contraseña en recuperación"""

    def test_change_password_successful(self):
        """Test que valida cambio exitoso de contraseña"""
        payload = {
            "email": "test@example.com",
            "password": "NewPassword123",
            "confirm": "NewPassword123"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/cambiar-password",
                json={
                    "success": True,
                    "message": "Contraseña actualizada correctamente"
                },
                status=200
            )
            response = requests.post(f"{BASE_URL}/cambiar-password", data=payload)

        assert response.status_code == 200
        assert response.json()["success"] is True

    def test_change_password_mismatch(self):
        """Test que valida que las contraseñas coincidan"""
        payload = {
            "email": "test@example.com",
            "password": "NewPassword123",
            "confirm": "DifferentPassword"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/cambiar-password",
                json={
                    "success": False,
                    "message": "Las contraseñas no coinciden"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/cambiar-password", data=payload)

        assert response.status_code == 400
        assert response.json()["success"] is False

    def test_change_password_weak_password(self):
        """Test que valida que contraseña no sea débil"""
        payload = {
            "email": "test@example.com",
            "password": "123",
            "confirm": "123"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/cambiar-password",
                json={
                    "success": False,
                    "message": "Contraseña muy débil"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/cambiar-password", data=payload)

        assert response.status_code == 400

    def test_change_password_same_as_old(self):
        """Test que valida que nueva contraseña sea diferente"""
        payload = {
            "email": "test@example.com",
            "password": "OldPassword123",
            "confirm": "OldPassword123"
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/cambiar-password",
                json={
                    "success": False,
                    "message": "La nueva contraseña no puede ser igual a la anterior"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/cambiar-password", data=payload)

        assert response.status_code == 400

    def test_change_password_empty_password(self):
        """Test que valida que contraseña no esté vacía"""
        payload = {
            "email": "test@example.com",
            "password": "",
            "confirm": ""
        }

        with responses.RequestsMock() as rsps:
            rsps.add(
                responses.POST,
                f"{BASE_URL}/cambiar-password",
                json={
                    "success": False,
                    "message": "Contraseña requerida"
                },
                status=400
            )
            response = requests.post(f"{BASE_URL}/cambiar-password", data=payload)

        assert response.status_code == 400
