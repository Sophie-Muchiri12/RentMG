from flask import Blueprint
from .auth import bp as auth_bp
from .properties import bp as properties_bp
from .units import bp as units_bp
from .issues import bp as issues_bp
from .payments import bp as payments_bp
from .leases import bp as leases_bp

def register_routes(app):
    app.register_blueprint(auth_bp, url_prefix="/api/auth")
    app.register_blueprint(properties_bp, url_prefix="/api/properties")
    app.register_blueprint(units_bp, url_prefix="/api/units")
    app.register_blueprint(issues_bp, url_prefix="/api/issues")
    app.register_blueprint(payments_bp, url_prefix="/api/payments")
    app.register_blueprint(leases_bp, url_prefix="/api/leases")
