with open("ROADMAP_ANIMATIONS.md", "r") as f:
    content = f.read()

content = content.replace("### Phase 2: Home Screen Orchestration\n- [ ] Apply the new `animateEnter` with coordinated staggering to all Home screen elements (Header, Tabs, Next Task, Feed items, Discover section).\n- [ ] Ensure the delays cascade smoothly from top to bottom or in a logical viewing order.\n- [ ] Refine the tab transition crossfades to use springs.", "### Phase 2: Home Screen Orchestration\n- [x] Apply the new `animateEnter` with coordinated staggering to all Home screen elements (Header, Tabs, Next Task, Feed items, Discover section).\n- [x] Ensure the delays cascade smoothly from top to bottom or in a logical viewing order.\n- [x] Refine the tab transition crossfades to use springs.")

with open("ROADMAP_ANIMATIONS.md", "w") as f:
    f.write(content)
