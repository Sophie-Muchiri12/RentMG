from rest_framework import viewsets, status, generics
from rest_framework.decorators import action, api_view, permission_classes
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.authtoken.models import Token
from django.contrib.auth import authenticate
from django.db.models import Q, Count, Sum
from .models import User, Property, Unit, Tenancy, Payment
from .serializers import (
    UserRegistrationSerializer, UserLoginSerializer, UserSerializer,
    PropertySerializer, PropertyCreateSerializer, UnitSerializer,
    TenancySerializer, TenantRegistrationSerializer, PaymentSerializer
)


@api_view(['POST'])
@permission_classes([AllowAny])
def register_user(request):
    """Register a new user (landlord, property manager, or tenant)"""
    serializer = UserRegistrationSerializer(data=request.data)
    if serializer.is_valid():
        user = serializer.save()
        token, created = Token.objects.get_or_create(user=user)
        
        return Response({
            'success': True,
            'message': 'User registered successfully',
            'data': {
                'user': UserSerializer(user).data,
                'token': token.key
            }
        }, status=status.HTTP_201_CREATED)
    
    return Response({
        'success': False,
        'message': 'Registration failed',
        'errors': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


@api_view(['POST'])
@permission_classes([AllowAny])
def login_user(request):
    """Login user and return token"""
    serializer = UserLoginSerializer(data=request.data)
    if serializer.is_valid():
        email = serializer.validated_data['email']
        password = serializer.validated_data['password']
        
        # Get user by email
        try:
            user = User.objects.get(email=email)
        except User.DoesNotExist:
            return Response({
                'success': False,
                'message': 'Invalid credentials'
            }, status=status.HTTP_401_UNAUTHORIZED)
        
        # Authenticate with username (since Django uses username for auth)
        user = authenticate(username=user.username, password=password)
        
        if user:
            token, created = Token.objects.get_or_create(user=user)
            return Response({
                'success': True,
                'message': 'Login successful',
                'data': {
                    'user': UserSerializer(user).data,
                    'token': token.key
                }
            }, status=status.HTTP_200_OK)
        
        return Response({
            'success': False,
            'message': 'Invalid credentials'
        }, status=status.HTTP_401_UNAUTHORIZED)
    
    return Response({
        'success': False,
        'message': 'Invalid data',
        'errors': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def logout_user(request):
    """Logout user by deleting token"""
    try:
        request.user.auth_token.delete()
        return Response({
            'success': True,
            'message': 'Logged out successfully'
        }, status=status.HTTP_200_OK)
    except Exception as e:
        return Response({
            'success': False,
            'message': str(e)
        }, status=status.HTTP_400_BAD_REQUEST)


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def get_user_profile(request):
    """Get current user profile"""
    serializer = UserSerializer(request.user)
    return Response({
        'success': True,
        'data': serializer.data
    }, status=status.HTTP_200_OK)


class PropertyViewSet(viewsets.ModelViewSet):
    """ViewSet for managing properties"""
    permission_classes = [IsAuthenticated]
    
    def get_queryset(self):
        user = self.request.user
        if user.user_type in ['landlord', 'property_manager']:
            return Property.objects.filter(owner=user).prefetch_related('units')
        return Property.objects.none()
    
    def get_serializer_class(self):
        if self.action in ['create', 'update', 'partial_update']:
            return PropertyCreateSerializer
        return PropertySerializer
    
    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if serializer.is_valid():
            property_obj = serializer.save(owner=request.user)
            return Response({
                'success': True,
                'message': 'Property created successfully',
                'data': PropertySerializer(property_obj).data
            }, status=status.HTTP_201_CREATED)
        
        return Response({
            'success': False,
            'message': 'Failed to create property',
            'errors': serializer.errors
        }, status=status.HTTP_400_BAD_REQUEST)
    
    def list(self, request, *args, **kwargs):
        queryset = self.get_queryset()
        serializer = PropertySerializer(queryset, many=True)
        return Response({
            'success': True,
            'data': serializer.data,
            'count': queryset.count()
        }, status=status.HTTP_200_OK)
    
    def retrieve(self, request, *args, **kwargs):
        instance = self.get_object()
        serializer = PropertySerializer(instance)
        return Response({
            'success': True,
            'data': serializer.data
        }, status=status.HTTP_200_OK)
    
    @action(detail=True, methods=['get'])
    def units(self, request, pk=None):
        """Get all units for a property"""
        property_obj = self.get_object()
        units = property_obj.units.all()
        serializer = UnitSerializer(units, many=True)
        return Response({
            'success': True,
            'data': serializer.data,
            'count': units.count()
        }, status=status.HTTP_200_OK)
    
    @action(detail=True, methods=['get'])
    def tenants(self, request, pk=None):
        """Get all active tenants for a property"""
        property_obj = self.get_object()
        tenancies = Tenancy.objects.filter(
            unit__property=property_obj,
            status='active'
        ).select_related('tenant', 'unit')
        serializer = TenancySerializer(tenancies, many=True)
        return Response({
            'success': True,
            'data': serializer.data,
            'count': tenancies.count()
        }, status=status.HTTP_200_OK)
    
    @action(detail=True, methods=['get'])
    def dashboard_stats(self, request, pk=None):
        """Get dashboard statistics for a property"""
        property_obj = self.get_object()
        
        total_units = property_obj.units.count()
        occupied_units = property_obj.units.filter(is_occupied=True).count()
        vacant_units = total_units - occupied_units
        
        # Get payment stats for current month
        from datetime import datetime
        current_month = datetime.now().strftime('%Y-%m')
        
        payments = Payment.objects.filter(
            tenancy__unit__property=property_obj,
            month_year=current_month
        )
        
        total_collected = payments.filter(status='completed').aggregate(
            total=Sum('amount')
        )['total'] or 0
        
        total_expected = property_obj.units.filter(is_occupied=True).aggregate(
            total=Sum('rent_amount')
        )['total'] or 0
        
        return Response({
            'success': True,
            'data': {
                'total_units': total_units,
                'occupied_units': occupied_units,
                'vacant_units': vacant_units,
                'occupancy_rate': round((occupied_units / total_units * 100) if total_units > 0 else 0, 2),
                'total_expected_rent': float(total_expected),
                'total_collected_rent': float(total_collected),
                'collection_rate': round((total_collected / total_expected * 100) if total_expected > 0 else 0, 2)
            }
        }, status=status.HTTP_200_OK)


class UnitViewSet(viewsets.ModelViewSet):
    """ViewSet for managing units"""
    serializer_class = UnitSerializer
    permission_classes = [IsAuthenticated]
    
    def get_queryset(self):
        user = self.request.user
        if user.user_type in ['landlord', 'property_manager']:
            return Unit.objects.filter(property__owner=user).select_related('property')
        return Unit.objects.none()
    
    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if serializer.is_valid():
            # Verify property belongs to user
            property_id = request.data.get('property')
            try:
                property_obj = Property.objects.get(id=property_id, owner=request.user)
            except Property.DoesNotExist:
                return Response({
                    'success': False,
                    'message': 'Property not found or access denied'
                }, status=status.HTTP_404_NOT_FOUND)
            
            unit = serializer.save()
            return Response({
                'success': True,
                'message': 'Unit created successfully',
                'data': serializer.data
            }, status=status.HTTP_201_CREATED)
        
        return Response({
            'success': False,
            'message': 'Failed to create unit',
            'errors': serializer.errors
        }, status=status.HTTP_400_BAD_REQUEST)
    
    def list(self, request, *args, **kwargs):
        property_id = request.query_params.get('property_id')
        queryset = self.get_queryset()
        
        if property_id:
            queryset = queryset.filter(property_id=property_id)
        
        serializer = self.get_serializer(queryset, many=True)
        return Response({
            'success': True,
            'data': serializer.data,
            'count': queryset.count()
        }, status=status.HTTP_200_OK)


class TenancyViewSet(viewsets.ModelViewSet):
    """ViewSet for managing tenancies"""
    serializer_class = TenancySerializer
    permission_classes = [IsAuthenticated]
    
    def get_queryset(self):
        user = self.request.user
        if user.user_type in ['landlord', 'property_manager']:
            return Tenancy.objects.filter(
                unit__property__owner=user
            ).select_related('tenant', 'unit', 'unit__property')
        elif user.user_type == 'tenant':
            return Tenancy.objects.filter(tenant=user).select_related('unit', 'unit__property')
        return Tenancy.objects.none()
    
    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        if serializer.is_valid():
            # Verify unit belongs to user's property
            unit_id = request.data.get('unit')
            try:
                unit = Unit.objects.get(id=unit_id, property__owner=request.user)
            except Unit.DoesNotExist:
                return Response({
                    'success': False,
                    'message': 'Unit not found or access denied'
                }, status=status.HTTP_404_NOT_FOUND)
            
            tenancy = serializer.save()
            return Response({
                'success': True,
                'message': 'Tenancy created successfully',
                'data': TenancySerializer(tenancy).data
            }, status=status.HTTP_201_CREATED)
        
        return Response({
            'success': False,
            'message': 'Failed to create tenancy',
            'errors': serializer.errors
        }, status=status.HTTP_400_BAD_REQUEST)
    
    def list(self, request, *args, **kwargs):
        queryset = self.get_queryset()
        status_filter = request.query_params.get('status')
        property_id = request.query_params.get('property_id')
        
        if status_filter:
            queryset = queryset.filter(status=status_filter)
        if property_id:
            queryset = queryset.filter(unit__property_id=property_id)
        
        serializer = self.get_serializer(queryset, many=True)
        return Response({
            'success': True,
            'data': serializer.data,
            'count': queryset.count()
        }, status=status.HTTP_200_OK)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def register_tenant_for_unit(request):
    """Register a new tenant and assign them to a unit"""
    # Only landlords and property managers can register tenants
    if request.user.user_type not in ['landlord', 'property_manager']:
        return Response({
            'success': False,
            'message': 'Only landlords and property managers can register tenants'
        }, status=status.HTTP_403_FORBIDDEN)
    
    # Verify unit belongs to the user
    unit_id = request.data.get('unit_id')
    try:
        unit = Unit.objects.get(id=unit_id, property__owner=request.user)
    except Unit.DoesNotExist:
        return Response({
            'success': False,
            'message': 'Unit not found or access denied'
        }, status=status.HTTP_404_NOT_FOUND)
    
    serializer = TenantRegistrationSerializer(data=request.data)
    if serializer.is_valid():
        tenant = serializer.save()
        token, created = Token.objects.get_or_create(user=tenant)
        
        # Get the created tenancy
        tenancy = Tenancy.objects.get(tenant=tenant, unit=unit)
        
        return Response({
            'success': True,
            'message': 'Tenant registered successfully',
            'data': {
                'tenant': UserSerializer(tenant).data,
                'tenancy': TenancySerializer(tenancy).data,
                'token': token.key
            }
        }, status=status.HTTP_201_CREATED)
    
    return Response({
        'success': False,
        'message': 'Failed to register tenant',
        'errors': serializer.errors
    }, status=status.HTTP_400_BAD_REQUEST)


class PaymentViewSet(viewsets.ModelViewSet):
    """ViewSet for managing payments"""
    serializer_class = PaymentSerializer
    permission_classes = [IsAuthenticated]
    
    def get_queryset(self):
        user = self.request.user
        if user.user_type in ['landlord', 'property_manager']:
            return Payment.objects.filter(
                tenancy__unit__property__owner=user
            ).select_related('tenancy', 'tenancy__tenant', 'tenancy__unit', 'tenancy__unit__property')
        elif user.user_type == 'tenant':
            return Payment.objects.filter(
                tenancy__tenant=user
            ).select_related('tenancy', 'tenancy__unit', 'tenancy__unit__property')
        return Payment.objects.none()
    
    def list(self, request, *args, **kwargs):
        queryset = self.get_queryset()
        status_filter = request.query_params.get('status')
        property_id = request.query_params.get('property_id')
        month_year = request.query_params.get('month_year')
        
        if status_filter:
            queryset = queryset.filter(status=status_filter)
        if property_id:
            queryset = queryset.filter(tenancy__unit__property_id=property_id)
        if month_year:
            queryset = queryset.filter(month_year=month_year)
        
        serializer = self.get_serializer(queryset, many=True)
        return Response({
            'success': True,
            'data': serializer.data,
            'count': queryset.count()
        }, status=status.HTTP_200_OK)