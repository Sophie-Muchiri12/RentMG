from datetime import datetime
from .extensions import db

class BaseModel(db.Model):
    __abstract__ = True
    id = db.Column(db.Integer, primary_key=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

class User(BaseModel):
    __tablename__ = "users"
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(256), nullable=False)
    role = db.Column(db.String(20), default="tenant")  # landlord or tenant
    full_name = db.Column(db.String(120))

class Property(BaseModel):
    __tablename__ = "properties"
    name = db.Column(db.String(120), nullable=False)
    address = db.Column(db.String(255))
    landlord_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    landlord = db.relationship("User", backref="properties", foreign_keys=[landlord_id])

class Unit(BaseModel):
    __tablename__ = "units"
    code = db.Column(db.String(50), nullable=False)
    rent_amount = db.Column(db.Integer, nullable=False)
    property_id = db.Column(db.Integer, db.ForeignKey("properties.id"), nullable=False)
    property = db.relationship("Property", backref="units")

class Lease(BaseModel):
    __tablename__ = "leases"
    unit_id = db.Column(db.Integer, db.ForeignKey("units.id"), nullable=False)
    tenant_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    start_date = db.Column(db.Date)
    end_date = db.Column(db.Date, nullable=True)
    status = db.Column(db.String(20), default="active")
    unit = db.relationship("Unit", backref="leases")
    tenant = db.relationship("User", foreign_keys=[tenant_id])

class Payment(BaseModel):
    __tablename__ = "payments"
    lease_id = db.Column(db.Integer, db.ForeignKey("leases.id"), nullable=False)
    method = db.Column(db.String(20)) # mpesa, bank
    amount = db.Column(db.Integer, nullable=False)
    status = db.Column(db.String(20), default="pending")
    reference = db.Column(db.String(64))
    mpesa_checkout_id = db.Column(db.String(64))
    lease = db.relationship("Lease", backref="payments")

class Issue(BaseModel):
    __tablename__ = "issues"
    title = db.Column(db.String(120), nullable=False)
    description = db.Column(db.Text)
    status = db.Column(db.String(20), default="open")
    priority = db.Column(db.String(20), default="normal")
    reporter_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    assignee_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=True)
    property_id = db.Column(db.Integer, db.ForeignKey("properties.id"), nullable=True)
    unit_id = db.Column(db.Integer, db.ForeignKey("units.id"), nullable=True)
    reporter = db.relationship("User", foreign_keys=[reporter_id])
    assignee = db.relationship("User", foreign_keys=[assignee_id])
