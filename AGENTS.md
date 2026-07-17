# Project Context & Agent Instructions

## Project Overview
This is a modern, premium Task Management Android application (Viora) built with Kotlin and Jetpack Compose.

## Design Language & Styling Rules
- **Theme:** Immersive Dark theme (Black backgrounds) with stark White cards.
- **Accent Color:** Neon Lime (`#B4FF00` or `VioraNeonLime`), used for primary actions, selected states, and add buttons.
- **Corner Radiuses:** Cards use very rounded corners, typically `28.dp`.
- **Typography:** SF Pro Display is the primary font family.
- **Specific Dimensions (CRITICAL):**
  - Spacing between vertical and horizontal cards in the Task Detail screen must be exactly `5.dp`.
  - The Status buttons (To-Do, In Progress, Done) have a fixed height of `72.dp`.
  - The "Due date" and "Assignees" cards have a fixed height of `101.dp`.
  - Horizontal padding for the scrollable content in the Task Detail screen is `2.dp` (left and right) to align perfectly with the Home screen layout.

## Current State
- **Home Screen:** Features a top navigation/header, a prominent "Next Task" card, and a scrollable feed of subsequent tasks.
- **Task Detail Screen:** Opens as a full-screen bottom-up animated overlay. Features a custom top bar (with folder selector), large title card, status selection buttons, due date and assignees cards (two-column layout), tags, and description. 
- **View Models:** State is managed via `VioraTaskViewModel`.

## Instructions for the Agent
When continuing development on this project:
1. STRICTLY adhere to the established visual identity (colors, spacing, and corner radiuses).
2. Do not use generic Material components if they conflict with the custom Viora design system.
3. Always check existing composables (like `TaskDetailScreen` and `HomeScreen`) before adding new features to ensure consistency.
