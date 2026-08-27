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

# Login
status, res = request("http://45.195.250.77:3000/api/auth/login", method="POST", data={"email": "dummy123@example.com", "password": "Password@123"})
token = res["data"]["tokens"]["accessToken"]
headers = {"Authorization": f"Bearer {token}"}

# Create Task without listId
status, res = request("http://45.195.250.77:3000/api/tasks", method="POST", data={"name": "Test Task", "status": "todo"}, headers=headers)
print("Create Task (no listId):", status, res)

# Get Lists
status, res_lists = request("http://45.195.250.77:3000/api/lists", headers=headers)
print("Lists:", res_lists)
if res_lists["data"]:
    list_id = res_lists["data"][0]["id"]
    status, res = request("http://45.195.250.77:3000/api/tasks", method="POST", data={"name": "Test Task", "status": "todo", "listId": list_id}, headers=headers)
    print("Create Task (with listId):", status, res)
