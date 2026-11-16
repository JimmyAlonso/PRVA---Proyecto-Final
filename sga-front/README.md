# SGA — JiyJu Glamping (frontend)

React 19 + Vite + TypeScript + React Router + Axios. Consume el API del backend (`../sga`).

## Ejecutar en local

```bash
npm install
npm run dev        # http://localhost:5173
```

Sin `VITE_API_URL`, la aplicación llama a `/api/v1` y el proxy de `vite.config.ts` lo reenvía a
`http://localhost:8080` (backend corriendo con `./gradlew bootRun`).

Para probar el front local contra el API desplegado:

```bash
VITE_API_URL=https://sga-api-jiyju.onrender.com/api/v1 npm run dev
```

## Construir

```bash
npm run build      # tsc + vite build → dist/
```

## Funcionalidades

| Quién | Qué puede hacer |
|---|---|
| Visitante | Ver los domos, tarifas por temporada, calendario de ocupación, cotizar una estancia con desglose por noche y buscar domos libres por fechas |
| Huésped | Crear cuenta, ingresar, reservar a su nombre (canal PORTAL), ver sus reservas con el desglose y la cuenta, y cancelar |
| Recepción y administración | Movimientos del día, listado de reservas con filtros, crear reservas por el canal DIRECTO, registrar pagos, confirmar, depósito, llegada, servicios adicionales, salida, no-show y cancelación |
| Personal de servicio | Domos pendientes de preparación: empezar y marcar como listos |

## Despliegue en Vercel (Guía 22)

| Campo | Valor |
|---|---|
| Root Directory | `sga-front` |
| Framework Preset | Vite |
| Variable `VITE_API_URL` | `https://sga-api-jiyju.onrender.com/api/v1` |

`vercel.json` responde `index.html` en cualquier ruta para que React Router resuelva la navegación
al recargar. Cambiar `VITE_API_URL` exige redesplegar, porque se incrusta al construir.
