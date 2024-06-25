let map;
let routes = [];
let currentRouteLayers = [];
let selectedMode = "foot";
let overlayVisible = false;
let activityLayers = [];
let markers = [];
let accessibilityMode = false;
let routeColors = {}; // Store colors for each busPathId
const predefinedColors = [
  'orange', '#FF5733', '#33FF57', '#3357FF', '#FF33A1', '#FF8C33', '#33FFBD', '#8D33FF',
  '#FFD133', '#33FF7A', '#FF3333', '#33D1FF', '#7A33FF', '#FF7A33', '#33FFEC', '#D1FF33',
  '#FF33E1', '#33FF33', '#336CFF', '#FF3357', '#33A1FF'
];
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
      if (route.mode === "transit") {
        routeItem.innerHTML = `
          <span class="route-details">
            <strong>${route.details}</strong><br>
            Time: ${route.time}<br>
            ${getModeIcon(route.mode)}
          </span>
          <img src="trash.svg" alt="Delete" class="trash-icon" onclick="deleteRoute(event, ${route.id})">
        `;
      } else {
        routeItem.innerHTML = `
          <span class="route-details">
            <strong>${route.details}</strong><br>
            Time: ${route.time}<br>
            Distance: ${route.distance}<br>
            ${getModeIcon(route.mode)}
          </span>
          <img src="trash.svg" alt="Delete" class="trash-icon" onclick="deleteRoute(event, ${route.id})">
        `;
      }
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
      case "transit":
        return '<img src="bus.svg" alt="Transit" class="icon"> Transit';
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

    document.querySelectorAll(".route-item").forEach((item) => item.classList.remove("active"));
    document.querySelector(`.route-item[data-route-id='${routeId}']`).classList.add("active");

    let routeInstructions = document.getElementById("route-instructions");

    document.getElementById("route-name").innerText = route.details;
    document.getElementById("route-time").innerText = `Time: ${route.time}`;

    if (route.mode === "transit") {
      document.getElementById("route-distance").style.display = "none";
    } else {
      document.getElementById("route-distance").style.display = "block";
      document.getElementById("route-distance").innerText = `Distance: ${route.distance}`;
    }

    // Clear previous instructions
    routeInstructions.innerHTML = "";

    // Add start walk image and first step
    let startStepDiv = document.createElement("div");
    startStepDiv.className = "route-step";
    startStepDiv.innerHTML = `
      <div class="route-timeline-dot">
        <img src="walk.svg" alt="Walking" class="route-icon">
      </div>
      <div class="route-step-details">
        <strong>Start Walking</strong><br>
        Walk to the first point
      </div>
    `;
    routeInstructions.appendChild(startStepDiv);

    // Add a solid line after the start walking step
    let startLineDiv = document.createElement("div");
    startLineDiv.className = "route-timeline-line";
    startLineDiv.innerHTML = `<div class="route-solid-line"></div>`;
    routeInstructions.appendChild(startLineDiv);

    if (route.mode === "bus" && route.stops) {
      route.stops.forEach((step, index) => {
        let stepDiv = document.createElement("div");
        stepDiv.className = "route-step";
        stepDiv.innerHTML = `
          <div class="route-timeline-dot">
            <img src="bus.svg" alt="Bus" class="route-icon">
          </div>
          <div class="route-step-details">
            <strong>${step.time}</strong><br>
            ${step.name}
          </div>
        `;
        routeInstructions.appendChild(stepDiv);

        if (index < route.stops.length - 1) {
          let lineDiv = document.createElement("div");
          lineDiv.className = "route-timeline-line";
          lineDiv.innerHTML = `<div class="route-solid-line"></div>`;
          routeInstructions.appendChild(lineDiv);
        }
      });
    }

    if (route.mode === "transit" && route.routes) {
      console.log("Transit routes:", route.routes);  // Debugging line to check routes array

      route.routes.forEach((routeDetail, index) => {
        console.log("Route detail:", routeDetail);  // Debugging line to check each route detail
        let transferDiv = document.createElement("div");
        transferDiv.className = "route-transfer-step";
        transferDiv.innerHTML = `
          <div class="route-timeline-dot">
            <img src="bus.svg" alt="Bus" class="route-icon">
          </div>
          <div class="route-step-details">
            <strong>Line: ${routeDetail.line}</strong><br>
            ${routeDetail.name}
          </div>
        `;
        routeInstructions.appendChild(transferDiv);

        if (index < route.routes.length - 1) {
          let lineDiv = document.createElement("div");
          lineDiv.className = "route-timeline-line";
          lineDiv.innerHTML = `<div class="route-solid-line"></div>`;
          routeInstructions.appendChild(lineDiv);
        }
      });
    }

    // Add a solid line before the final walk step
    let endWalkLineDiv = document.createElement("div");
    endWalkLineDiv.className = "route-timeline-line";
    endWalkLineDiv.innerHTML = `<div class="route-solid-line"></div>`;
    routeInstructions.appendChild(endWalkLineDiv);

    // Add end walk image and last step
    let endStepDiv = document.createElement("div");
    endStepDiv.className = "route-step";
    endStepDiv.innerHTML = `
      <div class="route-timeline-dot">
        <img src="walk.svg" alt="Walking" class="route-icon">
      </div>
      <div class="route-step-details">
        <strong>End Walking</strong><br>
        ${route.toPostal}
      </div>
    `;
    routeInstructions.appendChild(endStepDiv);

    // Add end icon after the final walk
    let endIconDiv = document.createElement("div");
    endIconDiv.className = "route-step";
    endIconDiv.innerHTML = `
      <div class="route-timeline-dot">
        <img src="end.png" alt="End" class="route-icon">
      </div>
      <div class="route-step-details">
        <strong>Destination</strong><br>
        ${route.toPostal}
      </div>
    `;
    routeInstructions.appendChild(endIconDiv);

    document.getElementById("route-details-container").style.display = "block";
    document.getElementById("route-details-container").style.height = "auto";
    document.getElementById("route-details-container").style.maxHeight = "calc(100% - 100px)"; /* Allow for spacing */
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
      const busPathIdToColor = new Map();
      let nextColorIndex = 1; // Start from 1 because 0 is reserved for null

      if (mode === "transit") {
        let currentSegmentLatLngs = [];
        let currentBusPathId = routeCoordinates[0][2];

        for (let i = 0; i < routeCoordinates.length; i++) {
          const coord = routeCoordinates[i];
          currentSegmentLatLngs.push([coord[0], coord[1]]);

          const nextBusPathId = i === routeCoordinates.length - 1 ? null : routeCoordinates[i + 1][2];
          
          if (nextBusPathId !== currentBusPathId || i === routeCoordinates.length - 1) {
            let color;
            if (currentBusPathId === null) {
              color = 'orange';
            } else {
              if (!busPathIdToColor.has(currentBusPathId)) {
                busPathIdToColor.set(currentBusPathId, predefinedColors[nextColorIndex % predefinedColors.length]);
                nextColorIndex++;
              }
              color = busPathIdToColor.get(currentBusPathId);
            }

            const layer = L.polyline(currentSegmentLatLngs, { color: color }).addTo(map);
            currentRouteLayers.push(layer);
            currentSegmentLatLngs = [];
            currentBusPathId = nextBusPathId;
          }
        }
      } else {
        let currentSegmentLatLngs = [];
        let currentType = routeCoordinates[0][2];

        for (let i = 0; i < routeCoordinates.length; i++) {
          const coord = routeCoordinates[i];
          currentSegmentLatLngs.push([coord[0], coord[1]]);

          const nextType = i === routeCoordinates.length - 1 ? null : routeCoordinates[i + 1][2];
          
          if (nextType !== currentType || i === routeCoordinates.length - 1) {
            let color = "#1A73E8"; // Default color
            if (mode === "bus") {
              color = currentType === 0 ? "orange" : "blue"; // Change color based on type
            }

            const layer = L.polyline(currentSegmentLatLngs, { color: color }).addTo(map);
            currentRouteLayers.push(layer);
            currentSegmentLatLngs = [];
            currentType = nextType;
          }
        }
      }

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
      toggleSEAILayers(true);
    } else {
      leftPanel.classList.remove("hide");
      accessibilityPanel.classList.remove("show");
      toggleSEAILayers(false);
    }
  } catch (error) {
    displayError("Error toggling overlay: " + error.message);
  }
}

