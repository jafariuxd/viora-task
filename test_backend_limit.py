import urllib.request
import json
import base64

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
if status == 200:
    access_token = res["data"]["tokens"]["accessToken"]
    # Try updating avatar with 1000 chars
    long_avatar = "data:image/png;base64," + ("A" * 1000)
    status2, res2 = request("http://45.195.250.77:3000/api/users/me", method="PATCH", data={"avatar": long_avatar}, headers={"Authorization": f"Bearer {access_token}"})
    print("Update status:", status2)
    print("Update response:", res2)
else:
    print("Login Failed:", status, res)
