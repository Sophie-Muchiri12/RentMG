from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Issue

bp = Blueprint("issues", __name__)


def _serialize_issue(i: Issue):
    return {
        "id": i.id,
        "title": i.title,
        "description": i.description,
        "status": i.status,
        "priority": i.priority,
        "reporter_id": i.reporter_id,
        "assignee_id": i.assignee_id,
        "property_id": i.property_id,
        "unit_id": i.unit_id,
        "created_at": i.created_at.isoformat(timespec="seconds") if i.created_at else None,
        "updated_at": i.updated_at.isoformat(timespec="seconds") if i.updated_at else None,
    }


@bp.get("/")
@jwt_required()
def list_issues():
    ident = get_jwt_identity()
    q = Issue.query
    if ident["role"] == "tenant":
        q = q.filter_by(reporter_id=ident["id"])
    items = [_serialize_issue(i) for i in q.order_by(Issue.created_at.desc()).all()]
    return jsonify(items)


@bp.post("/")
@jwt_required()
def create_issue():
    ident = get_jwt_identity()
    data = request.get_json() or {}
    i = Issue(title=data["title"], description=data.get("description"), reporter_id=ident["id"],
              priority=data.get("priority","normal"), property_id=data.get("property_id"), unit_id=data.get("unit_id"))
    db.session.add(i); db.session.commit()
    return jsonify(_serialize_issue(i)), 201


@bp.patch("/<int:issue_id>")
@jwt_required()
def update_issue(issue_id):
    data = request.get_json() or {}
    i = Issue.query.get_or_404(issue_id)
    for k in ["title","description","status","priority","assignee_id"]:
        if k in data: setattr(i, k, data[k])
    db.session.commit()
    return jsonify({"message":"updated"})
