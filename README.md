# Medical Appointment System

Веб-приложение для записи пациентов к врачам, работы регистратуры, просмотра приемов врачом и управления расписанием главным врачом

## Реализовано

- публичная часть для пациента: выбор врача, запись, подтверждение, кабинет, отмена записи
- внутренняя часть для сотрудников: регистратура, рабочее место врача, администрирование расписания
- роли `PATIENT`, `REGISTRAR`, `DOCTOR`, `CHIEF_DOCTOR`
- OIDC через Keycloak, отдельные публичный и внутренний клиенты
- режимы входа `local`, `ad`, `freeipa`, `ldap`, `external-oidc`
- PostgreSQL, Flyway, jOOQ
- REST API и OpenAPI в непроизводственном режиме
- Dockerfile, compose, Caddy reverse proxy
- тесты безопасности и интеграционные smoke-проверки

## Быстрый запуск локально

Windows:

```powershell
.\gradlew.bat bootRun
```

Linux:

```bash
./gradlew bootRun
```

Приложение откроется на http://localhost:8080

OpenAPI в локальном режиме:

- http://localhost:8080/swagger-ui.html
- http://localhost:8080/v3/api-docs

## Запуск через Docker Compose

Windows:

```powershell
Copy-Item .env.local.example .env
docker compose up --build
```

Linux:

```bash
cp .env.local.example .env
docker compose up --build
```

Адреса:

- публичная часть: http://public.localhost:8080
- внутренняя часть: http://internal.localhost:8080
- Keycloak: http://auth.localhost:8080

Остановка:

Windows и Linux:

```bash
docker compose down
```

## Тестовые пользователи

Пользователи задаются импортом realm Keycloak из `docker/keycloak/realm/medical-appointment-realm.json`

- `patient` — пациент
- `registrar` — регистратор
- `doctor` — врач
- `chief-doctor` — главный врач

Пароли смотри в realm-файле или переопредели при подготовке окружения

## Проверка качества

Windows:

```powershell
.\gradlew.bat spotlessCheck test
```

Linux:

```bash
./gradlew spotlessCheck test
```

Автоформатирование:

Windows:

```powershell
.\gradlew.bat spotlessApply
```

Linux:

```bash
./gradlew spotlessApply
```

## Pre-commit hook

Установка локального hook:

Windows:

```powershell
.\gradlew.bat installGitHooks
```

Linux:

```bash
./gradlew installGitHooks
```

Hook запускает `spotlessApply` перед коммитом

## Документация

- [Обзор документации](docs/index.ipynb)
- [Сценарии пользователей](docs/user-scenarios.ipynb)
- [Настройка приложения](docs/configuration.ipynb)
- [Запуск и окружение](docs/deployment.ipynb)
- [Безопасность и SSO](docs/security-sso.ipynb)
- [Тестирование](docs/testing.ipynb)
- [Эксплуатация](docs/operations.ipynb)

Диаграммы лежат в `docs/diagrams`
