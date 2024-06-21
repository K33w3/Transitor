let map;
let routes = [];
let currentRouteLayers = [];
let selectedMode = "foot";
let overlayVisible = false;
let activityLayers = [];
let markers = [];
let accessibilityMode = false;

/*
Initialize the map with the default view set to Maastricht.
*/
function initializeMap() {
  try {
    map = L.map("map").setView([50.851368, 5.690973], 13); // Default view set to Maastricht

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map);
  } catch (error) {
    displayError("Error initializing the map: " + error.message);
  }
}

/* 

Display error message in a notification banner at the bottom of the page. 
This includes the message parameter that is displayed.
*/
function displayError(message) {
  const container = document.getElementById("notification-container");
  const notification = document.createElement("div");
  notification.className = "notification";
  notification.innerText = message;

  container.appendChild(notification);
  container.style.display = "block";

  setTimeout(() => {
    notification.classList.add("show");
  }, 10);

  setTimeout(() => {
    notification.classList.remove("show");
    notification.classList.add("hide");
  }, 3000);

  setTimeout(() => {
    container.removeChild(notification);
    if (container.childElementCount === 0) {
      container.style.display = "none";
    }
  }, 3500);
}

/* 
Select the mode of transportation for the route planning. 
This includes the decision of the range slider if it is or is not disabled.
*/

function selectMode(mode) {
  try {
    selectedMode = mode;
    document
      .querySelectorAll(".transportation-options button")
      .forEach((button) => button.classList.remove("active"));

    const transportationButton = document.querySelector(
      `.transportation-options button[onclick="selectMode('${mode}')"]`
    );
    if (transportationButton) {
      transportationButton.classList.add("active");
    }
    document
      .querySelectorAll(".transit-button-container button")
      .forEach((button) => button.classList.remove("active"));

    if (mode === "transit") {
      const transitButton = document.querySelector(
        `.transit-button-container button[onclick="selectMode('${mode}')"]`
      );
      if (transitButton) {
        transitButton.classList.add("active");
      }
    }
    document.getElementById("range-slider").disabled =
      mode !== "bus" && mode !== "transit";
  } catch (error) {
    displayError("Error selecting mode: " + error.message);
  }
}

/*
Plan the route based on the postal codes and occasionally range entered by the user.
This sends the postal codes and the selected mode to the Java backend to calculate the route.
This includes the error message if the postal codes are not entered or if the route is not found.
*/

function planRoute() {
  try {
    var fromPostal = document.getElementById("from-postal").value;
    var toPostal = document.getElementById("to-postal").value;
    var range = document.getElementById("range-slider").value;
    if (fromPostal && toPostal) {
      window.javaUI.createJsonJavascript(
        fromPostal,
        toPostal,
        selectedMode,
        range
      );
    } else {
      displayError("Please enter both from and to postal codes.");
    }
  } catch (error) {
    displayError("No route found with this range. Please try again.");
  }
}

/*
Swap the postal codes entered by the user. Here is the logic behind the swap button.
*/

function swapInputs() {
  try {
    var fromPostal = document.getElementById("from-postal");
    var toPostal = document.getElementById("to-postal");
    var temp = fromPostal.value;
    fromPostal.value = toPostal.value;
    toPostal.value = temp;
  } catch (error) {
    displayError("Error swapping inputs: " + error.message);
  }
}

/*
Receive the route details from the Java backend and parse the JSON response.
Calls the updateRouteList function to update the route list and clearInputs function to clear the inputs.
Calls the showRouteDetails function to show the route details on the map as well as draw the route on the map.
Calls the displayError function if there is an error parsing the JSON response.
*/

function receiveRouteDetails(routeDetailsJson) {
  try {
    let route = JSON.parse(routeDetailsJson);
    route.id = routes.length + 1;
    route.time = route.time + " mins";
    route.distance = route.distance + " m";
    route.coordinates = JSON.parse(route.coordinates); // Parse coordinates string to JavaScript array
    routes.push(route);
    updateRouteList();
    clearInputs();
    showRouteDetails(route.id);
  } catch (error) {
    displayError("Error parsing JSON response: " + error.message);
  }
}

