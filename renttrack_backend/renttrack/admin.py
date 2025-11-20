from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from .models import User, Property, Unit, Tenancy, Payment


@admin.register(User)
class UserAdmin(BaseUserAdmin):
    list_display = ('username', 'email', 'first_name', 'last_name', 'user_type', 'phone_number', 'date_joined')
    list_filter = ('user_type', 'is_staff', 'is_active', 'date_joined')
    search_fields = ('username', 'email', 'first_name', 'last_name', 'phone_number')
    ordering = ('-date_joined',)
    
    fieldsets = BaseUserAdmin.fieldsets + (
        ('Additional Info', {
            'fields': ('user_type', 'phone_number')
        }),
    )
    
    add_fieldsets = BaseUserAdmin.add_fieldsets + (
        ('Additional Info', {
            'fields': ('user_type', 'phone_number', 'first_name', 'last_name', 'email')
        }),
    )


class UnitInline(admin.TabularInline):
    model = Unit
    extra = 1
    fields = ('unit_number', 'floor_number', 'bedrooms', 'bathrooms', 'rent_amount', 'is_occupied')
    readonly_fields = ('is_occupied',)


@admin.register(Property)
class PropertyAdmin(admin.ModelAdmin):
    list_display = ('name', 'owner', 'property_type', 'city', 'total_units', 'occupied_units', 'vacant_units', 'created_at')
    list_filter = ('property_type', 'city', 'country', 'created_at')
    search_fields = ('name', 'address', 'city', 'owner__username', 'owner__email')
    readonly_fields = ('created_at', 'updated_at', 'occupied_units', 'vacant_units')
    inlines = [UnitInline]
    
    fieldsets = (
        ('Basic Information', {
            'fields': ('owner', 'name', 'property_type', 'total_units')
        }),
        ('Location', {
            'fields': ('address', 'city', 'country')
        }),
        ('Details', {
            'fields': ('description',)
        }),
        ('Statistics', {
            'fields': ('occupied_units', 'vacant_units')
        }),
        ('Timestamps', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def get_queryset(self, request):
        queryset = super().get_queryset(request)
        return queryset.select_related('owner')


@admin.register(Unit)
class UnitAdmin(admin.ModelAdmin):
    list_display = ('property', 'unit_number', 'floor_number', 'bedrooms', 'bathrooms', 'rent_amount', 'is_occupied', 'created_at')
    list_filter = ('is_occupied', 'bedrooms', 'bathrooms', 'property__city', 'created_at')
    search_fields = ('unit_number', 'property__name', 'description')
    readonly_fields = ('created_at', 'updated_at')
    
    fieldsets = (
        ('Property & Unit Details', {
            'fields': ('property', 'unit_number', 'floor_number')
        }),
        ('Unit Specifications', {
            'fields': ('bedrooms', 'bathrooms', 'rent_amount', 'is_occupied')
        }),
        ('Description', {
            'fields': ('description',)
        }),
        ('Timestamps', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def get_queryset(self, request):
        queryset = super().get_queryset(request)
        return queryset.select_related('property', 'property__owner')


@admin.register(Tenancy)
class TenancyAdmin(admin.ModelAdmin):
    list_display = ('tenant', 'unit', 'property_name', 'start_date', 'end_date', 'rent_amount', 'status', 'created_at')
    list_filter = ('status', 'start_date', 'created_at')
    search_fields = ('tenant__username', 'tenant__email', 'unit__unit_number', 'unit__property__name')
    readonly_fields = ('created_at', 'updated_at')
    date_hierarchy = 'start_date'
    
    fieldsets = (
        ('Tenancy Details', {
            'fields': ('tenant', 'unit', 'status')
        }),
        ('Dates', {
            'fields': ('start_date', 'end_date', 'rent_due_day')
        }),
        ('Financial Details', {
            'fields': ('rent_amount', 'deposit_amount')
        }),
        ('Notes', {
            'fields': ('notes',)
        }),
        ('Timestamps', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def property_name(self, obj):
        return obj.unit.property.name
    property_name.short_description = 'Property'
    
    def get_queryset(self, request):
        queryset = super().get_queryset(request)
        return queryset.select_related('tenant', 'unit', 'unit__property')


@admin.register(Payment)
class PaymentAdmin(admin.ModelAdmin):
    list_display = ('tenant_name', 'property_name', 'unit_number', 'amount', 'payment_date', 'payment_method', 'status', 'transaction_ref')
    list_filter = ('status', 'payment_method', 'payment_date', 'created_at')
    search_fields = ('tenancy__tenant__username', 'tenancy__tenant__email', 'transaction_ref', 'tenancy__unit__unit_number')
    readonly_fields = ('created_at', 'updated_at')
    date_hierarchy = 'payment_date'
    
    fieldsets = (
        ('Payment Details', {
            'fields': ('tenancy', 'amount', 'payment_date', 'month_year')
        }),
        ('Transaction Info', {
            'fields': ('payment_method', 'transaction_ref', 'status')
        }),
        ('Notes', {
            'fields': ('notes',)
        }),
        ('Timestamps', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def tenant_name(self, obj):
        return obj.tenancy.tenant.get_full_name()
    tenant_name.short_description = 'Tenant'
    
    def property_name(self, obj):
        return obj.tenancy.unit.property.name
    property_name.short_description = 'Property'
    
    def unit_number(self, obj):
        return obj.tenancy.unit.unit_number
    unit_number.short_description = 'Unit'
    
    def get_queryset(self, request):
        queryset = super().get_queryset(request)
        return queryset.select_related('tenancy', 'tenancy__tenant', 'tenancy__unit', 'tenancy__unit__property')