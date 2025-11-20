from flask import Blueprint, request, jsonify
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity
from ..extensions import db
from ..models import User
from ..utils import hash_password, verify_password

bp = Blueprint("auth", __name__)


def _user_payload(user: User):
    """Serialize a user object for responses."""
    return {
        "id": user.id,
        "email": user.email,
        "role": user.role,
        "full_name": user.full_name,
        "created_at": user.created_at.isoformat(timespec="seconds") if user.created_at else None,
        "updated_at": user.updated_at.isoformat(timespec="seconds") if user.updated_at else None,
    }


@bp.post("/register")
def register():
    data = request.get_json() or {}
    email = data.get("email"); password = data.get("password"); role = data.get("role","tenant")
    if not email or not password: return jsonify({"error":"email and password required"}), 400
    if User.query.filter_by(email=email).first(): return jsonify({"error":"email exists"}), 409
    user = User(email=email, password_hash=hash_password(password), role=role, full_name=data.get("full_name"))
    db.session.add(user); db.session.commit()
    return jsonify({"message":"registered", "user": _user_payload(user)}), 201


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