/*
Clears the inputs entered by the user. This includes the postal codes entered by the user. This occurs after every route drawing.
*/

function clearInputs() {
  try {
    document.getElementById("from-postal").value = "";
    document.getElementById("to-postal").value = "";
  } catch (error) {
    displayError("Error clearing inputs: " + error.message);
  }
}

/*
Update the route list with the route details. This includes the route details, time, distance, and mode of transportation.
*/

function updateRouteList() {
  try {
    var routeList = document.getElementById("route-list");
    routeList.innerHTML = "";
    routes.forEach((route) => {
      var routeItem = document.createElement("div");
      routeItem.className = "route-item";
      routeItem.setAttribute("data-route-id", route.id);
      routeItem.innerHTML = `
                <span class="route-details">
                    <strong>${route.details}</strong><br>
                    Time: ${route.time}<br>
                    Distance: ${route.distance}<br>
                    ${getModeIcon(route.mode)}
                </span>
                <img src="trash.svg" alt="Delete" class="trash-icon" onclick="deleteRoute(event, ${
                  route.id
                })">
            `;
      routeItem.querySelector(".route-details").onclick = function () {
        showRouteDetails(route.id);
      };
      routeList.appendChild(routeItem);
    });
  } catch (error) {
    displayError("Error updating route list: " + error.message);
  }
}

/* 
Get the mode icon based on the mode of transportation selected by the user.
*/

function getModeIcon(mode) {
  try {
    switch (mode) {
      case "foot":
        return '<img src="walk.svg" alt="Walking" class="icon"> Walking';
      case "bike":
        return '<img src="bike.png" alt="Biking" class="icon"> Biking';
      case "bus":
        return '<img src="bus.svg" alt="Bus" class="icon"> Bus';
      case "aerial":
        return '<img src="plane.png" alt="Aerial" class="icon"> Aerial';
      default:
        return "";
    }
  } catch (error) {
    displayError("Error getting mode icon: " + error.message);
  }
}

/*
Delete the route based on the route ID. This includes the event and the route ID.
This removes the route from the route list and clears the map. If there are no routes, the route details container is hidden.
*/

function deleteRoute(event, routeId) {
  try {
    event.stopPropagation();
    routes = routes.filter((route) => route.id !== routeId);
    updateRouteList();
    clearMap();
    if (routes.length > 0) {
      showRouteDetails(routes[0].id);
    } else {
      document.getElementById("route-details-container").style.display = "none";
    }
  } catch (error) {
    displayError("Error deleting route: " + error.message);
  }
}

/*
Show the route details based on the route ID. This includes the route ID.
This highlights the selected route in the route list and displays the route details on the map.
Special instructions are displayed for bus and transit routes, where the stops are displayed.
*/

function showRouteDetails(routeId) {
  try {
    let route = routes.find((r) => r.id === routeId);
    if (!route) return;

    document
      .querySelectorAll(".route-item")
      .forEach((item) => item.classList.remove("active"));
    document
      .querySelector(`.route-item[data-route-id='${routeId}']`)
      .classList.add("active");

    let routeInstructions = document.getElementById("route-instructions");

    document.getElementById("route-name").innerText = route.details;
    document.getElementById("route-time").innerText = `Time: ${route.time}`;
    document.getElementById(
      "route-distance"
    ).innerText = `Distance: ${route.distance}`;

    // Clear previous instructions
    routeInstructions.innerHTML = "";

    if (route.mode === "bus") {
      // Append the instructions
      route.stops.forEach((step, index) => {
        let stepDiv = document.createElement("div");
        stepDiv.className = "step";
        stepDiv.innerHTML = `
                    <div class="timeline-dot bus"></div>
                    <div class="step-details">
                        <strong>${step.time}</strong><br>
                        ${step.name}
                    </div>
                `;
        routeInstructions.appendChild(stepDiv);

        if (index < route.stops.length - 1) {
          let lineDiv = document.createElement("div");
          lineDiv.className = "timeline-line";
          lineDiv.innerHTML = `<div class="solid-line"></div>`;
          routeInstructions.appendChild(lineDiv);
        }
      });
    }

    document.getElementById("route-details-container").style.display = "block";
    document.getElementById("route-details-container").style.height = "auto";
    document.getElementById("route-details-container").style.maxHeight =
      "calc(100% - 100px)"; /* Allow for spacing */
    document.getElementById("route-details-container").style.overflowY = "auto";

    drawRouteOnMap(route.coordinates, route.mode);
  } catch (error) {
    displayError("Error showing route details: " + error.message);
  }
}

