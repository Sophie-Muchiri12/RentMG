"""add property_id to users

Revision ID: 4c2b0e4c4dcb
Revises: 2ebb03db23bb
Create Date: 2025-11-23 00:00:00.000000

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = "4c2b0e4c4dcb"
down_revision = "2ebb03db23bb"
branch_labels = None
depends_on = None


def upgrade():
    op.add_column("users", sa.Column("property_id", sa.Integer(), nullable=True))
    op.create_foreign_key("fk_users_property", "users", "properties", ["property_id"], ["id"])


def downgrade():
    op.drop_constraint("fk_users_property", "users", type_="foreignkey")
    op.drop_column("users", "property_id")
