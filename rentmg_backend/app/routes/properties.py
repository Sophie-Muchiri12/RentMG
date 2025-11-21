from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Property

bp = Blueprint("properties", __name__)


def _serialize_property(p: Property):
    return {
        "id": p.id,
        "name": p.name,
        "address": p.address,
        "landlord_id": p.landlord_id,
        "created_at": p.created_at.isoformat(timespec="seconds") if p.created_at else None,
        "updated_at": p.updated_at.isoformat(timespec="seconds") if p.updated_at else None,
    }


@bp.get("/")
@jwt_required()
def list_properties():
    ident = get_jwt_identity()
    q = Property.query
    if ident["role"] == "landlord":
        q = q.filter_by(landlord_id=ident["id"])
    items = [_serialize_property(p) for p in q.all()]
    return jsonify(items)


@bp.get("/<int:property_id>")
@jwt_required()
def get_property(property_id):
    prop = Property.query.get_or_404(property_id)
    return jsonify(_serialize_property(prop))


@bp.post("/")
@jwt_required()
def create_property():
    ident = get_jwt_identity()
    if ident["role"] != "landlord": return jsonify({"error":"only landlord"}), 403
    data = request.get_json() or {}
    p = Property(name=data.get("name"), address=data.get("address"), landlord_id=ident["id"])
    db.session.add(p); db.session.commit()
    return jsonify(_serialize_property(p)), 201
