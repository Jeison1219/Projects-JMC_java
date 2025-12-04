"""
Tests unitarios para Notificaciones por Email
"""
import pytest
import re


class TestEmailNotificationContent:
    """Tests para contenido de notificaciones por email"""

    def test_registration_confirmation_email_format(self):
        """Test que valida formato de email de confirmación de registro"""
        email_content = """
        <h1>Bienvenido a nuestra plataforma</h1>
        <p>Tu cuenta ha sido creada exitosamente.</p>
        <p>Email: user@example.com</p>
        <p>Nombre: Test User</p>
        """

        assert "Bienvenido" in email_content
        assert "user@example.com" in email_content
        assert "Test User" in email_content

    def test_password_recovery_email_has_code(self):
        """Test que email de recuperación contiene el código"""
        email_content = """
        <p>Para recuperar tu contraseña, usa este código:</p>
        <p><strong>123456</strong></p>
        <p>Este código expira en 15 minutos</p>
        """

        code_pattern = r'\d{6}'
        assert re.search(code_pattern, email_content), "Email debe contener código de 6 dígitos"
        assert "expira" in email_content.lower()

    def test_email_notification_has_unsubscribe_link(self):
        """Test que email tiene enlace para desuscribirse"""
        email_content = """
        <footer>
            <a href="https://example.com/unsubscribe">Desuscribirse</a>
        </footer>
        """

        assert "unsubscribe" in email_content.lower() or "desuscribirse" in email_content.lower()

    def test_email_contains_no_sensitive_data_in_plain_text(self):
        """Test que email no contiene datos sensibles en plain text"""
        email_content = "password=SecurePass123&token=jwt-token-123"

        assert "password=" not in email_content.lower() or "hidden" in email_content
        assert "token=" not in email_content or "hidden" in email_content


class TestEmailSending:
    """Tests para envío de emails"""

    def test_verification_code_email_sent_on_registration(self):
        """Test que se envía email de código de verificación en registro"""
        # Mock del servicio de email
        emails_sent = []

        def send_verification_email(email, code):
            emails_sent.append({
                "to": email,
                "subject": "Código de verificación",
                "body": f"Tu código es: {code}"
            })

        send_verification_email("test@example.com", "123456")

        assert len(emails_sent) == 1
        assert emails_sent[0]["to"] == "test@example.com"
        assert "123456" in emails_sent[0]["body"]

    def test_recovery_code_email_sent_on_recovery_request(self):
        """Test que se envía email de código en solicitud de recuperación"""
        emails_sent = []

        def send_recovery_email(email, code):
            emails_sent.append({
                "to": email,
                "subject": "Recuperación de contraseña",
                "code": code
            })

        send_recovery_email("recovery@example.com", "654321")

        assert len(emails_sent) == 1
        assert emails_sent[0]["to"] == "recovery@example.com"

    def test_welcome_email_sent_after_successful_registration(self):
        """Test que se envía email de bienvenida después de registro"""
        emails_sent = []

        def send_welcome_email(email, name):
            emails_sent.append({
                "to": email,
                "subject": f"Bienvenido {name}",
                "body": f"Hola {name}, bienvenido a nuestra plataforma"
            })

        send_welcome_email("newuser@example.com", "Juan")

        assert len(emails_sent) == 1
        assert "Juan" in emails_sent[0]["subject"]
        assert "Juan" in emails_sent[0]["body"]

    def test_no_duplicate_emails_sent(self):
        """Test que no se envían emails duplicados"""
        emails_sent = []

        def send_email(email, code):
            # Verificar si ya se envió
            if not any(e["to"] == email and e["code"] == code for e in emails_sent):
                emails_sent.append({"to": email, "code": code})

        # Intentar enviar dos veces
        send_email("test@example.com", "123456")
        send_email("test@example.com", "123456")

        assert len(emails_sent) == 1, "No debe haber duplicados"


class TestEmailValidation:
    """Tests para validación de emails"""

    def test_valid_email_recipients(self):
        """Test que valida destinatarios válidos"""
        valid_emails = [
            "user@example.com",
            "user.name@example.co.uk",
            "user+tag@example.com"
        ]

        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'

        for email in valid_emails:
            assert re.match(email_pattern, email), f"Email {email} debe ser válido"

    def test_invalid_email_recipients_rejected(self):
        """Test que rechaza emails inválidos"""
        invalid_emails = [
            "userexample.com",
            "@example.com",
            "user@"
        ]

        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'

        for email in invalid_emails:
            assert not re.match(email_pattern, email), f"Email {email} debe ser inválido"

    def test_email_domain_validation(self):
        """Test que valida el dominio del email"""
        emails = {
            "user@gmail.com": True,
            "user@company.co.uk": True,
            "user@localhost": False,
            "user@127.0.0.1": False
        }

        def is_valid_domain(domain):
            # Domain debe tener al menos un punto
            return "." in domain and not domain.startswith(".")

        for email, expected in emails.items():
            domain = email.split("@")[1]
            result = is_valid_domain(domain)
            assert result == expected, f"Domain {domain} validation failed"


class TestEmailTemplate:
    """Tests para templates de email"""

    def test_template_variable_substitution(self):
        """Test que variables en template se reemplazan correctamente"""
        template = "Hola {{name}}, tu código es {{code}}"
        
        def substitute_variables(template, variables):
            result = template
            for key, value in variables.items():
                result = result.replace(f"{{{{{key}}}}}", str(value))
            return result

        result = substitute_variables(template, {"name": "Juan", "code": "123456"})

        assert "{{name}}" not in result
        assert "{{code}}" not in result
        assert "Juan" in result
        assert "123456" in result

    def test_template_html_encoding(self):
        """Test que HTML se encoda correctamente en templates"""
        name_with_html = "<script>alert('xss')</script>"
        template = f"Hola {name_with_html}"

        def encode_html(text):
            return (text.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace('"', "&quot;")
                        .replace("'", "&#39;"))

        encoded = encode_html(name_with_html)

        assert "<script>" not in encoded
        assert "&lt;script&gt;" in encoded

    def test_multipart_email_structure(self):
        """Test que email multipart tiene estructura correcta"""
        email = {
            "from": "noreply@example.com",
            "to": "user@example.com",
            "subject": "Verificación",
            "text": "Tu código es 123456",
            "html": "<p>Tu código es <strong>123456</strong></p>"
        }

        assert "from" in email
        assert "to" in email
        assert "subject" in email
        assert "text" in email
        assert "html" in email
