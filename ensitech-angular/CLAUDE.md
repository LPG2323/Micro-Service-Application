# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm start          # ng serve — dev server at http://localhost:4200
npm run build      # ng build — output to dist/
npm test           # ng test — Karma/Jasmine unit tests
npm run watch      # ng build --watch --configuration development
```

Run a single test file:
```bash
npx ng test --include='**/students.service.spec.ts'
```

Generate Angular artifacts:
```bash
npx ng generate component components/my-component
npx ng generate service services/my-service
```

## Architecture

**Framework:** Angular 16 (standalone-free, module-based). All declarations live in `AppModule` (`src/app/app.module.ts`).

**API base URL:** Configured in `src/apiUrl.ts` — a single exported constant. Switch between local and remote backends by editing that file.

**Auth flow:**
- `AuthService` stores the JWT response in `localStorage` under the key `user`.
- `AuthInterceptor` attaches `Authorization: Bearer <token>` to every non-login request.
- `HttpErrorInterceptor` catches 4xx/5xx errors and shows ngx-toastr notifications.
- Two functional guards protect routes: `authGuard` (checks `localStorage`) and `roleGuard` (reads `route.data.role` and compares against `user.role`).
- On app bootstrap, `APP_INITIALIZER` calls `authService.checkAuthentication()`.

**Roles:** `directeur` and `responsable`. The `dashboard` route is restricted to `directeur` only; all other management routes allow both roles.

**Services pattern:** Each domain has a service in `src/app/services/<domain>/` that imports `api_URL` from `src/apiUrl` and builds its endpoint (e.g., `api_URL + 'students'`). Services use `HttpClient` and return `Observable<T>`.

**Interfaces / models:** Defined in `src/app/interfaces/`. Keep backend DTO shapes there (e.g., `Student`, `Teacher`, `Course`, `Evaluation`, `AcademicYear`, `Registration`, `Speciality`).

**Dark mode:** `DarkmodeService` (`src/app/theme/`) toggles a `dark-mode` CSS class on `<body>` and persists the preference to `localStorage` under `themeMode`.

**Charts:** `ng2-charts` (Chart.js wrapper) used in `DashboardMainComponent` for bar charts. Refresh charts by replacing the entire data object reference (immutable update pattern).

**Shared utilities:**
- `PaginationComponent` — reusable pagination (`src/app/components/shared/pagination/`)
- `TrimPipe`, `FilterpipePipe`, `FiltercoursePipe`, `PhoneFormatPipe` — declared in `AppModule`
- `toggle-sidebar.ts` (`src/app/components/utils/`) — sidebar toggle helper

**Test credentials (dev only):**
- Director — username: `Patrick`, password: `AE!rkN$ba3y6zoS!`
- Head of Studies — username: `Sophie`, password: `Q&is4FmEGedAFmek`
