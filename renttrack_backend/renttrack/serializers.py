from rest_framework import serializers
from django.contrib.auth.password_validation import validate_password
from django.core.exceptions import ValidationError
from .models import User, Property, Unit, Tenancy, Payment


class UserRegistrationSerializer(serializers.ModelSerializer):
    password = serializers.CharField(
        write_only=True, 
        required=True, 
        validators=[validate_password],
        style={'input_type': 'password'}
    )
    password_confirm = serializers.CharField(
        write_only=True, 
        required=True,
        style={'input_type': 'password'}
    )
    
    class Meta:
        model = User
        fields = (
            'id', 'username', 'email', 'password', 'password_confirm',
            'first_name', 'last_name', 'phone_number', 'user_type'
        )
        extra_kwargs = {
            'first_name': {'required': True},
            'last_name': {'required': True},
            'email': {'required': True},
            'user_type': {'required': True}
        }
    
    def validate(self, attrs):
        if attrs['password'] != attrs['password_confirm']:
            raise serializers.ValidationError({"password": "Password fields didn't match."})
        return attrs
    
    def validate_email(self, value):
        if User.objects.filter(email=value).exists():
            raise serializers.ValidationError("A user with this email already exists.")
        return value
    
    def validate_username(self, value):
        if User.objects.filter(username=value).exists():
            raise serializers.ValidationError("A user with this username already exists.")
        return value
    
    def create(self, validated_data):
        validated_data.pop('password_confirm')
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            first_name=validated_data['first_name'],
            last_name=validated_data['last_name'],
            phone_number=validated_data.get('phone_number', ''),
            user_type=validated_data['user_type'],
            password=validated_data['password']
        )
        return user


class UserLoginSerializer(serializers.Serializer):
    email = serializers.EmailField(required=True)
    password = serializers.CharField(
        required=True,
        write_only=True,
        style={'input_type': 'password'}
    )


class UserSerializer(serializers.ModelSerializer):
    full_name = serializers.SerializerMethodField()
    
    class Meta:
        model = User
        fields = (
            'id', 'username', 'email', 'first_name', 'last_name',
            'full_name', 'phone_number', 'user_type', 'date_joined'
        )
        read_only_fields = ('id', 'date_joined')
    
    def get_full_name(self, obj):
        return obj.get_full_name()


class UnitSerializer(serializers.ModelSerializer):
    current_tenant = serializers.SerializerMethodField()
    
    class Meta:
        model = Unit
        fields = (
            'id', 'unit_number', 'floor_number', 'bedrooms', 'bathrooms',
            'rent_amount', 'is_occupied', 'description', 'current_tenant',
            'created_at', 'updated_at'
        )
        read_only_fields = ('id', 'created_at', 'updated_at')
    
    def get_current_tenant(self, obj):
        active_tenancy = obj.tenancies.filter(status='active').first()
        if active_tenancy:
            return {
                'id': active_tenancy.tenant.id,
                'name': active_tenancy.tenant.get_full_name(),
                'email': active_tenancy.tenant.email,
                'phone': active_tenancy.tenant.phone_number
            }
        return None


class PropertySerializer(serializers.ModelSerializer):
    owner_name = serializers.CharField(source='owner.get_full_name', read_only=True)
    units = UnitSerializer(many=True, read_only=True)
    occupied_units = serializers.ReadOnlyField()
    vacant_units = serializers.ReadOnlyField()
    
    class Meta:
        model = Property
        fields = (
            'id', 'owner', 'owner_name', 'name', 'property_type',
            'address', 'city', 'country', 'total_units', 'description',
            'occupied_units', 'vacant_units', 'units',
            'created_at', 'updated_at'
        )
        read_only_fields = ('id', 'created_at', 'updated_at')
    
    def validate_owner(self, value):
        if value.user_type not in ['landlord', 'property_manager']:
            raise serializers.ValidationError(
                "Only landlords and property managers can own properties."
            )
        return value


class PropertyCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Property
        fields = (
            'id', 'name', 'property_type', 'address', 'city',
            'country', 'total_units', 'description'
        )
        read_only_fields = ('id',)


