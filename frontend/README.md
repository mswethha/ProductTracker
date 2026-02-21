# Product Tracker – React frontend

- **Subscribers**: Register or log in, then view product status (available / out of stock) on the home page.
- **Telegram**: Subscribers who use the bot get alerts when product status changes.
- **Admin**: Log in with an admin account to open the Admin panel (product status, last in stock, Telegram subscribers).

## Run

1. Start the Spring Boot backend on port 8080.
2. From this folder run:
   ```bash
   npm install
   npm run dev
   ```
3. Open http://localhost:5173

## Create first admin

1. In backend `application.properties` set:
   ```properties
   product.tracker.admin-secret=yourSecret
   ```
2. Restart backend, then:
   ```bash
   curl -X POST http://localhost:8080/api/auth/register-admin \
     -H "Content-Type: application/json" \
     -d '{"secret":"yourSecret","username":"admin","email":"admin@example.com","password":"admin123"}'
   ```
3. Log in on the frontend with that username and password; the Admin link will appear.
