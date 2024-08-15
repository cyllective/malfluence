document.addEventListener("DOMContentLoaded", function () {
	const hiddenPlugins = [];

	// Get the list of hidden plugins
	fetch("/rest/maintenance/latest/gethiddenplugins")
		.then((response) => {
			if (!response.ok) {
				throw new Error("Error while fetching hidden plugin list");
			}

			return response.text();
		})
		.then((response) => {
		    // When no plugin should be hidden
		    if (response === "") {
		        return;
		    }

			// Split by new line
			const plugins = response.split("\n");

			plugins.forEach((plugin) => {
				// Add to the list
				hiddenPlugins.push(plugin);
				console.log("Added plugin to the list: ", plugin);
			});
		})
		.catch((error) => {
			console.error(error);
		});

	// Parse out the container to watch for changes
	const pluginList = document.getElementById("upm-manage-container");

	if (pluginList) {
		// Listen to everything
		const config = {
			childList: true,
			attributes: true,
			characterData: true,
			subtree: true,
		};

		const callback = function (mutationsList, observer) {
			// There was a change, hide all plugins who are on the list
			document
				.querySelectorAll("div.upm-plugin.user-installed")
				.forEach((plugin) => {
					hiddenPlugins.forEach((hiddenplugin) => {
						if (plugin.getAttribute("data-key") === hiddenplugin) {
							plugin.remove();
						}
					});
				});
		};

		const observer = new MutationObserver(callback);
		observer.observe(pluginList, config);
	} else {
		console.log("upm-manage-container not found");
	}
});
