import urllib.request
import json

def request(url, method="GET", data=None, headers={}):
    req = urllib.request.Request(url, method=method, headers=headers)
    if data:
        req.add_header('Content-Type', 'application/json')
        data = json.dumps(data).encode('utf-8')
    try:
        with urllib.request.urlopen(req, data=data) as response:
            return response.status, json.loads(response.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        return e.code, json.loads(e.read().decode('utf-8'))

status, res = request("http://45.195.250.77:3000/api/auth/login", method="POST", data={"email": "dummy123@example.com", "password": "Password@123"})
token = res["data"]["tokens"]["accessToken"]
headers = {"Authorization": f"Bearer {token}"}

# Long string > 500 chars
long_str = "data:image/jpeg;base64," + "A" * 550
status, res = request("http://45.195.250.77:3000/api/users/me", method="PATCH", data={"avatar": long_str}, headers=headers)
print("Long string status & response:", status, res)

# Under 500 chars
short_str = "https://example.com/avatar123.jpg"
status, res = request("http://45.195.250.77:3000/api/users/me", method="PATCH", data={"avatar": short_str}, headers=headers)
print("Short string status & response:", status, res)
