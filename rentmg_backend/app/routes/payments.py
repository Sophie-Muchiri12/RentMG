from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Payment, Lease, Unit, Property
from ..services_mpesa import stk_push

bp = Blueprint("payments", __name__)


def _serialize_payment(p: Payment):
    return {
        "id": p.id,
        "lease_id": p.lease_id,
        "method": p.method,
        "amount": p.amount,
        "status": p.status,
        "reference": p.reference,
        "mpesa_checkout_id": p.mpesa_checkout_id,
        "created_at": p.created_at.isoformat(timespec="seconds") if p.created_at else None,
        "updated_at": p.updated_at.isoformat(timespec="seconds") if p.updated_at else None,
    }


@bp.post("/mpesa/initiate")
@jwt_required()
def mpesa_initiate():
    ident = get_jwt_identity()
    data = request.get_json() or {}
    lease_id = data.get("lease_id"); amount = data.get("amount"); phone = data.get("phone")
    if not lease_id or not amount or not phone:
        return jsonify({"error":"lease_id, amount, phone required"}), 400
    lease = Lease.query.get_or_404(lease_id)
    if ident["role"] == "tenant" and lease.tenant_id != ident["id"]:
        return jsonify({"error": "not allowed to pay this lease"}), 403
    # Create payment record
    p = Payment(lease_id=lease_id, method="mpesa", amount=int(amount), status="pending")
    db.session.add(p); db.session.commit()
    try:
        mpesa_resp = stk_push(phone=str(phone), amount=int(amount), account_reference=f"LEASE{lease_id}")
    except Exception as exc:
        p.status = "failed"
        db.session.commit()
        return jsonify({"error": "failed to initiate mpesa", "detail": str(exc)}), 502
    p.mpesa_checkout_id = mpesa_resp.get("CheckoutRequestID")
    db.session.commit()
    return jsonify({"message": "Payment initiated", "payment_id": p.id, "mpesa_checkout_id": p.mpesa_checkout_id})


@bp.post("/mpesa/callback")
def mpesa_callback():
    data = request.get_json() or {}
    # Minimal example to mark as success based on ResultCode
    result_code = data.get("Body",{}).get("stkCallback",{}).get("ResultCode")
    checkout_id = data.get("Body",{}).get("stkCallback",{}).get("CheckoutRequestID")
    if checkout_id:
        p = Payment.query.filter_by(mpesa_checkout_id=checkout_id).first()
        if p:
            p.status = "completed" if result_code == 0 else "failed"
            if result_code == 0:
                p.reference = next((i["Value"] for i in data["Body"]["stkCallback"]["CallbackMetadata"]["Item"] if i["Name"]=="MpesaReceiptNumber"), None)
            db.session.commit()
    return jsonify({"ResultCode":0, "ResultDesc":"Accepted"})


@bp.get("/<int:payment_id>")
@jwt_required()
def get_payment(payment_id):
    ident = get_jwt_identity()
    q = Payment.query.join(Lease, Payment.lease_id == Lease.id)
    if ident["role"] == "tenant":
        q = q.filter(Lease.tenant_id == ident["id"])
    elif ident["role"] == "landlord":
        q = q.join(Unit, Lease.unit_id == Unit.id).join(Property, Unit.property_id == Property.id).filter(Property.landlord_id == ident["id"])
    p = q.filter(Payment.id == payment_id).first_or_404()
    return jsonify(_serialize_payment(p))


@bp.get("/history")
@jwt_required()
def payment_history():
    ident = get_jwt_identity()
    lease_id = request.args.get("lease_id", type=int)
    q = Payment.query.join(Lease, Payment.lease_id == Lease.id)
    if ident["role"] == "tenant":
        q = q.filter(Lease.tenant_id == ident["id"])
    elif ident["role"] == "landlord":
        q = q.join(Unit, Lease.unit_id == Unit.id).join(Property, Unit.property_id == Property.id).filter(Property.landlord_id == ident["id"])
    if lease_id:
        q = q.filter(Payment.lease_id == lease_id)
    items = [_serialize_payment(p) for p in q.order_by(Payment.created_at.desc()).all()]
    return jsonify(items)
