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
refresh_token = res["data"]["tokens"]["refreshToken"]
access_token = res["data"]["tokens"]["accessToken"]

print("Login status:", status)

# Call refresh endpoint directly
status, res = request("http://45.195.250.77:3000/api/auth/refresh", method="POST", data={"refreshToken": refresh_token})
print("Refresh status:", status)
print("New access token generated:", "accessToken" in res.get("data", {}))
