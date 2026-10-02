# Barniz Express - Backend Design

Shipping quote API for handcrafted Barniz de Pasto pieces (mopa-mopa lacquer). Demo of the
**Decorator** pattern: each shipping option wraps the shipment and adds cost, description and notes.
The frontend (separate teammate) consumes the contract below. Do not change it.

## Stack
Java 21, Spring Boot 3.x, Maven, JUnit 5 + MockMvc, jjwt for tokens, Bean Validation.
No database: products are seeded in memory. All code, comments, identifiers and messages in English.

## Architecture (hexagonal)
```
com.barnizexpress
  domain/        Shipment (interface), BaseShipment, ShipmentDecorator (abstract),
                 FragilePackagingDecorator, InsuranceDecorator, CustomsDecorator,
                 GiftWrapDecorator, ExpressDecorator, Product, OptionCode, Layer
  application/   QuoteService, QuoteCommand, ports: ProductRepository, TokenIssuer
  infrastructure/
    web/         AuthController, ProductController, OptionController, QuoteController,
                 dto records, ApiExceptionHandler, CorsConfig, JwtAuthFilter
    persistence/ InMemoryProductRepository
    security/    JwtTokenIssuer
    audit/       @AuditedQuote + AuditAspect (custom annotation, Spring AOP, logs who quoted what)
```
`Shipment` exposes `baseCostCop()`, `totalCostCop()`, `layers()` (list of `Layer(code,label,costCop,notes)`),
`description()`. A decorator holds a `Shipment` (composition, no inheritance chain) and adds one layer.
`QuoteService` builds the chain in the fixed order below.

## Wrapping order (innermost to outermost, fixed)
BaseShipment -> FRAGILE -> INSURANCE -> CUSTOMS -> GIFT -> EXPRESS
`layers` in the response follows this order. The base shipment is not a layer, it is `baseCostCop`.

## Pricing rules (COP, integers, round half up)
- Base: `12000 + 6000 * weightKg`; if destination country is not "Colombia" (case-insensitive) add `45000`.
- FRAGILE: +18000. Notes: "Foam lining", "Double-wall box".
- INSURANCE: 2% of `declaredValueCop`, minimum 5000. Notes include the covered value.
- CUSTOMS: +60000. Only valid for international destinations, otherwise 400. Notes: "DIAN export declaration", "Commercial invoice".
- GIFT: +9000. Requires non-blank `giftMessage` of at most 140 chars, otherwise 400. Notes include the message.
- EXPRESS: +35% of the total accumulated by all inner layers plus base (order-dependent on purpose, this shows why Decorator order matters). Notes: "1-2 business days".
- Incompatible pairs: CUSTOMS <-> EXPRESS (400 if both requested).
- Unknown option code, unknown productId, `declaredValueCop` < 0 or blank city/country: 400.
- `declaredValueCop` default is the product `basePriceCop` when null.

## Contract
Base URL `http://localhost:8080`. CORS allowed for `http://localhost:5173`. All routes except login require
`Authorization: Bearer <token>`; missing/invalid/expired is 401 `{error,message}`.

- `POST /api/v1/auth/login` `{username,password}` -> `{token,expiresAt}`. Only user `demo` / `demo123`. Token expires in 24h. Wrong credentials: 401.
- `GET /api/v1/products` -> `[{id,name,description,basePriceCop,weightKg,imageUrl}]`. Seed 4 pieces (e.g. a mopa-mopa lacquered tray, a bowl, a decorative box, a jewelry set). `imageUrl` like `/images/<id>.jpg`.
- `GET /api/v1/options` -> `[{code,name,description,pricingRule,incompatibleWith}]` for FRAGILE, INSURANCE, CUSTOMS, GIFT, EXPRESS. `pricingRule` is a short human string.
- `POST /api/v1/quotes` `{productId,destination:{city,country},declaredValueCop,options:[code],giftMessage?}` ->
  `{currency:"COP",baseCostCop,totalCop,layers:[{code,label,costCop,notes:[string]}]}`
- Errors: 400 `{error,message}`, e.g. `{"error":"VALIDATION","message":"CUSTOMS requires an international destination"}`.

## Custom decorators (annotation level)
`@AuditedQuote` on `QuoteService.quote(...)`, implemented with an aspect, logs user, product, options, total.
Validation annotations `@ValidGiftMessage` (max 140, no trailing spaces) and `@SafeText` (rejects SQL-injection-looking input such as `' OR 1=1`, `--`, `;DROP`).

## Tests (must pass with `mvn test`)
- One unit test class per decorator (cost, layer, notes).
- Order test: EXPRESS over (base+all inner) equals the expected number for a known case.
- MockMvc: login ok/fail, 401 without token, products, options, quote happy path, each 400 rule.

## Deliverables
- `mvn spring-boot:run` serves on 8080. `README.md` with setup, endpoints, a Mermaid class diagram of the decorators,
  and a "Decorators created" section (the teacher requires documenting the custom decorators).
- Small, meaningful commits in English. Do NOT push, do NOT touch anything outside this repo.