function toggleSEAILayers(show) {
    try {
        if (show) {
            fetchSEAIData().then(data => {
                data.forEach(entry => {
                    const { Lat, Lon, SEAI } = entry;
                    if (Lat && Lon) {
                        var color = getColorBySEAI(SEAI);
                        var marker = L.circleMarker([Lat, Lon], {
                            color: color,
                            radius: 8,
                            fillOpacity: 0.8
                        }).addTo(map);
                        activityLayers.push(marker);
                    } else {
                        console.error("Invalid Lat/Lon values for entry:", entry);
                    }
                });
            }).catch(error => {
                displayError("Error loading SEAI data: " + error.message);
            });
        } else {
            activityLayers.forEach(layer => {
                map.removeLayer(layer);
            });
            activityLayers = [];
        }
    } catch (error) {
        displayError("Error toggling SEAI layers: " + error.message);
    }
}

async function fetchSEAIData() {
    try {
        const response = await fetch('postalAccUpdated.csv');
        const csvText = await response.text();
        const data = Papa.parse(csvText, {
            header: true,
            dynamicTyping: true
        }).data;
        return data;
    } catch (error) {
        displayError("Error fetching SEAI data: " + error.message);
    }
}

document.addEventListener("DOMContentLoaded", (event) => {
  initializeMap();
});


