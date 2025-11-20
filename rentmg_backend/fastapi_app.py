"""FastAPI app that mirrors the Flask API so both can run side-by-side.

Usage:
    uvicorn rentmg_backend.fastapi_app:app --reload --port 8000
"""
from datetime import datetime, timedelta
from typing import Generator, Optional

import jwt
from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pydantic import BaseModel, EmailStr
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session

from app.config import Config
from app.models import User, Property, Unit, Lease, Issue, Payment
from app.utils import hash_password, verify_password
from app.services_mpesa import stk_push

# SQLAlchemy session for FastAPI (independent of Flask app context)
engine = create_engine(Config.SQLALCHEMY_DATABASE_URI, pool_pre_ping=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)

security = HTTPBearer()


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def _user_payload(user: User):
    return {
        "id": user.id,
        "email": user.email,
        "role": user.role,
        "full_name": user.full_name,
        "created_at": user.created_at.isoformat(timespec="seconds") if user.created_at else None,
        "updated_at": user.updated_at.isoformat(timespec="seconds") if user.updated_at else None,
    }


def _property_payload(p: Property):
    return {
        "id": p.id,
        "name": p.name,
        "address": p.address,
        "landlord_id": p.landlord_id,
        "created_at": p.created_at.isoformat(timespec="seconds") if p.created_at else None,
        "updated_at": p.updated_at.isoformat(timespec="seconds") if p.updated_at else None,
    }


def _unit_payload(u: Unit):
    return {
        "id": u.id,
        "code": u.code,
        "rent_amount": u.rent_amount,
        "property_id": u.property_id,
        "created_at": u.created_at.isoformat(timespec="seconds") if u.created_at else None,
        "updated_at": u.updated_at.isoformat(timespec="seconds") if u.updated_at else None,
    }


def _lease_payload(l: Lease):
    return {
        "id": l.id,
        "unit_id": l.unit_id,
        "tenant_id": l.tenant_id,
        "start_date": l.start_date.isoformat() if l.start_date else None,
        "end_date": l.end_date.isoformat() if l.end_date else None,
        "status": l.status,
        "property_id": l.unit.property_id if l.unit else None,
        "created_at": l.created_at.isoformat(timespec="seconds") if l.created_at else None,
        "updated_at": l.updated_at.isoformat(timespec="seconds") if l.updated_at else None,
    }


def _issue_payload(i: Issue):
    return {
        "id": i.id,
        "title": i.title,
        "description": i.description,
        "status": i.status,
        "priority": i.priority,
        "reporter_id": i.reporter_id,
        "assignee_id": i.assignee_id,
        "property_id": i.property_id,
        "unit_id": i.unit_id,
        "created_at": i.created_at.isoformat(timespec="seconds") if i.created_at else None,
        "updated_at": i.updated_at.isoformat(timespec="seconds") if i.updated_at else None,
    }


def _payment_payload(p: Payment):
    return {
        "id": p.id,
        "lease_id": p.lease_id,
        "method": p.method,
        "amount": p.amount,
        "status": p.status,
        "reference": p.reference,
        "mpesa_checkout_id": p.mpesa_checkout_id,
        "created_at": p.created_at.isoformat(timespec="seconds") if p.created_at else None,
        "updated_at": p.updated_at.isoformat(timespec="seconds") if p.updated_at else None,
    }


def create_token(user: User) -> str:
    """Generate a JWT compatible with the payload shape used in the Flask API."""
    now = datetime.utcnow()
    payload = {
        "sub": {"id": user.id, "role": user.role},
        "iat": now,
        "exp": now + timedelta(hours=12),
        "type": "access",
        "fresh": False,
    }
    return jwt.encode(payload, Config.JWT_SECRET_KEY, algorithm="HS256")


def get_identity(credentials: HTTPAuthorizationCredentials = Depends(security)):
    token = credentials.credentials
    try:
        decoded = jwt.decode(token, Config.JWT_SECRET_KEY, algorithms=["HS256"])
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Token expired")
    except Exception:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")
    ident = decoded.get("sub") or decoded.get("identity")
    if not ident or "id" not in ident:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token payload")
    return ident


# ----------------------------
# Pydantic Schemas
# ----------------------------
class RegisterBody(BaseModel):
    email: EmailStr
    password: str
    role: str = "tenant"
    full_name: Optional[str] = None


class LoginBody(BaseModel):
    email: EmailStr
    password: str


class PropertyCreateBody(BaseModel):
    name: str
    address: str


class UnitCreateBody(BaseModel):
    code: str
    rent_amount: float
    property_id: int


class LeaseCreateBody(BaseModel):
    unit_id: int
    tenant_id: int
    start_date: Optional[str] = None
    end_date: Optional[str] = None
    status: str = "active"


