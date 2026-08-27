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

# Register test
status, res = request("http://45.195.250.77:3000/api/auth/register", method="POST", data={
    "fullName": "Avatar Test",
    "username": "avatartest123",
    "email": "avatartest123@example.com",
    "password": "Password@123",
    "avatar": "https://example.com/avatar.jpg"
})
print("Register with avatar:", status, res)

# Login
status, res = request("http://45.195.250.77:3000/api/auth/login", method="POST", data={"email": "avatartest123@example.com", "password": "Password@123"})
print("Login:", status, res)
token = res["data"]["tokens"]["accessToken"]
headers = {"Authorization": f"Bearer {token}"}

# Update user
status, res = request("http://45.195.250.77:3000/api/users/me", method="PATCH", data={"avatar": "https://example.com/new_avatar.jpg"}, headers=headers)
print("Update user avatar:", status, res)

# Get user report
status, res = request("http://45.195.250.77:3000/api/users/me/report", method="GET", headers=headers)
print("Get report:", status, res)
