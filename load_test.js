import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
stages: [
  { duration: '30s', target: 50  },   // sobe para 10
  { duration: '30s', target: 100 },   // sobe para 30
  { duration: '30s', target: 150 },   // sobe para 50
  { duration: '30s', target: 200 },  // sobe para 70
  { duration: '30s', target: 300 },  // sobe para
  { duration: '30s', target: 350 },  // sobe mais no pico
  { duration: '30s', target: 450 },  // pico mais alto
  { duration: '30s', target: 0 },
]
};

function login() {
  const url = 'http://localhost:8080/login';
  const payload = JSON.stringify({ email: 'admin@gmail.com', password: 'admin' });
  const params = { headers: { 'Content-Type': 'application/json' } };

  const res = http.post(url, payload, params);
  check(res, { 'login ok': (r) => r.status === 200 });
  if (res.status === 200) {
    const token = res.json('token');
    return token;
  }
  return null;
}

function uploadFile(token) {
  const url = 'http://localhost:8080/upload';
  const payload = JSON.stringify({
    fileName: 'teste.txt',
    content: 'conteúdo do ficheiro'
  });
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    }
  };
  const res = http.post(url, payload, params);
  check(res, { 'upload ok': (r) => r.status === 200 });
  if (res.status === 200) {
    const fileId = res.json('fileId');
    return fileId;
  }
  return null;
}

function askQuestion(token, fileId) {
  const url = 'http://localhost:8080/question';
  const payload = JSON.stringify({
    fileIds: [fileId],
    question: 'Qual o conteúdo do ficheiro?'
  });
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    }
  };
  const res = http.post(url, payload, params);
  check(res, {
    'question ok': (r) => r.status === 200,
    'response has content': (r) => r.body && r.body.length > 0,
  });
}

export default function () {
  const token = login();
  if (!token) return;

  const fileId = uploadFile(token);
  if (!fileId) return;

  askQuestion(token, fileId);
  sleep(1);
}
