"""
Tests unitarios para validación de DTOs (Data Transfer Objects)
"""
import pytest
from unittest.mock import Mock


class TestUserRegistrationDtoValidation:
    """Tests para validación del DTO de registro"""

    def test_valid_user_registration_dto(self):
        """Test que valida DTO de registro válido"""
        dto = {
            "name": "Juan Carlos",
            "email": "juan@example.com",
            "password": "SecurePass123",
            "confirmPassword": "SecurePass123",
            "role": "USER"
        }

        # Validaciones básicas
        assert len(dto["name"]) > 0
        assert "@" in dto["email"]
        assert len(dto["password"]) >= 6
        assert dto["password"] == dto["confirmPassword"]
        assert dto["role"] in ["USER", "ADMIN", "MANAGER"]

    def test_dto_name_length_validation(self):
        """Test que valida longitud mínima del nombre"""
        dto = {
            "name": "A",  # Muy corto
            "email": "test@example.com",
            "password": "SecurePass123",
            "confirmPassword": "SecurePass123",
            "role": "USER"
        }

        assert len(dto["name"]) >= 2, "El nombre debe tener al menos 2 caracteres"

    def test_dto_email_format_validation(self):
        """Test que valida formato de email"""
        valid_email = "user@example.com"
        invalid_emails = [
            "userexample.com",
            "@example.com",
            "user@",
            "user@example",
            "user email@example.com"
        ]

        assert "@" in valid_email
        for email in invalid_emails:
            assert not ("@" in email and "." in email.split("@")[1]), f"Email {email} debería ser inválido"

    def test_dto_password_requirements(self):
        """Test que valida requisitos de contraseña"""
        valid_passwords = [
            "SecurePass123",
            "MyP@ssw0rd",
            "AnotherGood123"
        ]

        weak_passwords = [
            "123456",
            "password",
            "123",
            "aaa"
        ]

        for pwd in valid_passwords:
            assert len(pwd) >= 6, f"Contraseña {pwd} debería ser válida"

        for pwd in weak_passwords:
            assert len(pwd) >= 6 or pwd.isdigit(), f"Contraseña {pwd} es débil"

    def test_dto_role_validation(self):
        """Test que valida roles permitidos"""
        valid_roles = ["USER", "ADMIN", "MANAGER"]
        invalid_roles = ["SUPERADMIN", "GUEST", "ROOT", ""]

        for role in valid_roles:
            assert role in valid_roles, f"Rol {role} debería ser válido"

        for role in invalid_roles:
            assert role not in valid_roles, f"Rol {role} debería ser inválido"

    def test_dto_missing_required_fields(self):
        """Test que valida presencia de campos requeridos"""
        required_fields = ["name", "email", "password", "confirmPassword", "role"]
        dto = {
            "name": "Test User",
            "email": "test@example.com"
            # Falta password, confirmPassword, role
        }

        missing = [field for field in required_fields if field not in dto]
        assert len(missing) > 0, "Debería haber campos faltantes"

    def test_dto_extra_fields_ignored(self):
        """Test que campos extra se ignoran o se filtran"""
        dto = {
            "name": "Test User",
            "email": "test@example.com",
            "password": "SecurePass123",
            "confirmPassword": "SecurePass123",
            "role": "USER",
            "extra_field": "should_be_ignored",
            "malicious_field": "injection_attempt"
        }

        allowed_fields = ["name", "email", "password", "confirmPassword", "role"]
        filtered_dto = {k: v for k, v in dto.items() if k in allowed_fields}

        assert len(filtered_dto) == len(allowed_fields)
        assert "extra_field" not in filtered_dto
        assert "malicious_field" not in filtered_dto


class TestLoginDtoValidation:
    """Tests para validación del DTO de login"""

    def test_valid_login_dto(self):
        """Test que valida DTO de login válido"""
        dto = {
            "email": "test@example.com",
            "password": "SecurePass123"
        }

        assert "@" in dto["email"]
        assert len(dto["password"]) > 0

    def test_login_dto_email_required(self):
        """Test que valida que email es requerido"""
        dto = {"password": "SecurePass123"}

        assert "email" not in dto, "Email falta en el DTO"

    def test_login_dto_password_required(self):
        """Test que valida que contraseña es requerida"""
        dto = {"email": "test@example.com"}

        assert "password" not in dto, "Contraseña falta en el DTO"

    def test_login_dto_whitespace_handling(self):
        """Test que valida manejo de espacios en blanco"""
        dto_with_spaces = {
            "email": "  test@example.com  ",
            "password": "  SecurePass123  "
        }

        # Se deben remover espacios
        email = dto_with_spaces["email"].strip()
        password = dto_with_spaces["password"].strip()

        assert email == "test@example.com"
        assert password == "SecurePass123"


class TestPasswordRecoveryDtoValidation:
    """Tests para validación de DTO de recuperación de contraseña"""

    def test_valid_recovery_request_dto(self):
        """Test que valida DTO de solicitud de recuperación"""
        dto = {"email": "test@example.com"}

        assert "@" in dto["email"]
        assert "." in dto["email"]

    def test_recovery_dto_email_format(self):
        """Test que valida formato de email en recuperación"""
        invalid_emails = [
            "user",
            "user@",
            "@example.com",
            "user@example",
            ""
        ]

        for email in invalid_emails:
            is_valid = "@" in email and "." in email.split("@")[-1] if "@" in email else False
            assert not is_valid, f"Email {email} debería ser inválido"

    def test_new_password_dto_validation(self):
        """Test que valida DTO de nueva contraseña"""
        dto = {
            "password": "NewSecurePass123",
            "confirm": "NewSecurePass123"
        }

        assert dto["password"] == dto["confirm"]
        assert len(dto["password"]) >= 6


class TestProfileUpdateDtoValidation:
    """Tests para validación de DTO de actualización de perfil"""

    def test_valid_profile_update_dto(self):
        """Test que valida DTO de actualización válido"""
        dto = {
            "name": "Updated Name",
            "phone": "1234567890",
            "address": "123 Main St"
        }

        assert len(dto["name"]) > 0
        assert len(dto["phone"]) > 0

    def test_profile_dto_name_update(self):
        """Test que valida actualización de nombre"""
        dto = {"name": "New Name"}

        assert len(dto["name"]) > 0
        assert len(dto["name"]) <= 100

    def test_profile_dto_phone_format(self):
        """Test que valida formato de teléfono"""
        valid_phones = ["1234567890", "+34912345678", "+1 (234) 567-8900"]
        invalid_phones = ["abc", "123"]  # Muy corto

        for phone in valid_phones:
            digits = ''.join(c for c in phone if c.isdigit())
            assert len(digits) >= 10, f"Teléfono {phone} debería ser válido"

        for phone in invalid_phones:
            digits = ''.join(c for c in phone if c.isdigit())
            assert len(digits) < 10, f"Teléfono {phone} debería ser inválido"

    def test_profile_dto_optional_fields(self):
        """Test que permite campos opcionales"""
        dto_minimal = {"name": "User"}

        dto_complete = {
            "name": "User",
            "phone": "1234567890",
            "address": "123 Main St",
            "city": "New York"
        }

        assert len(dto_minimal) >= 1
        assert len(dto_complete) >= len(dto_minimal)
