with open("ROADMAP_ANIMATIONS.md", "r") as f:
    content = f.read()

content = content.replace("### Phase 3: Task Detail Fluidity\n- [ ] Animate the entrance of elements within the `TaskDetailScreen`.\n- [ ] The title, status pills, date/assignee cards, and description should stagger in right after the sheet/overlay is opened.\n- [ ] Smooth out the overlay entrance itself if possible.", "### Phase 3: Task Detail Fluidity\n- [x] Animate the entrance of elements within the `TaskDetailScreen`.\n- [x] The title, status pills, date/assignee cards, and description should stagger in right after the sheet/overlay is opened.\n- [x] Smooth out the overlay entrance itself if possible.")

with open("ROADMAP_ANIMATIONS.md", "w") as f:
    f.write(content)
