from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Issue

bp = Blueprint("issues", __name__)

@bp.get("/")
@jwt_required()
def list_issues():
    ident = get_jwt_identity()
    q = Issue.query
    if ident["role"] == "tenant":
        q = q.filter_by(reporter_id=ident["id"])
    items = [{"id":i.id, "title":i.title, "status":i.status, "priority":i.priority} for i in q.order_by(Issue.created_at.desc()).all()]
    return jsonify(items)

@bp.post("/")
@jwt_required()
def create_issue():
    ident = get_jwt_identity()
    data = request.get_json() or {}
    i = Issue(title=data["title"], description=data.get("description"), reporter_id=ident["id"],
              priority=data.get("priority","normal"), property_id=data.get("property_id"), unit_id=data.get("unit_id"))
    db.session.add(i); db.session.commit()
    return jsonify({"id": i.id}), 201

@bp.patch("/<int:issue_id>")
@jwt_required()
def update_issue(issue_id):
    data = request.get_json() or {}
    i = Issue.query.get_or_404(issue_id)
    for k in ["title","description","status","priority","assignee_id"]:
        if k in data: setattr(i, k, data[k])
    db.session.commit()
    return jsonify({"message":"updated"})
