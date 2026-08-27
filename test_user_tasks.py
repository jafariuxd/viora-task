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

status, res = request("http://45.195.250.77:3000/api/auth/login", method="POST", data={"email": "mehranamarbini@gmail.com", "password": "Password@123"})
if status == 200:
    token = res["data"]["tokens"]["accessToken"]
    headers = {"Authorization": f"Bearer {token}"}
    status, res = request("http://45.195.250.77:3000/api/tasks", headers=headers)
    
    with open("user_tasks.json", "w") as f:
        json.dump(res, f, indent=2)
    print("Tasks fetched and saved to user_tasks.json. Status:", status)
else:
    print("Login failed for user:", status, res)
