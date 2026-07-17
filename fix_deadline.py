import sys

with open('app/src/main/java/com/example/ui/screens/auth/DeadlineScreen.kt', 'r') as f:
    content = f.read()

# Replace the stray } at the end
content = content.replace("            )\n}\n        }\n    }\n}", 
"""            )
        }
    }
}""")

with open('app/src/main/java/com/example/ui/screens/auth/DeadlineScreen.kt', 'w') as f:
    f.write(content)