class TenancySerializer(serializers.ModelSerializer):
    tenant_name = serializers.CharField(source='tenant.get_full_name', read_only=True)
    tenant_email = serializers.CharField(source='tenant.email', read_only=True)
    tenant_phone = serializers.CharField(source='tenant.phone_number', read_only=True)
    unit_number = serializers.CharField(source='unit.unit_number', read_only=True)
    property_name = serializers.CharField(source='unit.property.name', read_only=True)
    
    class Meta:
        model = Tenancy
        fields = (
            'id', 'tenant', 'tenant_name', 'tenant_email', 'tenant_phone',
            'unit', 'unit_number', 'property_name', 'start_date', 'end_date',
            'rent_amount', 'deposit_amount', 'rent_due_day', 'status',
            'notes', 'created_at', 'updated_at'
        )
        read_only_fields = ('id', 'created_at', 'updated_at')
    
    def validate_tenant(self, value):
        if value.user_type != 'tenant':
            raise serializers.ValidationError("Selected user is not a tenant.")
        return value
    
    def validate(self, attrs):
        # Check if unit is already occupied
        unit = attrs.get('unit')
        if unit and unit.is_occupied:
            # Check if this is an update or new tenancy
            if not self.instance:
                raise serializers.ValidationError(
                    {"unit": "This unit is already occupied."}
                )
        return attrs


class TenantRegistrationSerializer(serializers.ModelSerializer):
    """Simplified serializer for registering tenants for a specific unit"""
    password = serializers.CharField(
        write_only=True, 
        required=True, 
        validators=[validate_password]
    )
    password_confirm = serializers.CharField(write_only=True, required=True)
    unit_id = serializers.IntegerField(write_only=True, required=True)
    start_date = serializers.DateField(required=True)
    deposit_amount = serializers.DecimalField(max_digits=10, decimal_places=2, required=False, default=0)
    
    class Meta:
        model = User
        fields = (
            'id', 'username', 'email', 'password', 'password_confirm',
            'first_name', 'last_name', 'phone_number',
            'unit_id', 'start_date', 'deposit_amount'
        )
        extra_kwargs = {
            'first_name': {'required': True},
            'last_name': {'required': True},
            'email': {'required': True}
        }
    
    def validate(self, attrs):
        if attrs['password'] != attrs['password_confirm']:
            raise serializers.ValidationError({"password": "Password fields didn't match."})
        
        # Validate unit exists and is not occupied
        try:
            unit = Unit.objects.get(id=attrs['unit_id'])
            if unit.is_occupied:
                raise serializers.ValidationError({"unit_id": "This unit is already occupied."})
        except Unit.DoesNotExist:
            raise serializers.ValidationError({"unit_id": "Unit not found."})
        
        return attrs
    
    def create(self, validated_data):
        unit_id = validated_data.pop('unit_id')
        start_date = validated_data.pop('start_date')
        deposit_amount = validated_data.pop('deposit_amount', 0)
        validated_data.pop('password_confirm')
        
        # Create tenant user
        tenant = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            first_name=validated_data['first_name'],
            last_name=validated_data['last_name'],
            phone_number=validated_data.get('phone_number', ''),
            user_type='tenant',
            password=validated_data['password']
        )
        
        # Create tenancy
        unit = Unit.objects.get(id=unit_id)
        Tenancy.objects.create(
            tenant=tenant,
            unit=unit,
            start_date=start_date,
            rent_amount=unit.rent_amount,
            deposit_amount=deposit_amount,
            status='active'
        )
        
        return tenant


class PaymentSerializer(serializers.ModelSerializer):
    tenant_name = serializers.CharField(source='tenancy.tenant.get_full_name', read_only=True)
    unit_number = serializers.CharField(source='tenancy.unit.unit_number', read_only=True)
    property_name = serializers.CharField(source='tenancy.unit.property.name', read_only=True)
    
    class Meta:
        model = Payment
        fields = (
            'id', 'tenancy', 'tenant_name', 'unit_number', 'property_name',
            'amount', 'payment_date', 'payment_method', 'transaction_ref',
            'status', 'month_year', 'notes', 'created_at', 'updated_at'
        )
        read_only_fields = ('id', 'created_at', 'updated_at')
    
    def validate_transaction_ref(self, value):
        if self.instance:
            # Update case - exclude current instance from uniqueness check
            if Payment.objects.exclude(pk=self.instance.pk).filter(transaction_ref=value).exists():
                raise serializers.ValidationError("A payment with this transaction reference already exists.")
        else:
            # Create case
            if Payment.objects.filter(transaction_ref=value).exists():
                raise serializers.ValidationError("A payment with this transaction reference already exists.")
        return value