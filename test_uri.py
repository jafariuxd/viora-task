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

# Send file URI string (under 500 chars)
file_uri = "file:///data/user/0/com.example/files/user_avatar_178367.jpg"
status, res = request("http://45.195.250.77:3000/api/users/me", method="PATCH", data={"avatar": file_uri}, headers=headers)
print("Update file_uri status & response:", status, res["success"])

# Login again to confirm server returned avatar
status, res = request("http://45.195.250.77:3000/api/auth/login", method="POST", data={"email": "dummy123@example.com", "password": "Password@123"})
print("Retrieved avatar on login:", res["data"]["user"]["avatar"])
