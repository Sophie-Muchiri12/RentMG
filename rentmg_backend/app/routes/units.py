from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Unit, Property

bp = Blueprint("units", __name__)


def _serialize_unit(u: Unit):
    return {
        "id": u.id,
        "code": u.code,
        "rent_amount": u.rent_amount,
        "property_id": u.property_id,
        "created_at": u.created_at.isoformat(timespec="seconds") if u.created_at else None,
        "updated_at": u.updated_at.isoformat(timespec="seconds") if u.updated_at else None,
    }


@bp.get("/by-property/<int:property_id>")
@jwt_required()
def list_units(property_id):
    units = Unit.query.filter_by(property_id=property_id).all()
    return jsonify([_serialize_unit(u) for u in units])


@bp.get("/<int:unit_id>")
@jwt_required()
def get_unit(unit_id):
    unit = Unit.query.get_or_404(unit_id)
    return jsonify(_serialize_unit(unit))


@bp.post("/")
@jwt_required()
def create_unit():
    data = request.get_json() or {}
    u = Unit(code=data["code"], rent_amount=int(data["rent_amount"]), property_id=data["property_id"])
    db.session.add(u); db.session.commit()
    return jsonify(_serialize_unit(u)), 201
