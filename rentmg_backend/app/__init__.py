from flask import Flask
from .extensions import db, migrate, jwt, cors
from .config import Config
from .routes import register_routes

def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)
    cors.init_app(app, resources={r"/api/*": {"origins": app.config.get("CORS_ORIGINS", "*").split(",")}})
    db.init_app(app)
    migrate.init_app(app, db)
    jwt.init_app(app)
    register_routes(app)
    return app
