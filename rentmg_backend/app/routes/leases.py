from datetime import datetime
from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Lease

bp = Blueprint("leases", __name__)


def _serialize_lease(l: Lease):
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


@bp.get("/")
@jwt_required()
def list_leases():
    ident = get_jwt_identity()
    # Landlord sees all; tenant sees own
    q = Lease.query
    if ident["role"] == "tenant":
        q = q.filter_by(tenant_id=ident["id"])
    items = [_serialize_lease(l) for l in q.all()]
    return jsonify(items)


@bp.post("/")
@jwt_required()
def create_lease():
    ident = get_jwt_identity()
    if ident["role"] != "landlord":
        return jsonify({"error":"only landlord"}), 403
    data = request.get_json() or {}
    start_date = data.get("start_date")
    end_date = data.get("end_date")
    l = Lease(
        unit_id=data["unit_id"],
        tenant_id=data["tenant_id"],
        status=data.get("status","active"),
        start_date=datetime.fromisoformat(start_date).date() if start_date else None,
        end_date=datetime.fromisoformat(end_date).date() if end_date else None,
    )
    db.session.add(l); db.session.commit()
    return jsonify(_serialize_lease(l)), 201
