from flask import Blueprint, jsonify

bp = Blueprint("root", __name__)


@bp.get("/")
def index():
    return jsonify(
        {
            "message": "RentMG backend is running",
            "docs": [
                "/api/auth/register",
                "/api/auth/login",
                "/api/auth/me",
            ],
        }
    )


@bp.get("/health")
def health():
    return jsonify({"status": "ok"})


@bp.get("/docs")
def docs():
    """Simple docs hint for the Flask server."""
    return jsonify(
        {
            "message": "Swagger UI is not enabled on Flask. Use the FastAPI server for interactive docs.",
            "fastapi_docs": "/docs (when running uvicorn rentmg_backend.fastapi_app:app --reload)",
            "api_base": "/api",
            "endpoints": [
                "/api/auth/register",
                "/api/auth/login",
                "/api/auth/me",
                "/api/properties/",
                "/api/units/",
                "/api/leases/",
                "/api/payments/",
                "/api/issues/",
            ],
        }
    )


@bp.get("/favicon.ico")
def favicon():
    # Avoid noisy 404s in logs for browsers requesting /favicon.ico
    return "", 204
