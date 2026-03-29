# Order Processing Specification

## Purpose

This specification defines the Order Processing domain for the e-commerce application, enabling users to place orders from shopping carts with order tracking, unique order numbers, and shipping address management.

## ADDED Requirements

### Requirement: Order Creation

The system SHALL allow users to create orders from their shopping carts. Each order MUST contain items derived from the cart, a unique order number, shipping address, and initial status of PENDING.

- GIVEN a user with items in their shopping cart
- WHEN the user submits an order request with shipping address
- THEN a new order is created with unique order number, items from cart, total amount, and PENDING status

- GIVEN a user with an empty shopping cart
- WHEN the user attempts to create an order
- THEN an error is returned indicating the cart is empty

### Requirement: Unique Order Number

The system SHALL generate a unique order number for each order. Order numbers MUST be non-null, non-empty strings.

- GIVEN a new order is being created
- WHEN the order is saved
- THEN a unique order number is generated and associated with the order

### Requirement: Order Status Tracking

The system SHALL track order status. The order MUST support the following statuses: PENDING, PAID, SHIPPED, DELIVERED, CANCELLED.

- GIVEN an order with PENDING status
- WHEN the status is updated to PAID
- THEN the order status changes to PAID

- GIVEN an order with PAID status
- WHEN the status is updated to CANCELLED
- THEN the order status changes to CANCELLED

### Requirement: Shipping Address

The system SHALL capture shipping address with each order. The shipping address MUST contain: street, city, state, zipCode, country.

- GIVEN an order creation request
- WHEN the shipping address is provided
- THEN the shipping address is stored with the order

- GIVEN an order creation request
- WHEN the shipping address is missing or incomplete
- THEN an error is returned indicating invalid shipping address

### Requirement: View User Orders

The system SHALL allow users to view their order history. The system MUST return orders sorted by creation date (newest first).

- GIVEN a user with existing orders
- WHEN the user requests their order history
- THEN a list of orders is returned sorted by creation date descending

- GIVEN a user with no orders
- WHEN the user requests their order history
- THEN an empty list is returned

### Requirement: Get Single Order

The system SHALL allow users to retrieve a specific order by ID.

- GIVEN an order exists in the system
- WHEN a user requests the order by ID
- THEN the full order details are returned

- GIVEN an order does not exist in the system
- WHEN a user requests a non-existent order
- THEN an OrderNotFoundException is thrown

## MODIFIED Requirements

None.

## REMOVED Requirements

None.
