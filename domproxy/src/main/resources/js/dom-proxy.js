let reconnectInterval = null;
let eventSource = null;

function connectToServer() {
	if (!eventSource || eventSource.readyState === EventSource.CLOSED) {
		const eventSource = new EventSource("http://127.0.0.1:5001/sse");
		eventSource.onopen = () => {
			console.log("Client connected to server, waiting for jobs...");
			clearInterval(reconnectInterval);
		};
		eventSource.onerror = (error) => {
			console.error(error);
		};
		eventSource.onmessage = async (event) => {
			const job = event.data;
			if (job) {
				runJob(job);
			} else {
				console.log("Did not receive any jobs via SSE");
			}
		};
	}
}

function runJob(job) {
	console.log(`Running job: ${job}`);
	return fetch(`/rest/jobs/latest/run?job=${job}`)
		.then((response) => {
			if (!response.ok) {
				throw new Error(response.status);
			}
			return response.text();
		})
		.then((targetOutput) => {
			return fetch("http://127.0.0.1:5001/collector", {
				method: "POST",
				body: targetOutput,
				headers: { "Content-Type": "text/plain" },
			});
		})
		.then((sendOutput) => sendOutput.text())
		.catch((error) => {
			console.error(error);
		});
}

document.addEventListener("DOMContentLoaded", () => {
	reconnectInterval = setInterval(connectToServer, 5000);
});
