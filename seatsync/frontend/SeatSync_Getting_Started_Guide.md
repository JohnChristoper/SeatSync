# SeatSync — Getting Started Guide
### What to learn, in what order, and what to actually do first

You already know React, Spring Boot, Tailwind, PostgreSQL, MySQL, and some NoSQL. That covers roughly 60% of this project. What's missing is a specific, learnable list — not "get better at backend," but named technologies you can search for individually. This guide is that list, in the order you'll actually need them, mapped to the phases in the project plan.

---

## Before anything: read your own plan like a spec

Open `SeatSync_FullStack_Project_Plan.docx` and treat Section 15 (Development Phases) as your master checklist. Don't try to hold the whole architecture in your head — just look at "which phase am I on" and only study what that phase needs. Everything below is organized the same way.

---

## Phase 0 — Setup (start here, this week)

**Goal:** an empty but running full-stack skeleton. Nothing fancy yet.

What to search / learn:
- "Spring Initializr" — generate your backend skeleton (dependencies: Spring Web, Spring Data JPA, PostgreSQL driver, Spring Security, Validation)
- "Vite React project setup" — generate your frontend skeleton
- "Tailwind CSS Vite install" — wire Tailwind into that React app
- "Docker Compose Postgres Redis local development" — this is new for you if you haven't used Redis yet; the goal here is just getting a `docker-compose.yml` that starts Postgres + Redis + pgAdmin (optional) with one command

**Concrete next step right now:** install Docker Desktop if you don't have it, then get a `docker-compose.yml` running Postgres + Redis locally and confirm you can connect to both. That's it. Don't touch Spring Boot code until that works.

---

## Phase 1 — Auth

You know Spring Security basics presumably from CRUD apps. What's new here is doing it *properly* with refresh tokens.

What to search:
- "Spring Boot JWT access token refresh token tutorial" (pick one that covers **both** tokens, not just access-token-only tutorials — those are everywhere and skip the harder half)
- "JWT revocation refresh token Redis" — this is the specific technique the plan calls for (storing refresh tokens in Redis so logout can actually invalidate them)
- "Spring Security role based authorization PreAuthorize"

You don't need to learn OAuth or Keycloak or anything like that — plain JWT is enough and is what the plan specifies.

---

## Phase 2 — Catalog (Venue/Event/Showtime CRUD)

Nothing new technically here — this is the CRUD you already know. Use this phase to get your DTO/Service/Repository conventions solid before things get harder. Don't rush past it just because it's familiar; a clean pattern here saves you pain in Phase 4.

If you want one thing to tighten up: search **"Spring Boot DTO mapping MapStruct"** — worth knowing even if you end up mapping DTOs by hand.

---

## Phase 3 — Seat map read model

What to search:
- "Spring Boot Redis integration Spring Data Redis" — this is your first real Redis usage in the app (caching the seat map as a Redis Hash)
- "Redis data types hash set string" — you don't need a deep Redis course, just understand String, Hash, and TTL/expiry. That's ~80% of what you'll use.

---

## Phase 4 — Seat hold (the core problem — spend real time here)

This is the one phase worth over-studying, because it's the centerpiece of the whole project.

What to search, in order:
1. "Redis SETNX atomic operations" — understand *why* this is atomic and what problem that solves
2. "Redis key expiration TTL" — how TTL-based auto-expiry works
3. "race condition database vs Redis lock" — general conceptual reading, doesn't need to be Spring-specific, just to understand *why* you're not using `SELECT ... FOR UPDATE` here
4. "Redis keyspace notifications" (optional/stretch) — only if you want the "seat auto-released" event to trigger a broadcast the instant it expires, rather than relying on the next read to notice

**Don't move to Phase 5 until you can explain, out loud, without notes:** what happens when two people click the same seat at the same millisecond. If you can't explain it cleanly, you don't understand it yet — and this is the exact question you'll get asked about this project later.

---

## Phase 5 — Real-time layer (WebSocket)

Entirely new territory if you haven't done it before. Budget real time for this.

