from PIL import Image
import io
import base64

img = Image.new('RGB', (10, 10), color = 'red')
img.putpixel((5, 5), (0, 255, 0))

bos = io.BytesIO()
img.save(bos, format="PNG", optimize=True)
b64 = base64.b64encode(bos.getvalue()).decode('ascii')
data_uri = f"data:image/png;base64,{b64}"
print(f"Length of small PNG: {len(data_uri)}")

img2 = Image.new('RGB', (16, 16), color = 'blue')
bos2 = io.BytesIO()
img2.save(bos2, format="JPEG", quality=10)
b64_2 = base64.b64encode(bos2.getvalue()).decode('ascii')
data_uri_2 = f"data:image/jpeg;base64,{b64_2}"
print(f"Length of small JPEG: {len(data_uri_2)}")
