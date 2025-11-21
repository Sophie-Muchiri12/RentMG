# RentMG Seed Data - Login Credentials

All accounts use the password: **password123**

## Database Summary

- **10 Landlords** with 12 rental properties
- **10 Tenants** renting units across different properties
- **49 Units** total (3-6 units per property)
- **8 Active Leases** and 2 ended leases (historical data)
- **127 Payment Records** showing payment history over time
- **16 Maintenance Issues** with various statuses

## Landlord Accounts

| Email | Full Name |
|-------|-----------|
| john.smith@landlord.com | John Smith |
| sarah.johnson@landlord.com | Sarah Johnson |
| michael.brown@landlord.com | Michael Brown |
| emily.davis@landlord.com | Emily Davis |
| david.wilson@landlord.com | David Wilson |
| lisa.anderson@landlord.com | Lisa Anderson |
| robert.taylor@landlord.com | Robert Taylor |
| jennifer.martinez@landlord.com | Jennifer Martinez |
| james.garcia@landlord.com | James Garcia |
| mary.rodriguez@landlord.com | Mary Rodriguez |

## Tenant Accounts

| Email | Full Name |
|-------|-----------|
| alice.cooper@tenant.com | Alice Cooper |
| bob.williams@tenant.com | Bob Williams |
| carol.martinez@tenant.com | Carol Martinez |
| daniel.lee@tenant.com | Daniel Lee |
| emma.thomas@tenant.com | Emma Thomas |
| frank.moore@tenant.com | Frank Moore |
| grace.jackson@tenant.com | Grace Jackson |
| henry.white@tenant.com | Henry White |
| iris.harris@tenant.com | Iris Harris |
| jack.thompson@tenant.com | Jack Thompson |

## Properties

Each landlord owns 1-2 properties in various locations:

1. Sunset Apartments - 123 Main Street, Nairobi
2. Green Valley Complex - 456 Oak Avenue, Mombasa
3. Palm Heights - 789 Beach Road, Nakuru
4. Riverside Residences - 321 River Drive, Kisumu
5. Mountain View Estate - 654 Hill Street, Eldoret
6. Urban Towers - 987 City Center, Nairobi
7. Coastal Villas - 147 Ocean Boulevard, Mombasa
8. Garden Apartments - 258 Park Lane, Thika
9. Lakeside Homes - 369 Lake View, Kisumu
10. Skyline Plaza - 741 High Street, Nairobi
11. Harbor Point - 852 Port Road, Mombasa
12. Valley View - 963 Valley Drive, Nakuru

## Data Features

### Leases
- Leases span 6-24 months back to simulate real usage
- 80% are currently active, 20% have ended (historical data)
- Each active tenant has a unit assigned

### Payments
- Monthly payment records for each lease
- Mix of completed, pending, and failed payments (mostly completed)
- Realistic payment methods (M-Pesa and Bank transfers)
- Payment references and M-Pesa checkout IDs where applicable
- Some payment variations to simulate real-world scenarios

### Issues
- Various maintenance issues reported by tenants
- Mix of priorities (low, normal, high)
- Different statuses (open, in_progress, resolved, closed)
- Issue types include:
  - Plumbing problems (leaks, clogs)
  - Electrical issues (outlets, lights)
  - HVAC problems (AC, heating)
  - Security issues (locks)
  - Maintenance requests (paint, fixtures)
  - General complaints (noise, parking)

## Re-running the Seed Script

To clear and re-seed the database:

```bash
cd /home/pc/StudioProjects/RentMG/rentmg_backend
.venv/bin/python seed_data.py
```

This will clear all existing data and create fresh seed data with new random variations.