What to search:
- "Spring Boot WebSocket STOMP tutorial" — this is the standard combo; most tutorials use a chat app as the example, which is fine for learning the mechanics
- "STOMP SockJS fallback" — just know what this is for (older browsers/restrictive networks), don't over-invest in it
- "React WebSocket client STOMP" or "@stomp/stompjs react" — the frontend side
- "WebSocket pub sub multiple server instances Redis" — only once single-instance WebSocket works; this is the harder, later part (Phase 5 stretch)

Build order within this phase: get one hardcoded message broadcasting from backend to frontend first. Don't try to wire it to real seat holds until the plumbing itself works.

---

## Phase 6 — Booking & mock payment

Mostly transactional logic you already know (`@Transactional` in Spring). One thing worth a quick search:
- "Spring Boot @Transactional pitfalls" — worth 20 minutes of reading so you don't accidentally wrap the wrong scope and hold a DB transaction open across the Redis call

No real payment gateway needed yet — just a service method that always "succeeds" and produces a Payment record. If you want to eventually swap in something real, "Stripe test mode Spring Boot" is the search when you get there — not now.

---

## Phase 7–8 — Organizer dashboard, admin, notifications

Nothing new here technically — more CRUD, more role-based views, basic aggregation queries (`GROUP BY`-style reporting). Good phase to practice writing efficient JPQL/native queries for the dashboard numbers instead of pulling everything into Java and summing it there.

Search if needed: "Spring Data JPA aggregate queries projections"

---

## Phase 9 — Testing & load testing

This is the second most important phase to actually do, not skip (people skip testing on portfolio projects constantly — doing it properly is a differentiator by itself).

What to search, in order:
1. "JUnit 5 Mockito Spring Boot service test" — if rusty
2. "Testcontainers Spring Boot Postgres" — new if you haven't used it; lets your tests run against a real, disposable Postgres instead of H2
3. "Testcontainers Redis" — same idea for Redis
4. "k6 load testing tutorial" — pick k6 over JMeter if you want something scriptable in JavaScript and lighter to set up; JMeter is fine too if you prefer a GUI
5. Write the specific test: "fire 100 concurrent requests same endpoint" — this is the script that proves your no-double-booking claim. This artifact matters more than almost anything else in the whole project for interview purposes.

---

## Phase 10 — Deployment & polish

What to search:
- "GitHub Actions Spring Boot Maven CI" — test-and-build pipeline
- "GitHub Actions React Vite build" — same for frontend
- "Deploy Spring Boot Docker Railway" or "Render" or "Fly.io" — pick one, don't shop around endlessly, they're all fine for a portfolio project
- "Deploy React Vercel" — frontend hosting, this part is genuinely easy
- "Spring Boot environment variables application.yml profiles" — so your local vs. production config is clean

---

## A few honest notes on how to actually study this

- **Don't take a full course on any one of these technologies before starting.** You'll learn Redis, WebSocket, and Testcontainers far faster by needing them for a specific phase than by front-loading three separate courses before writing a line of SeatSync code. Search, skim, build, get stuck, search again — that loop is faster than linear learning.
- **When you get stuck on Phase 4 or 5, that's normal — it's supposed to be the hard part.** If everything felt easy the whole way through, you picked the wrong project.
- **Keep a running notes file (even just a markdown file in `docs/`) of decisions and gotchas as you go.** This becomes your interview material later, and future-you will not remember why you chose `SETNX` over a database lock unless you write it down now.
- **Don't wait until Phase 9 to write your first test.** Write a basic test as soon as Phase 1's auth logic exists, even if it's simple. It's much harder to retrofit testing habits later than to build them in from day one.

---

## Your actual next step, right now

1. Install Docker Desktop (if not already installed).
2. Write a `docker-compose.yml` with Postgres + Redis.
3. Run `docker compose up` and confirm both are reachable.
4. Generate your Spring Boot skeleton via Spring Initializr.
5. Generate your React + Vite + Tailwind skeleton.
6. Stop there for day one. Commit it to a new GitHub repo before doing anything else.

That's it — that's Phase 0, and it's the only thing you need to think about today.
