# Mini Bank Application

Mini Bank merupakan aplikasi simulasi perbankan sederhana yang dibangun menggunakan **Java 21, Spring Boot 4, dan PostgreSQL**.

## Fitur Utama

1. **Registrasi Customer**
   - Customer melakukan registrasi.
   - Sistem otomatis membuat User, Customer, Account, dan Account Balance.

2. **Login**
   - Customer yang sudah terdaftar dapat login menggunakan kredensial.

3. **Transfer**
   - Customer dapat melakukan transfer ke rekening nasabah lain.
   - Sistem melakukan validasi rekening dan saldo sebelum transaksi diproses.

## Arsitektur Aplikasi

Aplikasi ini menggunakan **Modular Monolith Architecture**.

Arsitektur ini dipilih untuk mempercepat proses pengembangan sekaligus menjaga pemisahan tanggung jawab setiap modul.

Modul aplikasi meliputi:
- Authentication
- Customer
- Account
- Transaction
- registrasi


Setiap modul memiliki tanggung jawab bisnis masing-masing sehingga lebih mudah dikembangkan dan dipelihara.

Sebagai contoh, modul Transaction dapat dikembangkan lebih lanjut untuk mendukung setor tunai, tarik tunai, transfer internal, dan transfer antarbank.

## Teknologi

- Java 21
- Spring Boot 3
- Spring Data JPA
- Spring Security & JWT
- PostgreSQL
- Flyway
- Docker & Docker Compose

## Cara Menjalankan Aplikasi

**Prerequisites:** Docker sudah terinstal di komputer.

1. Clone repository:

```bash
git clone https://github.com/AminTurmudiYusup/minibank.git
```

2. Masuk ke direktori project:

```bash
cd mini-bank
```


3. Build dan jalankan aplikasi:

```bash
docker compose up --build -d
```

Aplikasi dan database akan berjalan di dalam Docker container. Database migration akan dijalankan otomatis menggunakan Flyway.

Aplikasi dapat diakses melalui:

```text
{{baseUrl}}/api/registrations
{{baseUrl}}/api/auth/login
{{baseUrl}}/api/transfers
```


Untuk menghentikan aplikasi:

```bash
docker compose down
```
