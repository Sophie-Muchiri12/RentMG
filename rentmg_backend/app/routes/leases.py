from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Lease

bp = Blueprint("leases", __name__)

@bp.get("/")
@jwt_required()
def list_leases():
    ident = get_jwt_identity()
    # Landlord sees all; tenant sees own
    q = Lease.query
    if ident["role"] == "tenant":
        q = q.filter_by(tenant_id=ident["id"])
    items = [{
        "id": l.id,
        "unit_id": l.unit_id,
        "tenant_id": l.tenant_id,
        "start_date": l.start_date.isoformat() if l.start_date else None,
        "end_date": l.end_date.isoformat() if l.end_date else None,
        "status": l.status
    } for l in q.all()]
    return jsonify(items)

@bp.post("/")
@jwt_required()
def create_lease():
    data = request.get_json() or {}
    l = Lease(unit_id=data["unit_id"], tenant_id=data["tenant_id"], status=data.get("status","active"))
    db.session.add(l); db.session.commit()
    return jsonify({"id": l.id}), 201
