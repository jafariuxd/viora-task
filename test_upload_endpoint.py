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
        return e.code, e.read().decode('utf-8')
    except Exception as e:
        return 0, str(e)

for path in ["/api/upload", "/api/files/upload", "/api/users/avatar", "/api/media/upload"]:
    status, res = request(f"http://45.195.250.77:3000{path}", method="POST")
    print(path, status)
