# Viora Animation & Transition Revamp Roadmap

## Overview
Goal: Achieve a premium, highly consistent, and fluid animation feel across the app, similar to top-tier luxury apps (e.g., Porsche car rental concept). Animations should feel "orchestrated," where elements prepare the screen together.

## Phases

### Phase 1: Foundation (Core Animation Utilities)
- [x] Refactor `Modifiers.kt` (`animateEnter`).
- [x] Replace `tween` with physics-based `spring` animations for scale, alpha, and translation.
- [x] Add a vertical slide-up (`translationY`) effect so elements "rise" into place gracefully.
- [x] Introduce a shared `SpringSpec` for consistency (e.g., `dampingRatio = 0.8f, stiffness = 300f`).

### Phase 2: Home Screen Orchestration
- [x] Apply the new `animateEnter` with coordinated staggering to all Home screen elements (Header, Tabs, Next Task, Feed items, Discover section).
- [x] Ensure the delays cascade smoothly from top to bottom or in a logical viewing order.
- [x] Refine the tab transition crossfades to use springs.

### Phase 3: Task Detail Fluidity
- [x] Animate the entrance of elements within the `TaskDetailScreen`.
- [x] The title, status pills, date/assignee cards, and description should stagger in right after the sheet/overlay is opened.
- [x] Smooth out the overlay entrance itself if possible.

### Phase 4: App-wide Propagation
- [x] Apply orchestrated entrances to the `AgendaScreen` (Calendar events cascading in).
- [x] Apply to `ListDetailScreen` and `TeamDetailScreen`.
- [x] Final polish and performance check to ensure no jank during animations.
