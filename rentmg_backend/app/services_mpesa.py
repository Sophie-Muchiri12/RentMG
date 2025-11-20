import base64, requests, datetime, os
from flask import current_app

def _access_token():
    key = current_app.config["MPESA_CONSUMER_KEY"]
    secret = current_app.config["MPESA_CONSUMER_SECRET"]
    resp = requests.get(
        "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials",
        auth=(key, secret), timeout=20
    )
    resp.raise_for_status()
    return resp.json()["access_token"]

def stk_push(phone, amount, account_reference, description="Rent"):
    token = _access_token()
    timestamp = datetime.datetime.now().strftime("%Y%m%d%H%M%S")
    shortcode = current_app.config["MPESA_SHORTCODE"]
    passkey = current_app.config["MPESA_PASSKEY"]
    password = base64.b64encode((shortcode+passkey+timestamp).encode()).decode()
    payload = {
        "BusinessShortCode": shortcode,
        "Password": password,
        "Timestamp": timestamp,
        "TransactionType": "CustomerPayBillOnline",
        "Amount": int(amount),
        "PartyA": phone,
        "PartyB": shortcode,
        "PhoneNumber": phone,
        "CallBackURL": current_app.config["MPESA_CALLBACK_URL"],
        "AccountReference": account_reference[:12],
        "TransactionDesc": description[:30]
    }
    headers = {"Authorization": f"Bearer {token}"}
    resp = requests.post("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest",
                         json=payload, headers=headers, timeout=30)
    resp.raise_for_status()
    return resp.json()
