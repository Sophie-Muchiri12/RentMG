from datetime import datetime, timedelta
from app import create_app
from app.extensions import db
from app.models import User, Property, Unit, Lease, Payment, Issue
from app.utils import hash_password
import random

app = create_app()

def clear_data():
    with app.app_context():
        print("Clearing existing data...")
        Issue.query.delete()
        Payment.query.delete()
        Lease.query.delete()
        Unit.query.delete()
        Property.query.delete()
        User.query.delete()
        db.session.commit()
        print("Data cleared.")

def seed_data():
    with app.app_context():
        print("Seeding data...")

        landlords = []
        landlord_names = [
            "John Smith", "Sarah Johnson", "Michael Brown", "Emily Davis",
            "David Wilson", "Lisa Anderson", "Robert Taylor", "Jennifer Martinez",
            "James Garcia", "Mary Rodriguez"
        ]

        print("Creating landlords...")
        for i, name in enumerate(landlord_names, 1):
            email = name.lower().replace(" ", ".") + "@landlord.com"
            landlord = User(
                email=email,
                password_hash=hash_password("password123"),
                role="landlord",
                full_name=name
            )
            db.session.add(landlord)
            landlords.append(landlord)

        db.session.commit()
        print(f"Created {len(landlords)} landlords")

        tenants = []
        tenant_names = [
            "Alice Cooper", "Bob Williams", "Carol Martinez", "Daniel Lee",
            "Emma Thomas", "Frank Moore", "Grace Jackson", "Henry White",
            "Iris Harris", "Jack Thompson"
        ]

        print("Creating tenants...")
        for i, name in enumerate(tenant_names, 1):
            email = name.lower().replace(" ", ".") + "@tenant.com"
            tenant = User(
                email=email,
                password_hash=hash_password("password123"),
                role="tenant",
                full_name=name
            )
            db.session.add(tenant)
            tenants.append(tenant)

        db.session.commit()
        print(f"Created {len(tenants)} tenants")

        properties = []
        property_data = [
            ("Sunset Apartments", "123 Main Street, Nairobi"),
            ("Green Valley Complex", "456 Oak Avenue, Mombasa"),
            ("Palm Heights", "789 Beach Road, Nakuru"),
            ("Riverside Residences", "321 River Drive, Kisumu"),
            ("Mountain View Estate", "654 Hill Street, Eldoret"),
            ("Urban Towers", "987 City Center, Nairobi"),
            ("Coastal Villas", "147 Ocean Boulevard, Mombasa"),
            ("Garden Apartments", "258 Park Lane, Thika"),
            ("Lakeside Homes", "369 Lake View, Kisumu"),
            ("Skyline Plaza", "741 High Street, Nairobi"),
            ("Harbor Point", "852 Port Road, Mombasa"),
            ("Valley View", "963 Valley Drive, Nakuru")
        ]

        print("Creating properties...")
        for i, (name, address) in enumerate(property_data):
            landlord = landlords[i % len(landlords)]
            prop = Property(
                name=name,
                address=address,
                landlord_id=landlord.id
            )
            db.session.add(prop)
            properties.append(prop)

        db.session.commit()
        print(f"Created {len(properties)} properties")

        units = []
        print("Creating units...")
        for prop in properties:
            num_units = random.randint(3, 6)
            base_rent = random.choice([15000, 20000, 25000, 30000, 35000, 40000])

            for unit_num in range(1, num_units + 1):
                code = f"{chr(65 + (unit_num - 1) // 10)}{unit_num:02d}"
                rent_variation = random.randint(-2000, 3000)
                unit = Unit(
                    code=code,
                    rent_amount=base_rent + rent_variation,
                    property_id=prop.id
                )
                db.session.add(unit)
                units.append(unit)

        db.session.commit()
        print(f"Created {len(units)} units")

        leases = []
        available_tenants = tenants.copy()
        random.shuffle(units)

        print("Creating leases...")
        for i, unit in enumerate(units[:len(tenants)]):
            tenant = available_tenants[i]

            months_ago = random.randint(6, 24)
            start_date = datetime.now().date() - timedelta(days=months_ago * 30)

            is_active = random.random() > 0.2
            if is_active:
                end_date = None
                status = "active"
            else:
                end_months = random.randint(1, months_ago - 1)
                end_date = start_date + timedelta(days=end_months * 30)
                status = "ended"

            lease = Lease(
                unit_id=unit.id,
                tenant_id=tenant.id,
                start_date=start_date,
                end_date=end_date,
                status=status
            )
            db.session.add(lease)
            leases.append(lease)

        db.session.commit()
        print(f"Created {len(leases)} leases")

        payments = []
        print("Creating payment history...")
        for lease in leases:
            unit = Unit.query.get(lease.unit_id)
            monthly_rent = unit.rent_amount

            start = lease.start_date
            end = lease.end_date if lease.end_date else datetime.now().date()

            current_date = start
            while current_date <= end:
                payment_status = random.choices(
                    ["completed", "completed", "completed", "pending", "failed"],
                    weights=[40, 30, 20, 7, 3]
                )[0]

                method = random.choice(["mpesa", "mpesa", "bank"])

                amount = monthly_rent
                if payment_status == "completed":
                    amount_variation = random.choice([0, 0, 0, random.randint(-1000, 1000)])
                    amount += amount_variation

                reference = f"REF{random.randint(100000, 999999)}" if payment_status == "completed" else None
                mpesa_id = f"MPX{random.randint(1000000000, 9999999999)}" if method == "mpesa" and payment_status == "completed" else None

                payment = Payment(
                    lease_id=lease.id,
                    method=method,
                    amount=amount,
                    status=payment_status,
                    reference=reference,
                    mpesa_checkout_id=mpesa_id
                )
                payment.created_at = datetime.combine(current_date, datetime.min.time())
                db.session.add(payment)
                payments.append(payment)

                current_date = current_date + timedelta(days=30)

        db.session.commit()
        print(f"Created {len(payments)} payment records")

        issues = []
        issue_templates = [
            ("Leaking Faucet", "The kitchen faucet is leaking and needs repair", "normal"),
            ("Broken Window", "Bedroom window is cracked and needs replacement", "high"),
            ("AC Not Working", "Air conditioning unit is not cooling properly", "high"),
            ("Door Lock Issue", "Front door lock is jammed", "high"),
            ("Water Heater Problem", "Hot water not working in bathroom", "normal"),
            ("Electrical Outlet", "Living room outlet is not working", "normal"),
            ("Pest Control Needed", "Noticed some pests in the kitchen area", "normal"),
            ("Plumbing Issue", "Bathroom sink is clogged", "normal"),
            ("Paint Peeling", "Paint is peeling off the walls in bedroom", "low"),
            ("Light Fixture", "Ceiling light not working in hallway", "normal"),
            ("Noise Complaint", "Excessive noise from neighbors", "low"),
            ("Parking Issue", "Assigned parking spot being used by others", "low"),
            ("Garbage Disposal", "Kitchen garbage disposal is broken", "normal"),
            ("Carpet Stain", "Large stain on living room carpet", "low"),
            ("Heating Problem", "Heater not working properly", "high")
        ]

        print("Creating maintenance issues...")
        for lease in leases:
            if lease.status == "active":
                num_issues = random.randint(0, 4)

                for _ in range(num_issues):
                    title, description, priority = random.choice(issue_templates)

                    days_ago = random.randint(1, 180)
                    created_date = datetime.now() - timedelta(days=days_ago)

                    status = random.choices(
                        ["open", "in_progress", "resolved", "closed"],
                        weights=[30, 25, 25, 20]
                    )[0]

                    unit = Unit.query.get(lease.unit_id)

                    issue = Issue(
                        title=title,
                        description=description,
                        status=status,
                        priority=priority,
                        reporter_id=lease.tenant_id,
                        property_id=unit.property_id,
                        unit_id=unit.id
                    )
                    issue.created_at = created_date
                    db.session.add(issue)
                    issues.append(issue)

        db.session.commit()
        print(f"Created {len(issues)} maintenance issues")

        print("\n" + "="*60)
        print("SEED DATA SUMMARY")
        print("="*60)
        print(f"Landlords: {len(landlords)}")
        print(f"Tenants: {len(tenants)}")
        print(f"Properties: {len(properties)}")
        print(f"Units: {len(units)}")
        print(f"Leases: {len([l for l in leases if l.status == 'active'])} active, {len([l for l in leases if l.status == 'ended'])} ended")
        print(f"Payments: {len(payments)}")
        print(f"Issues: {len(issues)}")
        print("="*60)
        print("\nLOGIN CREDENTIALS (all passwords: password123)")
        print("="*60)
        print("\nLANDLORDS:")
        for landlord in landlords[:5]:
            print(f"  {landlord.email}")
        print(f"  ... and {len(landlords) - 5} more")

        print("\nTENANTS:")
        for tenant in tenants[:5]:
            print(f"  {tenant.email}")
        print(f"  ... and {len(tenants) - 5} more")
        print("="*60)

if __name__ == "__main__":
    print("RentMG Seed Data Script")
    print("="*60)
    clear_data()
    seed_data()
    print("\nSeeding completed successfully!")
