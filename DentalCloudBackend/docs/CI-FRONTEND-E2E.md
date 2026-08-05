# Caller CI E2E frontend-backend

El workflow [`frontend-e2e.yml`](../../.github/workflows/frontend-e2e.yml)
invoca el workflow reutilizable del repositorio frontend. Recibe explícitamente
los dos refs que deben probarse:

- `frontend_ref`: rama, tag o SHA del frontend;
- `backend_ref`: rama, tag o SHA del backend con la fixture `e2e`.

## Ejecución

Después de publicar ambas ramas, iniciar **Actions → Frontend integration E2E**
desde el backend y confirmar los refs. El caller delega en el workflow del
frontend la creación de PostgreSQL, el build, el seed, Playwright y los
artefactos de diagnóstico.

La configuración actual usa como defaults las ramas de trabajo locales
`ci/F5-X-01-e2e` y `ci/F5-X-01-cross-repo`; deben existir en sus respectivos
remotos antes de ejecutar el job. No se deben usar credenciales ni datos
clínicos de producción.

F5-X-01 requiere una ejecución exitosa con ambos SHAs registrados y sin errores
inesperados de consola o página.
