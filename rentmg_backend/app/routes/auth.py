from flask import Blueprint, request, jsonify
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity
from ..extensions import db
from sqlalchemy import func
from ..models import User, Property
from ..utils import hash_password, verify_password

bp = Blueprint("auth", __name__)


def _user_payload(user: User):
    """Serialize a user object for responses."""
    prop = getattr(user, "linked_property", None)
    if not prop and user.property_id:
        prop = Property.query.get(user.property_id)
    prop_name = prop.name if prop else None
    return {
        "id": user.id,
        "email": user.email,
        "role": user.role,
        "full_name": user.full_name,
        "property_id": user.property_id,
        "property_name": prop_name,
        "created_at": user.created_at.isoformat(timespec="seconds") if user.created_at else None,
        "updated_at": user.updated_at.isoformat(timespec="seconds") if user.updated_at else None,
    }


@bp.post("/register")
def register():
    data = request.get_json() or {}
    email = data.get("email")
    password = data.get("password")
    role = (data.get("role") or "tenant").lower()
    property_name = (data.get("property_name") or "").strip()
    property_address = (data.get("property_address") or "").strip()

    if role not in ("tenant", "landlord", "property_manager"):
        return jsonify({"error": "role must be tenant, landlord, or property_manager"}), 400
    if not email or not password:
        return jsonify({"error": "email and password required"}), 400
    if User.query.filter_by(email=email).first():
        return jsonify({"error": "email exists"}), 409

    # Resolve property for tenants or ensure details for landlords
    selected_property = None
    if role == "tenant":
        if not property_name:
            return jsonify({"error": "property_name required for tenant signup"}), 400
        matches = Property.query.filter(func.lower(Property.name) == property_name.lower()).all()
        if not matches:
            return jsonify({"error": "property not found"}), 404
        if len(matches) > 1:
            return jsonify({"error": "multiple properties share that name; please use an exact/unique name"}), 409
        selected_property = matches[0]
    elif role == "landlord":
        if not property_name:
            return jsonify({"error": "property_name required to create landlord profile"}), 400
        if not property_address:
            return jsonify({"error": "property_address required to create landlord profile"}), 400

    user = User(email=email, password_hash=hash_password(password), role=role, full_name=data.get("full_name"))
    if selected_property:
        user.property_id = selected_property.id

    db.session.add(user)
    db.session.flush()  # populate user.id for property creation

    if role == "landlord":
        new_prop = Property(name=property_name, address=property_address or None, landlord_id=user.id)
        db.session.add(new_prop)
        db.session.flush()
        user.property_id = new_prop.id

    db.session.commit()
    token = create_access_token(identity={"id": user.id, "role": user.role})
    return jsonify({"message":"registered", "access_token": token, "user": _user_payload(user)}), 201


@bp.post("/login")
def login():
    data = request.get_json() or {}
    user = User.query.filter_by(email=data.get("email")).first()
    if not user or not verify_password(user.password_hash, data.get("password","")):
        return jsonify({"error":"invalid credentials"}), 401
    token = create_access_token(identity={"id": user.id, "role": user.role})
    return jsonify({"access_token": token, "user": _user_payload(user)})


@bp.get("/me")
@jwt_required()
def me():
    ident = get_jwt_identity()
    user = User.query.get_or_404(ident["id"])
    return jsonify(_user_payload(user))
