with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

content = content.replace("[versions]\n", "[versions]\nzxing = \"3.5.3\"\n")
content = content.replace("[libraries]\n", "[libraries]\nzxing-core = { group = \"com.google.zxing\", name = \"core\", version.ref = \"zxing\" }\n")

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)
