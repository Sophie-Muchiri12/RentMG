from django.core.management.base import BaseCommand
from datetime import date
from app.models import User, Property, Unit, Tenancy, Payment

class Command(BaseCommand):
    help = "Seeds the database with sample users, properties, units, tenancies, and payments"

    def handle(self, *args, **options):
        self.stdout.write("Seeding data...")

        # Clear old data (optional)
        Payment.objects.all().delete()
        Tenancy.objects.all().delete()
        Unit.objects.all().delete()
        Property.objects.all().delete()
        User.objects.exclude(is_superuser=True).delete()

        # --- USERS ---
        landlord = User.objects.create_user(
            username="landlord_joe",
            email="joe@propertyhub.com",
            password="Landlord123!",
            first_name="Joe",
            last_name="Kamau",
            phone_number="+254712345678",
            user_type="landlord"
        )

        manager = User.objects.create_user(
            username="manager_ann",
            email="ann@propertyhub.com",
            password="Manager123!",
            first_name="Ann",
            last_name="Wanjiru",
            phone_number="+254733987654",
            user_type="property_manager"
        )

        tenant1 = User.objects.create_user(
            username="tenant_mary",
            email="mary@tenant.com",
            password="Tenant123!",
            first_name="Mary",
            last_name="Otieno",
            phone_number="+254711000111",
            user_type="tenant"
        )

        tenant2 = User.objects.create_user(
            username="tenant_peter",
            email="peter@tenant.com",
            password="Tenant123!",
            first_name="Peter",
            last_name="Mwangi",
            phone_number="+254722000222",
            user_type="tenant"
        )

        # --- PROPERTIES ---
        property1 = Property.objects.create(
            owner=landlord,
            name="Sunset Apartments",
            property_type="apartment",
            address="123 Mombasa Road",
            city="Nairobi",
            total_units=3,
            description="Modern 3-storey apartment with secure parking."
        )

        property2 = Property.objects.create(
            owner=manager,
            name="Greenfield Offices",
            property_type="commercial",
            address="7 Riverside Drive",
            city="Nairobi",
            total_units=2,
            description="Office complex with 24/7 power backup."
        )

        # --- UNITS ---
        unit1 = Unit.objects.create(property=property1, unit_number="A1", floor_number=1, bedrooms=2, bathrooms=1, rent_amount=45000)
        unit2 = Unit.objects.create(property=property1, unit_number="A2", floor_number=2, bedrooms=3, bathrooms=2, rent_amount=55000)
        unit3 = Unit.objects.create(property=property1, unit_number="A3", floor_number=3, bedrooms=1, bathrooms=1, rent_amount=35000)
        office1 = Unit.objects.create(property=property2, unit_number="101", floor_number=1, bedrooms=0, bathrooms=1, rent_amount=80000)
        office2 = Unit.objects.create(property=property2, unit_number="102", floor_number=2, bedrooms=0, bathrooms=1, rent_amount=85000)

        # --- TENANCIES ---
        tenancy1 = Tenancy.objects.create(
            tenant=tenant1,
            unit=unit1,
            start_date=date(2024, 6, 1),
            rent_amount=unit1.rent_amount,
            deposit_amount=90000,
            rent_due_day=5,
            status='active'
        )

        tenancy2 = Tenancy.objects.create(
            tenant=tenant2,
            unit=office1,
            start_date=date(2024, 4, 1),
            rent_amount=office1.rent_amount,
            deposit_amount=160000,
            rent_due_day=1,
            status='active'
        )

        # --- PAYMENTS ---
        Payment.objects.create(
            tenancy=tenancy1,
            amount=45000,
            payment_date=date(2024, 11, 1),
            payment_method='mpesa',
            transaction_ref='MPESA12345',
            status='completed',
            month_year='2024-11'
        )

        Payment.objects.create(
            tenancy=tenancy2,
            amount=80000,
            payment_date=date(2024, 10, 30),
            payment_method='bank',
            transaction_ref='BANK98765',
            status='completed',
            month_year='2024-10'
        )

        self.stdout.write(self.style.SUCCESS("✅ Database seeded successfully!"))
