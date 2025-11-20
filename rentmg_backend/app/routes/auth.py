from flask import Blueprint, request, jsonify
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity
from ..extensions import db
from ..models import User
from ..utils import hash_password, verify_password

bp = Blueprint("auth", __name__)

@bp.post("/register")
def register():
    data = request.get_json() or {}
    email = data.get("email"); password = data.get("password"); role = data.get("role","tenant")
    if not email or not password: return jsonify({"error":"email and password required"}), 400
    if User.query.filter_by(email=email).first(): return jsonify({"error":"email exists"}), 409
    user = User(email=email, password_hash=hash_password(password), role=role, full_name=data.get("full_name"))
    db.session.add(user); db.session.commit()
    return jsonify({"message":"registered"})

@bp.post("/login")
def login():
    data = request.get_json() or {}
    user = User.query.filter_by(email=data.get("email")).first()
    if not user or not verify_password(user.password_hash, data.get("password","")):
        return jsonify({"error":"invalid credentials"}), 401
    token = create_access_token(identity={"id": user.id, "role": user.role})
    return jsonify({"access_token": token, "user": {"id": user.id, "email": user.email, "role": user.role, "full_name": user.full_name}})

@bp.get("/me")
@jwt_required()
def me():
    return jsonify(get_jwt_identity())
