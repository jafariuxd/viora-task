import urllib.request
import urllib.parse
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

def test():
    status, res = request("http://45.195.250.77:3000/api/auth/login", method="POST", data={"email": "dummy123@example.com", "password": "Password@123"})
    print("Login:", status, res)
    if status not in (200, 201):
        return
            
    token = res["data"]["tokens"]["accessToken"]
    headers = {"Authorization": f"Bearer {token}"}
    
    status, res2 = request("http://45.195.250.77:3000/api/users/me/report", headers=headers)
    print("Report:", status, res2)

    status, res3 = request("http://45.195.250.77:3000/api/teams", headers=headers)
    print("Teams:", status, res3)

    status, res4 = request("http://45.195.250.77:3000/api/lists", headers=headers)
    print("Lists:", status, res4)

    status, res5 = request("http://45.195.250.77:3000/api/tasks", headers=headers)
    print("Tasks:", status, res5)

test()
