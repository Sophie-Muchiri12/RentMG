from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Unit, Property

bp = Blueprint("units", __name__)

@bp.get("/by-property/<int:property_id>")
@jwt_required()
def list_units(property_id):
    units = Unit.query.filter_by(property_id=property_id).all()
    return jsonify([{"id":u.id, "code":u.code, "rent_amount":u.rent_amount} for u in units])

@bp.post("/")
@jwt_required()
def create_unit():
    data = request.get_json() or {}
    u = Unit(code=data["code"], rent_amount=data["rent_amount"], property_id=data["property_id"])
    db.session.add(u); db.session.commit()
    return jsonify({"id": u.id}), 201
