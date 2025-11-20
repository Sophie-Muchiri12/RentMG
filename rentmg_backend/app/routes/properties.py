from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Property

bp = Blueprint("properties", __name__)

@bp.get("/")
@jwt_required()
def list_properties():
    ident = get_jwt_identity()
    q = Property.query
    if ident["role"] == "landlord":
        q = q.filter_by(landlord_id=ident["id"])
    items = [{"id":p.id, "name":p.name, "address":p.address} for p in q.all()]
    return jsonify(items)

@bp.post("/")
@jwt_required()
def create_property():
    ident = get_jwt_identity()
    if ident["role"] != "landlord": return jsonify({"error":"only landlord"}), 403
    data = request.get_json() or {}
    p = Property(name=data.get("name"), address=data.get("address"), landlord_id=ident["id"])
    db.session.add(p); db.session.commit()
    return jsonify({"id": p.id, "name": p.name, "address": p.address}), 201