function getColorByRange(range) {
  switch (range) {
    case '0-20':
      return 'red';
    case '21-50':
      return 'orange';
    case '51-100':
      return 'yellow';
    case '101-150':
      return 'lightgreen';
    case '151-200':
      return 'green';
    case '201-300':
      return 'blue';
    case '301-400':
      return 'purple';
    case '401-500':
      return 'pink';
    case '501-600':
      return 'magenta';
    case '601+':
      return 'black';
    default:
      return 'grey';
  }
}

function getColorBySEAI(SEAI) {
  try {
    if (SEAI >= 0 && SEAI <= 20) {
      return "red";
    } else if (SEAI > 20 && SEAI <= 50) {
      return "orange";
    } else if (SEAI > 50 && SEAI <= 100) {
      return "yellow";
    } else if (SEAI > 100 && SEAI <= 150) {
      return "lightgreen";
    } else if (SEAI > 150 && SEAI <= 200) {
      return "green";
    } else if (SEAI > 200 && SEAI <= 300) {
      return "lightblue";
    } else if (SEAI > 300 && SEAI <= 400) {
      return "blue";
    } else if (SEAI > 400 && SEAI <= 500) {
      return "purple";
    } else if (SEAI > 500 && SEAI <= 600) {
      return "magenta";
    } else if (SEAI > 600) {
      return "black";
    } else {
      return "grey";
    }
  } catch (error) {
    displayError("Error getting color by SEAI: " + error.message);
  }
}

function calculateAccessibilityDistribution(data) {
  const distribution = {
    "0-20": 0,
    "21-50": 0,
    "51-100": 0,
    "101-150": 0,
    "151-200": 0,
    "201-300": 0,
    "301-400": 0,
    "401-500": 0,
    "501-600": 0,
    "601+": 0,
  };

  data.forEach(entry => {
    const { SEAI } = entry;
    if (SEAI >= 0 && SEAI <= 20) {
      distribution["0-20"]++;
    } else if (SEAI > 20 && SEAI <= 50) {
      distribution["21-50"]++;
    } else if (SEAI > 50 && SEAI <= 100) {
      distribution["51-100"]++;
    } else if (SEAI > 100 && SEAI <= 150) {
      distribution["101-150"]++;
    } else if (SEAI > 150 && SEAI <= 200) {
      distribution["151-200"]++;
    } else if (SEAI > 200 && SEAI <= 300) {
      distribution["201-300"]++;
    } else if (SEAI > 300 && SEAI <= 400) {
      distribution["301-400"]++;
    } else if (SEAI > 400 && SEAI <= 500) {
      distribution["401-500"]++;
    } else if (SEAI > 500 && SEAI <= 600) {
      distribution["501-600"]++;
    } else if (SEAI > 600) {
      distribution["601+"]++;
    }
  });

  return distribution;
}

function updatePieChart(distribution) {
  const total = Object.values(distribution).reduce((sum, value) => sum + value, 0);
  const pieChart = document.querySelector('.pie-chart');

  // Clear previous segments
  while (pieChart.firstChild) {
    pieChart.removeChild(pieChart.firstChild);
  }

  let cumulativePercentage = 0;

  Object.entries(distribution).forEach(([range, count], index) => {
    if (count > 0) {
      const percentage = (count / total) * 100;
      const segment = document.createElement('div');
      segment.classList.add('pie-chart-segment');
      segment.classList.add(`segment-${range.replace('+', '-plus').replace('-', '-')}`);
      
      const color = getColorByRange(range);
      const segmentAngle = (percentage / 100) * 360;
      segment.style.background = `conic-gradient(${color} ${segmentAngle}deg, transparent 0)`;
      segment.style.transform = `rotate(${cumulativePercentage}deg)`;
      cumulativePercentage += segmentAngle;
      
      pieChart.appendChild(segment);
    }
  });
}

fetchSEAIData().then(data => {
  const distribution = calculateAccessibilityDistribution(data);
  updatePieChart(distribution);
}).catch(error => {
  displayError("Error updating pie chart: " + error.message);
});
