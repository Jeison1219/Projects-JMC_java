"""
Configuración global para los tests con pytest
"""
import pytest
import requests
from unittest.mock import Mock, patch, MagicMock

BASE_URL = "http://localhost:8080"


@pytest.fixture
def base_url():
    """Fixture que proporciona la URL base"""
    return BASE_URL


@pytest.fixture
def api_client():
    """Fixture que proporciona un cliente HTTP con sesión"""
    return requests.Session()


@pytest.fixture
def mock_session():
    """Fixture que proporciona una sesión mock"""
    return MagicMock(spec=requests.Session)


@pytest.fixture
def valid_user_data():
    """Fixture con datos válidos de usuario"""
    return {
        "name": "Test User",
        "email": "test@example.com",
        "password": "Test1234",
        "confirmPassword": "Test1234",
        "role": "USER"
    }


@pytest.fixture
def valid_login_data():
    """Fixture con datos válidos de login"""
    return {
        "email": "test@example.com",
        "password": "Test1234"
    }


@pytest.fixture
def valid_password_recovery_data():
    """Fixture con datos válidos de recuperación de contraseña"""
    return {
        "email": "test@example.com"
    }
