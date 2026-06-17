# Шаблоны источников пользователей

Эти файлы задают платформенный контракт для будущего развертывания. Приложение продолжает работать только через Keycloak и OIDC, а конкретный источник пользователей подключается на стороне Keycloak.

Режимы:

- `LOCAL` - локальные пользователи Keycloak для разработки
- `AD` - сотрудники из Active Directory
- `FREEIPA` - сотрудники из FreeIPA
- `LDAP` - сотрудники из LDAP-каталога
- `EXTERNAL_OIDC` - вход через уже существующий внешний OIDC-провайдер

В приложении режим выбирается переменной:

```properties
IDENTITY_MODE=LOCAL
```

Внешний клиент предназначен для пациентов:

```properties
OAUTH2_PUBLIC_CLIENT_ID=medical-appointment-public
```

Внутренний клиент предназначен для сотрудников:

```properties
OAUTH2_INTERNAL_CLIENT_ID=medical-appointment-internal
```