class IssueCreateBody(BaseModel):
    title: str
    description: Optional[str] = None
    property_id: Optional[int] = None
    unit_id: Optional[int] = None
    priority: Optional[str] = "normal"


class IssueUpdateBody(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    status: Optional[str] = None
    priority: Optional[str] = None
    assignee_id: Optional[int] = None


class PaymentInitBody(BaseModel):
    lease_id: int
    amount: float
    phone: str


# ----------------------------
# FastAPI Application
# ----------------------------
app = FastAPI(title="RentMG FastAPI", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=Config.CORS_ORIGINS.split(","),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ----------------------------
# Auth
# ----------------------------
@app.post("/api/auth/register")
def register(payload: RegisterBody, db: Session = Depends(get_db)):
    if db.query(User).filter(User.email == payload.email).first():
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="email exists")
    user = User(
        email=payload.email,
        password_hash=hash_password(payload.password),
        role=payload.role,
        full_name=payload.full_name,
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    token = create_token(user)
    return {"message": "registered", "access_token": token, "user": _user_payload(user)}


@app.post("/api/auth/login")
def login(payload: LoginBody, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == payload.email).first()
    if not user or not verify_password(user.password_hash, payload.password):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="invalid credentials")
    token = create_token(user)
    return {"access_token": token, "user": _user_payload(user)}


@app.get("/api/auth/me")
def me(identity=Depends(get_identity), db: Session = Depends(get_db)):
    user = db.query(User).get(identity["id"])
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    return _user_payload(user)


# ----------------------------
# Properties
# ----------------------------
@app.get("/api/properties/")
def list_properties(identity=Depends(get_identity), db: Session = Depends(get_db)):
    q = db.query(Property)
    if identity["role"] == "landlord":
        q = q.filter(Property.landlord_id == identity["id"])
    return [_property_payload(p) for p in q.all()]


@app.get("/api/properties/{property_id}")
def get_property(property_id: int, identity=Depends(get_identity), db: Session = Depends(get_db)):
    prop = db.query(Property).get(property_id)
    if not prop:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Property not found")
    if identity["role"] == "landlord" and prop.landlord_id != identity["id"]:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="forbidden")
    return _property_payload(prop)


@app.post("/api/properties/")
def create_property(payload: PropertyCreateBody, identity=Depends(get_identity), db: Session = Depends(get_db)):
    if identity["role"] != "landlord":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="only landlord")
    prop = Property(name=payload.name, address=payload.address, landlord_id=identity["id"])
    db.add(prop)
    db.commit()
    db.refresh(prop)
    return _property_payload(prop)


# ----------------------------
# Units
# ----------------------------
@app.get("/api/units/by-property/{property_id}")
def list_units(property_id: int, identity=Depends(get_identity), db: Session = Depends(get_db)):
    units = db.query(Unit).filter(Unit.property_id == property_id).all()
    return [_unit_payload(u) for u in units]


@app.get("/api/units/{unit_id}")
def get_unit(unit_id: int, identity=Depends(get_identity), db: Session = Depends(get_db)):
    unit = db.query(Unit).get(unit_id)
    if not unit:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Unit not found")
    return _unit_payload(unit)


@app.post("/api/units/")
def create_unit(payload: UnitCreateBody, identity=Depends(get_identity), db: Session = Depends(get_db)):
    if identity["role"] != "landlord":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="only landlord")
    unit = Unit(code=payload.code, rent_amount=int(payload.rent_amount), property_id=payload.property_id)
    db.add(unit)
    db.commit()
    db.refresh(unit)
    return _unit_payload(unit)


# ----------------------------
# Leases
# ----------------------------
@app.get("/api/leases/")
def list_leases(identity=Depends(get_identity), db: Session = Depends(get_db)):
    q = db.query(Lease)
    if identity["role"] == "tenant":
        q = q.filter(Lease.tenant_id == identity["id"])
    leases = q.all()
    return [_lease_payload(l) for l in leases]


@app.post("/api/leases/")
def create_lease(payload: LeaseCreateBody, identity=Depends(get_identity), db: Session = Depends(get_db)):
    if identity["role"] != "landlord":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="only landlord")

    def parse_date(val: Optional[str]):
        if not val:
            return None
        try:
            return datetime.fromisoformat(val).date()
        except ValueError:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid date format")

    lease = Lease(
        unit_id=payload.unit_id,
        tenant_id=payload.tenant_id,
        status=payload.status,
        start_date=parse_date(payload.start_date),
        end_date=parse_date(payload.end_date),
    )
    db.add(lease)
    db.commit()
    db.refresh(lease)
    return _lease_payload(lease)