/*
Clear the map of all layers and markers. This includes the current route layers and markers.
*/

function clearMap() {
  try {
    currentRouteLayers.forEach((layer) => {
      map.removeLayer(layer);
    });
    currentRouteLayers = [];

    markers.forEach((marker) => map.removeLayer(marker));
    markers = [];
  } catch (error) {
    displayError("Error clearing map: " + error.message);
  }
}

const startIcon = L.divIcon({
  className: "start-marker",
  iconSize: [12, 12],
});

const endIcon = L.divIcon({
  className: "end-marker",
  iconSize: [12, 12],
});

/*
Draw the route on the map based on the route coordinates and mode of transportation.
This includes the route coordinates and mode of transportation.
*/

function drawRouteOnMap(routeCoordinates, mode) {
  try {
    clearMap(); // Clear previous layers and markers

    if (routeCoordinates.length > 0) {
      let segments = [];
      let currentSegment = {
        type: routeCoordinates[0][2],
        latLngs: [],
      };

      for (let i = 0; i < routeCoordinates.length; i++) {
        let coord = routeCoordinates[i];
        currentSegment.latLngs.push([coord[0], coord[1]]);

        // Check if the next segment has a different type or if it is the last coordinate
        if (
          i === routeCoordinates.length - 1 ||
          routeCoordinates[i + 1][2] !== currentSegment.type
        ) {
          segments.push(currentSegment);
          if (i !== routeCoordinates.length - 1) {
            currentSegment = {
              type: routeCoordinates[i + 1][2],
              latLngs: [],
            };
          }
        }
      }

      segments.forEach((segment) => {
        let color = "#1A73E8"; // Default color
        if (mode === "bus") {
          color = segment.type === 0 ? "orange" : "blue"; // Change color based on type
        }

        const layer = L.polyline(segment.latLngs, { color: color }).addTo(map);
        currentRouteLayers.push(layer); // Store the layer
      });

      const startPoint = [routeCoordinates[0][0], routeCoordinates[0][1]];
      const endPoint = [
        routeCoordinates[routeCoordinates.length - 1][0],
        routeCoordinates[routeCoordinates.length - 1][1],
      ];

      const startMarker = L.marker(startPoint, {
        icon: startIcon,
      })
        .addTo(map)
        .bindPopup("Start Point");
      const endMarker = L.marker(endPoint, {
        icon: endIcon,
      })
        .addTo(map)
        .bindPopup("End Point");

      markers.push(startMarker);
      markers.push(endMarker);

      map.fitBounds(
        L.polyline(
          routeCoordinates.map((coord) => [coord[0], coord[1]])
        ).getBounds()
      );
    }
  } catch (error) {
    displayError("Error drawing route on map: " + error.message);
  }
}

/*
Toggle the overlay panel. This includes the toggle slider, left panel, and accessibility panel.
*/

function toggleOverlay() {
  try {
    let toggleSlider = document.getElementById("toggle-slider");
    let leftPanel = document.getElementById("left-panel");
    let accessibilityPanel = document.getElementById("accessibility-panel");

    overlayVisible = !overlayVisible;
    accessibilityMode = !accessibilityMode;

    toggleSlider.classList.toggle("left", !overlayVisible);
    toggleSlider.classList.toggle("right", overlayVisible);
    document
      .querySelector(".toggle-container")
      .classList.toggle("active", overlayVisible);

    if (accessibilityMode) {
      leftPanel.classList.add("hide");
      accessibilityPanel.classList.add("show");
    } else {
      leftPanel.classList.remove("hide");
      accessibilityPanel.classList.remove("show");
    }
  } catch (error) {
    displayError("Error toggling overlay: " + error.message);
  }
}

/* 
Toggle the accessibility mode. This includes the accessibility mode button.
*/

document.addEventListener("DOMContentLoaded", (event) => {
  initializeMap();
});
