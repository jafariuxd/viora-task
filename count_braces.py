with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'r') as f:
    lines = f.readlines()

brace_count = 0
for i, line in enumerate(lines):
    brace_count += line.count('{') - line.count('}')
    print(f"{i+1}: {brace_count}")