# ----------------------------
# Issues
# ----------------------------
@app.get("/api/issues/")
def list_issues(identity=Depends(get_identity), db: Session = Depends(get_db)):
    q = db.query(Issue)
    if identity["role"] == "tenant":
        q = q.filter(Issue.reporter_id == identity["id"])
    return [_issue_payload(i) for i in q.order_by(Issue.created_at.desc()).all()]


@app.post("/api/issues/")
def create_issue(payload: IssueCreateBody, identity=Depends(get_identity), db: Session = Depends(get_db)):
    issue = Issue(
        title=payload.title,
        description=payload.description,
        reporter_id=identity["id"],
        priority=payload.priority or "normal",
        property_id=payload.property_id,
        unit_id=payload.unit_id,
    )
    db.add(issue)
    db.commit()
    db.refresh(issue)
    return _issue_payload(issue)


@app.patch("/api/issues/{issue_id}")
def update_issue(issue_id: int, payload: IssueUpdateBody, identity=Depends(get_identity), db: Session = Depends(get_db)):
    issue = db.query(Issue).get(issue_id)
    if not issue:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Issue not found")
    for field, value in payload.dict(exclude_none=True).items():
        setattr(issue, field, value)
    db.commit()
    return {"message": "updated"}


# ----------------------------
# Payments
# ----------------------------
@app.post("/api/payments/mpesa/initiate")
def mpesa_initiate(payload: PaymentInitBody, identity=Depends(get_identity), db: Session = Depends(get_db)):
    lease = db.query(Lease).get(payload.lease_id)
    if not lease:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Lease not found")
    if identity["role"] == "tenant" and lease.tenant_id != identity["id"]:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="not allowed")

    payment = Payment(lease_id=payload.lease_id, method="mpesa", amount=int(payload.amount), status="pending")
    db.add(payment)
    db.commit()
    db.refresh(payment)

    try:
        mpesa_resp = stk_push(phone=str(payload.phone), amount=int(payload.amount), account_reference=f"LEASE{payload.lease_id}")
        payment.mpesa_checkout_id = mpesa_resp.get("CheckoutRequestID")
        db.commit()
    except Exception as exc:
        payment.status = "failed"
        db.commit()
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY, detail=f"Mpesa initiation failed: {exc}")

    return {
        "message": "Payment initiated",
        "payment_id": payment.id,
        "mpesa_checkout_id": payment.mpesa_checkout_id,
    }


@app.post("/api/payments/mpesa/callback")
def mpesa_callback(payload: dict, db: Session = Depends(get_db)):
    result_code = payload.get("Body", {}).get("stkCallback", {}).get("ResultCode")
    checkout_id = payload.get("Body", {}).get("stkCallback", {}).get("CheckoutRequestID")
    if checkout_id:
        payment = db.query(Payment).filter(Payment.mpesa_checkout_id == checkout_id).first()
        if payment:
            payment.status = "completed" if result_code == 0 else "failed"
            if result_code == 0:
                try:
                    payment.reference = next(
                        i["Value"]
                        for i in payload["Body"]["stkCallback"]["CallbackMetadata"]["Item"]
                        if i["Name"] == "MpesaReceiptNumber"
                    )
                except Exception:
                    payment.reference = payment.reference
            db.commit()
    return {"ResultCode": 0, "ResultDesc": "Accepted"}


@app.get("/api/payments/{payment_id}")
def get_payment(payment_id: int, identity=Depends(get_identity), db: Session = Depends(get_db)):
    q = db.query(Payment).join(Lease, Payment.lease_id == Lease.id)
    if identity["role"] == "tenant":
        q = q.filter(Lease.tenant_id == identity["id"])
    elif identity["role"] == "landlord":
        q = q.join(Unit, Lease.unit_id == Unit.id).join(Property, Unit.property_id == Property.id).filter(Property.landlord_id == identity["id"])
    payment = q.filter(Payment.id == payment_id).first()
    if not payment:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Payment not found")
    return _payment_payload(payment)


@app.get("/api/payments/history")
def payment_history(lease_id: Optional[int] = None, identity=Depends(get_identity), db: Session = Depends(get_db)):
    q = db.query(Payment).join(Lease, Payment.lease_id == Lease.id)
    if identity["role"] == "tenant":
        q = q.filter(Lease.tenant_id == identity["id"])
    elif identity["role"] == "landlord":
        q = q.join(Unit, Lease.unit_id == Unit.id).join(Property, Unit.property_id == Property.id).filter(Property.landlord_id == identity["id"])
    if lease_id:
        q = q.filter(Payment.lease_id == lease_id)
    payments = q.order_by(Payment.created_at.desc()).all()
    return [_payment_payload(p) for p in payments]
