# Oldtech Ecommerce Website - Secondhand Devices Marketplace (Backend)

## Overview
**Year:** 2025  
The **Oldtech Ecommerce Website** is a backend system designed for a secondhand devices marketplace.
Built with **Spring Boot** and **Java 23**, it delivers a modern, production-ready framework optimized for scalability, security, and real-time operations.

---
## Admin Pages
![Admin tổng quan](https://github.com/user-attachments/assets/00970fe8-ea91-4a2f-84aa-09117eac6583)

![Hỗ trợ nội dung](https://github.com/user-attachments/assets/09bd3e95-2763-4c8a-96f5-ee5871ef9189)

![Quản lý thông báo](https://github.com/user-attachments/assets/7e8e50ca-4bc8-4a0c-aaa7-c8cf01c3ec57)


## Customer Pages
<img width="1841" height="891" alt="Trang chủ" src="https://github.com/user-attachments/assets/2fb062c4-2267-4b74-86aa-3834f65c1f6e" />
![Danh mục(Trang chủ)](https://github.com/user-attachments/assets/1ea4778e-e410-47ea-8b3b-3cd17d162078)


## Seller Pages

![Thống kê (Người bán)](https://github.com/user-attachments/assets/b1637830-4f4b-47a6-ab2d-e9ce480a0a7d)


![Sản phẩm của tôi (Người bán)](https://github.com/user-attachments/assets/d05312d8-7f58-422e-8bcc-af2737a8c13a)



![Đánh giá người bán](https://github.com/user-attachments/assets/786c3e20-b0e5-4592-bf0a-a7b5f59f68cb)



## Key Features

- **Spring Boot + Spring Framework**
  - Simplified configuration and rapid development.
  - Dependency Injection and MVC architecture for maintainable, testable code.

- **Authentication & Security**
  - **OAuth2** with Facebook and Google APIs for secure third-party login.
  - **Two-Factor Authentication (2FA)** for enhanced account protection.
  - **JWT (JSON Web Token)** for secure, role-based access control across **users, customers, sellers, admins, and superadmins**.

- **Payments**
  - **MoMo Integration** for seamless and secure transaction processing.

- **Real-time Streaming**
  - **Kafka** for handling order notifications, inventory updates, and event-driven workflows.

- **Database**
  - Hosted with **Tailscale + MySQL**, ensuring secure, networked relational data storage.

- **Development & Testing**
  - **Ngrok** for HTTPS-enabled local development and external testing.
  - **Postman** for API testing and validation of endpoints.

---

## System Architecture

1. **Authentication Layer**
   - OAuth2 social login, 2FA, JWT-based access control.

2. **Business Logic Layer**
   - Role-based operations (customers, sellers, admins, superadmins).
   - Product management, transaction workflows.

3. **Data & Messaging Layer**
   - MySQL via Tailscale for relational data.
   - Kafka for event streaming.

4. **External Services**
   - MoMo payment gateway integration.

---

## Tech Stack

- **Language:** Java 23  
- **Frameworks:** Spring Boot, Spring Framework  
- **Security:** OAuth2, 2FA, JWT  
- **Payments:** MoMo API  
- **Streaming:** Apache Kafka  
- **Database:** MySQL (via Tailscale)  
- **Dev Tools:** Ngrok, Postman  

---

## Development & Testing

- Use **Ngrok** for secure local testing with HTTPS.  
- APIs are validated using **Postman collections**.  
- Integration tests ensure smooth interaction between services (auth, payments, messaging).  

---

## Future Improvements

- Optimize the  code and UX/UI
- CI/CD pipelines with GitHub Actions or Jenkins.  
- Extended analytics dashboards for sellers and admins.  

---

