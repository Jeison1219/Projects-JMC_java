# Tests Unitarios para Proyecto Java - Guía Completa

Este directorio contiene una suite completa de tests unitarios en Python para validar la API del proyecto Java.

## 📋 Estructura de Tests

### 1. **test_auth_registration.py**
Tests para el proceso de registro de usuarios:
- Validación de contraseñas coincidentes
- Validación de emails duplicados
- Validación de datos requeridos
- Registro exitoso
- Manejo de roles

### 2. **test_auth_login.py**
Tests para el proceso de login:
- Login con credenciales válidas
- Validación de contraseña incorrecta
- Validación de email no registrado
- Seguridad contra ataques (SQL injection, etc.)
- Rate limiting de intentos fallidos

### 3. **test_password_recovery.py**
Tests para recuperación de contraseña:
- Solicitud de código de verificación
- Validación de código
- Cambio de contraseña
- Manejo de códigos expirados
- Validación de IP

### 4. **test_profile_management.py**
Tests para gestión de perfil:
- Obtener datos del perfil
- Actualizar información del perfil
- Cambio de contraseña desde perfil
- Validaciones de datos

### 5. **test_dto_validation.py**
Tests para validación de DTOs (Data Transfer Objects):
- Validación de UserRegistrationDto
- Validación de LoginDto
- Validación de PasswordRecoveryDto
- Validación de ProfileUpdateDto
- Campos requeridos y opcionales

### 6. **test_security_validation.py**
Tests para seguridad y validaciones:
- Prevención de SQL Injection
- Prevención de XSS
- Prevención de Command Injection
- Validación de emails
- Validación de contraseñas
- Manejo de caracteres especiales

### 7. **test_api_responses.py**
Tests para respuestas de API:
- Códigos de estado HTTP (200, 201, 400, 401, 403, 404, 500)
- Formato de respuestas JSON
- Campos requeridos en respuestas
- Content-Type headers
- Paginación

### 8. **test_api_integration.py**
Tests de integración:
- Flujo completo de registro y login
- Flujo de recuperación de contraseña
- Gestión de perfil
- Solicitudes concurrentes
- Manejo de errores

### 9. **test_email_notifications.py**
Tests para notificaciones por email:
- Contenido de emails
- Envío de códigos de verificación
- Validación de destinatarios
- Templates de email

## 🚀 Instalación

### 1. Instalar dependencias

```bash
pip install -r requirements.txt
```

### 2. Dependencias incluidas:
- `pytest` - Framework de testing
- `pytest-cov` - Coverage reporting
- `pytest-mock` - Mocking support
- `requests` - HTTP client
- `responses` - Mock HTTP responses
- `pytest-asyncio` - Async support

## 📝 Ejecución de Tests

### Ejecutar todos los tests:
```bash
pytest
```

### Ejecutar tests de un archivo específico:
```bash
pytest tests/test_auth_registration.py
```

### Ejecutar tests de una clase específica:
```bash
pytest tests/test_auth_registration.py::TestRegistrationValidation
```

### Ejecutar un test específico:
```bash
pytest tests/test_auth_registration.py::TestRegistrationValidation::test_register_password_mismatch
```

### Ejecutar con salida detallada:
```bash
pytest -v
```

### Ejecutar con salida muy detallada:
```bash
pytest -vv
```

### Ejecutar y mostrar prints:
```bash
pytest -s
```

### Generar reporte de cobertura:
```bash
pytest --cov=. --cov-report=html
```

### Ejecutar tests que contengan una palabra clave:
```bash
pytest -k "registration"
```

### Ejecutar tests que NO contengan una palabra clave:
```bash
pytest -k "not security"
```

## 📊 Cobertura de Tests

Los tests cubren las siguientes áreas:

### Autenticación
- ✅ Registro de usuarios
- ✅ Login
- ✅ Recuperación de contraseña
- ✅ Cambio de contraseña

### Validación
- ✅ Validación de emails
- ✅ Validación de contraseñas
- ✅ Validación de campos requeridos
- ✅ Validación de longitudes

### Seguridad
- ✅ Prevención de SQL Injection
- ✅ Prevención de XSS
- ✅ Prevención de Command Injection
- ✅ Manejo seguro de datos sensibles

### API
- ✅ Códigos de estado HTTP
- ✅ Formatos de respuesta
- ✅ Headers de respuesta
- ✅ Manejo de errores

### Integración
- ✅ Flujos completos de usuario
- ✅ Solicitudes concurrentes
- ✅ Recuperación de errores

## 🔧 Configuración

### conftest.py
Archivo de configuración global de pytest con fixtures reutilizables:
- `base_url` - URL base de la API
- `api_client` - Cliente HTTP
- `mock_session` - Sesión mock
- `valid_user_data` - Datos de usuario válidos
- `valid_login_data` - Datos de login válidos
- `valid_password_recovery_data` - Datos de recuperación válidos

## ✅ Requisitos Previos

1. **Backend Java corriendo**: Los tests asumen que el servidor está en `http://localhost:8080`
2. **Python 3.8+**: Requerido para ejecutar los tests
3. **Pip**: Para instalar dependencias

## 🐛 Solución de Problemas

### Error: ModuleNotFoundError: No module named 'responses'
```bash
pip install responses
```

### Error: Connection refused en localhost:8080
Asegúrate de que tu servidor Java está ejecutándose:
```bash
# En la carpeta del proyecto Java
./mvnw spring-boot:run
```

### Error: pytest: command not found
```bash
pip install pytest
```

## 📈 Mejores Prácticas

1. **Ejecutar tests localmente** antes de hacer commit
2. **Generar cobertura** regularmente para identificar código no testeado
3. **Actualizar tests** cuando cambies la API
4. **Usar fixtures** para datos reutilizables
5. **Agrupar tests** por funcionalidad

## 🎯 Próximos Pasos

Para mejorar la cobertura, considera agregar tests para:
- [ ] Validación de tokens JWT
- [ ] Expiración de sesiones
- [ ] Autorización por roles
- [ ] Historial de auditoría
- [ ] Rate limiting
- [ ] Cachés

## 📞 Soporte

Para preguntas o problemas, consulta la documentación oficial:
- [Pytest Documentation](https://docs.pytest.org/)
- [Requests Documentation](https://requests.readthedocs.io/)
- [Responses Documentation](https://responses.readthedocs.io/)
