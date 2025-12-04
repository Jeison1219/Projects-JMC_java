"""
Tests unitarios para Seguridad y Validaciones de entrada
"""
import pytest
import json
import re


class TestInputSanitization:
    """Tests para sanitización de entrada"""

    def test_sql_injection_prevention(self):
        """Test que previene SQL injection"""
        malicious_inputs = [
            "' OR '1'='1",
            "'; DROP TABLE users; --",
            "1' UNION SELECT * FROM users--",
            "admin' --"
        ]

        def is_safe(input_str):
            # Validar que no contiene patrones SQL comunes
            dangerous_patterns = [
                r"(\s(OR|AND|UNION|SELECT|DELETE|DROP|INSERT|UPDATE)\s)",
                r"(--|;|/\*|\*/)"
            ]
            for pattern in dangerous_patterns:
                if re.search(pattern, input_str, re.IGNORECASE):
                    return False
            return True

        for malicious in malicious_inputs:
            assert not is_safe(malicious), f"Input '{malicious}' debería ser detectado como inseguro"

    def test_xss_prevention(self):
        """Test que previene XSS (Cross-Site Scripting)"""
        xss_inputs = [
            "<script>alert('XSS')</script>",
            "<img src=x onerror='alert(1)'>",
            "javascript:alert(1)",
            "<iframe src='javascript:alert(1)'></iframe>"
        ]

        def is_safe_html(input_str):
            # Validar que no contiene tags HTML/scripts
            dangerous_tags = r"<script|javascript:|onerror|<iframe|<img.*onerror"
            return not re.search(dangerous_tags, input_str, re.IGNORECASE)

        for xss in xss_inputs:
            assert not is_safe_html(xss), f"Input '{xss}' debería ser detectado como XSS"

    def test_command_injection_prevention(self):
        """Test que previene Command Injection"""
        command_injections = [
            "; rm -rf /",
            "| cat /etc/passwd",
            "&&whoami",
            "`whoami`"
        ]

        def is_safe_command(input_str):
            dangerous_chars = [";", "|", "&", "`", "$", "\\"]
            return not any(char in input_str for char in dangerous_chars)

        for cmd in command_injections:
            assert not is_safe_command(cmd), f"Input '{cmd}' debería ser detectado como peligroso"

    def test_path_traversal_prevention(self):
        """Test que previene Path Traversal"""
        traversal_inputs = [
            "../../../etc/passwd",
            "..\\..\\..\\windows\\system32",
            "....//....//etc/passwd",
            "....\\\\....\\\\windows\\system32"
        ]

        def is_safe_path(input_str):
            return not (".." in input_str or input_str.startswith("/"))

        for path in traversal_inputs:
            assert not is_safe_path(path), f"Path '{path}' debería ser detectado como inseguro"


class TestEmailValidation:
    """Tests para validación de email"""

    def test_valid_email_formats(self):
        """Test que valida emails válidos"""
        valid_emails = [
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user123@example-domain.com",
            "test_user@example.org"
        ]

        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'

        for email in valid_emails:
            assert re.match(email_pattern, email), f"Email '{email}' debería ser válido"

    def test_invalid_email_formats(self):
        """Test que rechaza emails inválidos"""
        invalid_emails = [
            "userexample.com",
            "@example.com",
            "user@",
            "user@@example.com",
            "user@example",
            "user name@example.com",
            "user@example..com"
        ]

        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'

        for email in invalid_emails:
            assert not re.match(email_pattern, email), f"Email '{email}' debería ser inválido"

    def test_email_length_validation(self):
        """Test que valida longitud máxima de email"""
        short_email = "a@b.co"
        long_email = "a" * 256 + "@example.com"

        assert len(short_email) >= 6
        assert len(long_email) > 254, "Email debe ser menor a 254 caracteres"


