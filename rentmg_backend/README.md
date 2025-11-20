# RentMG Flask Backend (MySQL + JWT + Mpesa Sandbox)

This backend exposes REST APIs for landlords and tenants.

## Quick Start

```bash
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env   # fill values
export FLASK_APP=wsgi:app
flask db init && flask db migrate -m "init" && flask db upgrade
python manage.py
```

### Key Endpoints
- `GET  /api/leases/` (list; tenant sees own, landlord sees all)
- `POST /api/leases/` (create; minimal for testing)

- `GET  /api/payments/{id}` (poll a single payment)
- `GET  /api/payments/history?lease_id=` (list payments)


- `POST /api/auth/register` {email,password,role(landlord|tenant),full_name}
- `POST /api/auth/login` -> {access_token}
- `GET  /api/properties/` (landlord lists own)
- `POST /api/properties/` (landlord create)
- `GET  /api/properties/<id>` (property details)
- `GET  /api/units/by-property/<id>`
- `GET  /api/units/<id>` (single unit, includes property_id)
- `POST /api/units/`

## FastAPI (side-by-side)

FastAPI mirrors the same endpoints so you can run Flask and FastAPI together.

```
uvicorn rentmg_backend.fastapi_app:app --reload --port 8000
```

Notes:
- Uses the same MySQL DSN from `.env` (`mysql+mysqlconnector://root:12345@localhost:3306/RentMG`)
- JWT secret is shared with Flask so tokens work across both stacks
- CORS origins are read from `CORS_ORIGINS`
- `GET  /api/issues/` (tenant sees own; landlord sees all)
- `POST /api/issues/` {title, description, property_id?, unit_id?}
- `PATCH /api/issues/<id>`
- `POST /api/payments/mpesa/initiate` {lease_id, amount, phone}
- `POST /api/payments/mpesa/callback` (public Daraja callback)

## Android Notes

- Use Retrofit with an OkHttp Interceptor that adds `Authorization: Bearer <token>` for /api/* calls
- Emulator can reach Flask at `http://10.0.2.2:5000/`
- Real device uses your LAN IP, add it to `CORS_ORIGINS`

## Organization

- `app/models.py` SQLAlchemy models
- `app/routes/*` modular blueprints
- `app/services_mpesa.py` Daraja STK Push (sandbox)
- `app/utils.py` password hashing
- JWT-based auth via `Flask-JWT-Extended`
