import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
    stages: [
        {duration: '10s', target: 30000},
        {duration: '10s', target: 30000},
        {duration: '10s', target: 20000},
        {duration: '10s', target: 10000},
        {duration: '10s', target: 5000},
        {duration: '10s', target: 5000},
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% 요청이 500ms 이내에 응답
        http_req_failed: ['rate<0.01'], // 에러율 1% 미만
    },
};

function generatePhoneNumber(){
    return '010-' + String(Math.floor(1000 + Math.random() * 9000)) + '-' + String(Math.floor(1000 + Math.random() * 9000));
}

export default function (){
    // 요청 본문 데이터
    const userId = Number(__VU); // 현재 가상 사용자 ID를 사용해 고유 ID로 설정
    // console.log(`User ID: ${userId}`); // 로그 출력
    const payload = JSON.stringify({
       userId: userId,
       eventId: 1,
       name: 'name' + userId,
       phone: generatePhoneNumber(),
    });

    // POST 요청 헤더
    const headers = {
        'Content-Type': 'application/json',
    };
    // POST 요청 전송
    const response = http.post('http://localhost:8080/api/v10/tickets/purchase', payload, {headers: headers});
    check(response, {
        'status is 200': (r) => r.status === 200,
        'response contains success message': (r) => typeof r.body === 'string',
    });

    sleep(1);
}