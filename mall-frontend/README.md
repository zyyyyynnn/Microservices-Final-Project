# MallCloud Frontend

Vue 3 + Vite + TypeScript front-end for the MallCloud course demo.

## Run

```powershell
npm install
npm run dev
```

Default URL:

```text
http://localhost:5173
```

The Vite dev server proxies `/api/v1/**` to `http://localhost:9100`, so all business requests still go through MallCloud Gateway.

## Build

```powershell
npm run build
```

The app does not use mock data. If backend services are unavailable, pages show API errors instead of fabricating success states.
