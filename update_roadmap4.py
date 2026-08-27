with open("ROADMAP_ANIMATIONS.md", "r") as f:
    content = f.read()

content = content.replace("### Phase 4: App-wide Propagation\n- [ ] Apply orchestrated entrances to the `AgendaScreen` (Calendar events cascading in).\n- [ ] Apply to `ListDetailScreen` and `TeamDetailScreen`.\n- [ ] Final polish and performance check to ensure no jank during animations.", "### Phase 4: App-wide Propagation\n- [x] Apply orchestrated entrances to the `AgendaScreen` (Calendar events cascading in).\n- [x] Apply to `ListDetailScreen` and `TeamDetailScreen`.\n- [x] Final polish and performance check to ensure no jank during animations.")

with open("ROADMAP_ANIMATIONS.md", "w") as f:
    f.write(content)
