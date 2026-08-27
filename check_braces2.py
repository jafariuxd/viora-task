import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    text = f.read()

lines = text.split('\n')
count = 0
for i, line in enumerate(lines):
    count += line.count('{')
    count -= line.count('}')
    if count < 0:
        print(f"Negative at line {i+1}: {line}")
    if i % 100 == 0:
        print(f"Line {i}: {count}")

print("Final:", count)
