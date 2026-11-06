# Project Summary — Room & Equipment Booking System

**Project type:** University team coursework, Object-Oriented Programming / Data Structures  
**Language:** Java  
**UI:** Java Swing / AWT  
**Database:** SQLite via JDBC

## Problem and approach

In a classroom project, the team developed a desktop interface to model room and equipment reservation requests. The user interface separates student booking tasks from administrative review functions, backed by a local relational database.

## Features represented in the codebase

| User features | Administrator features |
|---|---|
| Account creation and sign-in | Administrative sign-in (local environment configuration only) |
| Meeting-room selection / booking forms | Approve or review booking requests |
| Booking calendar, status, and history | Equipment inventory and availability views |
| Personal notifications and account screens | Notifications, basic analytics, and user status |

## Learning outcomes

- Domain classes and object-oriented design (`User`, `Booking`, `BookingDetail`, `Notification`)
- Java Swing event-driven UI design
- JDBC and SQLite data interaction
- Multi-screen navigation and admin/user workflows

## Scope and integrity

The repository intentionally excludes real SQLite data and generated Java class files. This snapshot is preserved for study and code review rather than production deployment. No independent end-to-end functional or security certification is claimed.
