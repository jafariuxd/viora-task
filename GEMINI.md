# Viora Task - Project Notebook

## Overview
Viora Task is a modern, premium Task Management Android application built with Kotlin and Jetpack Compose. It focuses on a clean, immersive dark theme with high-contrast accent colors and smooth animations.

## Core Features
1.  **Task Management:**
    *   Create, view, update, and delete tasks.
    *   Tasks have statuses: To-Do (Red), In Progress (Blue), and Done (Green).
    *   Tasks belong to Lists and Teams.
    *   Tasks can have due dates and assignees.
2.  **Organization:**
    *   **Teams:** Groupings of lists and members.
    *   **Lists:** Categories within teams to organize specific tasks.
3.  **Agenda & Calendar:**
    *   View upcoming events.
    *   Integrates with device calendar / Google Calendar.
    *   Custom calendar grid and timeline view.
4.  **Discover Section:**
    *   Daily insights (quotes).
    *   Suggested articles related to productivity and habits.
    *   Data is cached per session (`DiscoverCache`) to optimize performance and prevent reloading on tab switches.
5.  **User Profile & Settings:**
    *   Track completed and overdue tasks.
    *   Manage app settings.

## Design Language & System (Viora Design System)
*   **Theme:** Immersive Dark theme (Black backgrounds `Color(0xFF000000)` / `VioraBackground`).
*   **Cards:** Stark White cards (`Color(0xFFFFFFFF)`) or dark gray cards (`Color(0xFF1C1C1E)`).
*   **Accent Color:** Neon Lime (`#B4FF00` or `VioraNeonLime`). Used for primary actions, selected states, and add buttons.
*   **Corner Radii:** Cards use very rounded corners, typically `24.dp` or `28.dp`.
*   **Typography:** SF Pro Display is the primary font family.
*   **Status Colors (VioraColors):**
    *   To-Do: Light Red container, Dark Red text
    *   In Progress: Light Blue container, Dark Blue text
    *   Done: Light Green container, Dark Green text

## Technical Architecture
*   **UI Framework:** Jetpack Compose.
*   **State Management:** ViewModels (`VioraTaskViewModel`, `AgendaViewModel`) using `StateFlow`.
*   **Data Layer:** Currently uses mock data/local state management for rapid prototyping.
*   **Animations:** Extensive use of Compose animation APIs (`Animatable`, `spring`, `tween`) for enter animations (`animateEnter`), stagger effects, and shimmer loading states (`shimmerEffect`).

## Performance Guidelines
*   Use `itemsIndexed` in `LazyColumn`/`LazyRow` for lists with staggered animations instead of `indexOf` to prevent scrolling lag.
*   Cache static or semi-static content (like the Discover section) to avoid unnecessary recompositions.
*   Use `drawBehind` for custom graphics modifiers like `shimmerEffect` to avoid triggering layout passes.