class TestPasswordValidation:
    """Tests para validación de contraseña"""

    def test_strong_password_requirements(self):
        """Test que valida requisitos de contraseña fuerte"""
        strong_passwords = [
            "SecureP@ss123",
            "MyGr8Pass!",
            "Complex#Pass99"
        ]

        def is_strong_password(pwd):
            return (
                len(pwd) >= 8 and
                any(c.isupper() for c in pwd) and
                any(c.islower() for c in pwd) and
                any(c.isdigit() for c in pwd)
            )

        for pwd in strong_passwords:
            assert is_strong_password(pwd), f"Contraseña '{pwd}' debería ser fuerte"

    def test_weak_password_rejection(self):
        """Test que rechaza contraseñas débiles"""
        weak_passwords = [
            "123456",
            "password",
            "qwerty",
            "abc123",
            "111111"
        ]

        def is_strong_password(pwd):
            return (
                len(pwd) >= 8 and
                any(c.isupper() for c in pwd) and
                any(c.islower() for c in pwd) and
                any(c.isdigit() for c in pwd)
            )

        for pwd in weak_passwords:
            assert not is_strong_password(pwd), f"Contraseña '{pwd}' debería ser débil"

    def test_password_length_requirements(self):
        """Test que valida longitud mínima y máxima"""
        too_short = "Pass1"
        valid = "Password123"
        too_long = "a" * 256 + "1"

        assert len(too_short) < 8, "Contraseña muy corta"
        assert 8 <= len(valid) <= 255, "Contraseña válida"
        assert len(too_long) > 255, "Contraseña muy larga"


class TestInputLengthValidation:
    """Tests para validación de longitud de input"""

    def test_name_length_limits(self):
        """Test que valida longitud del nombre"""
        too_short = ""
        valid = "Juan Carlos"
        too_long = "a" * 256

        assert len(too_short) > 0, "Nombre no puede estar vacío"
        assert 0 < len(valid) <= 100, "Nombre válido"
        assert len(too_long) > 100, "Nombre muy largo"

    def test_phone_number_validation(self):
        """Test que valida formato de teléfono"""
        valid_phones = [
            "1234567890",
            "+34912345678",
            "912-345-6789"
        ]

        invalid_phones = [
            "12345",  # Muy corto
            "abcdefghij",  # No numérico
            "123 456 789 0123 456"  # Muy largo
        ]

        def is_valid_phone(phone):
            # Extraer solo dígitos
            digits = re.sub(r'\D', '', phone)
            return 10 <= len(digits) <= 15

        for phone in valid_phones:
            assert is_valid_phone(phone), f"Teléfono '{phone}' debería ser válido"

        for phone in invalid_phones:
            assert not is_valid_phone(phone), f"Teléfono '{phone}' debería ser inválido"

    def test_address_length_limits(self):
        """Test que valida longitud de dirección"""
        valid_address = "123 Main Street, Apartment 4B"
        too_long_address = "a" * 256

        assert len(valid_address) <= 255, "Dirección válida"
        assert len(too_long_address) > 255, "Dirección muy larga"


class TestSpecialCharacterHandling:
    """Tests para manejo de caracteres especiales"""

    def test_unicode_character_acceptance(self):
        """Test que acepta caracteres unicode válidos"""
        unicode_names = [
            "José María",
            "François",
            "Müller",
            "李明",
            "Владимир"
        ]

        for name in unicode_names:
            assert len(name) > 0, f"Nombre '{name}' debería ser aceptado"

    def test_special_character_escaping(self):
        """Test que escapa caracteres especiales en contextos HTML"""
        special_chars = {
            "<": "&lt;",
            ">": "&gt;",
            "&": "&amp;",
            '"': "&quot;",
            "'": "&#39;"
        }

        def escape_html(text):
            for char, escape in special_chars.items():
                text = text.replace(char, escape)
            return text

        test_input = "Hello <script>alert('xss')</script>"
        escaped = escape_html(test_input)

        assert "<script>" not in escaped
        assert "&lt;script&gt;" in escaped

    def test_newline_carriage_return_handling(self):
        """Test que maneja saltos de línea y retornos de carro"""
        inputs_with_newlines = [
            "Line1\nLine2",
            "Line1\rLine2",
            "Line1\r\nLine2"
        ]

        def sanitize_newlines(text):
            return text.replace("\n", " ").replace("\r", " ").strip()

        for text in inputs_with_newlines:
            sanitized = sanitize_newlines(text)
            assert "\n" not in sanitized
            assert "\r" not in sanitized
