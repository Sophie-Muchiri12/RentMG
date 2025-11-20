from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..extensions import db
from ..models import Payment, Lease, Unit
from ..services_mpesa import stk_push

bp = Blueprint("payments", __name__)

@bp.post("/mpesa/initiate")
@jwt_required()
def mpesa_initiate():
    ident = get_jwt_identity()
    data = request.get_json() or {}
    lease_id = data.get("lease_id"); amount = data.get("amount"); phone = data.get("phone")
    if not lease_id or not amount or not phone:
        return jsonify({"error":"lease_id, amount, phone required"}), 400
    lease = Lease.query.get_or_404(lease_id)
    # Create payment record
    p = Payment(lease_id=lease_id, method="mpesa", amount=int(amount), status="initiated")
    db.session.add(p); db.session.commit()
    mpesa_resp = stk_push(phone=str(phone), amount=int(amount), account_reference=f"LEASE{lease_id}")
    p.mpesa_checkout_id = mpesa_resp.get("CheckoutRequestID")
    db.session.commit()
    return jsonify({"payment_id": p.id, "mpesa": mpesa_resp})

@bp.post("/mpesa/callback")
def mpesa_callback():
    data = request.get_json() or {}
    # Minimal example to mark as success based on ResultCode
    result_code = data.get("Body",{}).get("stkCallback",{}).get("ResultCode")
    checkout_id = data.get("Body",{}).get("stkCallback",{}).get("CheckoutRequestID")
    if checkout_id:
        p = Payment.query.filter_by(mpesa_checkout_id=checkout_id).first()
        if p:
            p.status = "success" if result_code == 0 else "failed"
            if result_code == 0:
                p.reference = next((i["Value"] for i in data["Body"]["stkCallback"]["CallbackMetadata"]["Item"] if i["Name"]=="MpesaReceiptNumber"), None)
            db.session.commit()
    return jsonify({"ResultCode":0, "ResultDesc":"Accepted"})


@bp.get("/<int:payment_id>")
@jwt_required()
def get_payment(payment_id):
    p = Payment.query.get_or_404(payment_id)
    return jsonify({
        "id": p.id,
        "lease_id": p.lease_id,
        "method": p.method,
        "amount": p.amount,
        "status": p.status,
        "reference": p.reference,
        "mpesa_checkout_id": p.mpesa_checkout_id,
        "created_at": p.created_at.isoformat()
    })

@bp.get("/history")
@jwt_required()
def payment_history():
    lease_id = request.args.get("lease_id", type=int)
    q = Payment.query
    if lease_id:
        q = q.filter_by(lease_id=lease_id)
    items = [{
        "id": p.id,
        "lease_id": p.lease_id,
        "method": p.method,
        "amount": p.amount,
        "status": p.status,
        "reference": p.reference,
        "created_at": p.created_at.isoformat()
    } for p in q.order_by(Payment.created_at.desc()).all()]
    return jsonify(items)
