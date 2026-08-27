import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

print("event_pattern:", bool(re.search(r"""        Row\(
            modifier = Modifier
                \.fillMaxWidth\(\)
                \.padding\(14\.dp\),
            verticalAlignment = Alignment\.CenterVertically
        \) \{
            if \(loading\) \{
                // Shimmer layout
                Box\(""", content)))

print("event_end_pattern:", bool(re.search(r"""                        textAlign = androidx\.compose\.ui\.text\.style\.TextAlign\.Center
                    \)
                \}
            \}
        \}
        \}
    \}
\}""", content)))

