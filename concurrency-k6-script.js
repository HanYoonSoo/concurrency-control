import http from "k6/http";

export const options = {
    scenarios: {
        spike: {
            executor: "constant-vus",
            vus: 200,
            duration: "1s",
            gracefulStop: "5m"
        },
    },
};

export default function () {
    const url = "http://localhost:8080/api/v3/tickets/1/purchase"
    const data = {
        "amount": 1
    }
    const res = http.post(url, JSON.stringify(data), {
        headers: {
            "Content-Type": "application/json"
        },
    });
    console.log(res.body);
};