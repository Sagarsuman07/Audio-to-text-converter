



//Open the modal when "Download" button is clicked
document.getElementById("downloadButton").addEventListener("click", function() {
	let downloadModal = new bootstrap.Modal(document.getElementById("downloadModal"));
	downloadModal.show();
});

// Handle file download based on selected format
function downloadFile(format) {
	let content = document.getElementById("result").innerText;

	if (format === "txt") {
		downloadTXT(content);
	} else if (format === "pdf") {
		downloadPDF(content);
	} else if (format === "docx") {
		downloadDOCX(content);
	}

	// Close modal after selection
	let downloadModal = bootstrap.Modal.getInstance(document.getElementById("downloadModal"));
	downloadModal.hide();
}

function downloadTXT(content) {
	let blob = new Blob([content], { type: "text/plain" });
	let link = document.createElement("a");
	link.href = URL.createObjectURL(blob);
	link.download = "transcription.txt";
	document.body.appendChild(link);
	link.click();
	document.body.removeChild(link);
}

function downloadPDF(content) {
	let docDefinition = { content: content };
	pdfMake.createPdf(docDefinition).download("transcription.pdf");
}

function downloadDOCX(content) {
	let blob = new Blob([content], { type: "application/vnd.openxmlformats-officedocument.wordprocessingml.document" });
	saveAs(blob, "transcription.docx");
}









//Add event listener to the Format button
document.getElementById("formatBtn").addEventListener("click", function() {
	// Get the transcribed text
	const transcribedText = document.getElementById("result").innerText;


	// Create a form to submit the text
	const form = document.createElement('form');
	form.method = 'POST';
	form.action = '/api/text/format';
	form.target = '_blank'; // Open in new tab

	// Create hidden input for the text
	const textInput = document.createElement('input');
	textInput.type = 'hidden';
	textInput.name = 'rawText';
	textInput.value = transcribedText;

	// Add the input to the form
	form.appendChild(textInput);

	// Add the form to the document body
	document.body.appendChild(form);

	// Submit the form
	form.submit();

	// Remove the form from the document
	document.body.removeChild(form);

	// Hide loading message after a short delay
	setTimeout(() => {
		//document.getElementById("loading").style.display = "none";
	}, 1000);
});









//creating a global variable to store the transcription text
let transcriptionText = "";

// creating a global variable to store the trimmed audio blob
let trimmedAudioBlob = null;

let fileInput = document.getElementById("fileInput");
let recordButton = document.getElementById("record");
let uploadButton = document.getElementById("uploadButton");
let resetButton = document.getElementById("resetButton");

// Handle file selection
fileInput.addEventListener("change", function() {
	if (fileInput.files.length > 0) {
		recordButton.disabled = true; // Disable recording if file is chosen
		uploadButton.disabled = false; // Enable upload button
		resetButton.style.display = "block";
	}
});

// Handle form submission (file upload)
document.getElementById("uploadForm").addEventListener("submit", async function(event) {
	event.preventDefault();

	let formData = new FormData(this);
	let separateSpeakers = document.getElementById("separateSpeakersSwitch").checked;
	formData.append("separateSpeakers", separateSpeakers);

	let resultContainer = document.getElementById("transcriptionContainer");
	let resultText = document.getElementById("result");

	document.getElementById("loading").style.display = "block";
	resultContainer.style.display = "none";

	let response = await fetch("/api/audio/upload", {
		method: "POST",
		body: formData
	});

	let data = await response.json();

	document.getElementById("loading").style.display = "none";



	//storing the trancription text in a global variable
	transcriptionText = data.transcription;

	resultText.innerText = transcriptionText;
	resultContainer.style.display = "block";


	// convert Base64 audio to Blob and store in global variable
	if (data.audioBase64) {
		const byteCharacters = atob(data.audioBase64);
		const byteNumbers = new Array(byteCharacters.length).fill(0).map((_, i) => byteCharacters.charCodeAt(i));
		const byteArray = new Uint8Array(byteNumbers);
		trimmedAudioBlob = new Blob([byteArray], { type: 'audio/wav' }); // or 'audio/mp3' depending on your format
	}


});









// saving to database
const saveButton = document.getElementById("save2DB");
const modal = document.getElementById("authModal");
const closeModal = document.querySelector(".close");
const loginForm = document.getElementById("loginForm");
const signupForm = document.getElementById("signupForm");

document.getElementById("switchToLogin").onclick = () => {
	loginForm.style.display = "flex";
	signupForm.style.display = "none";
};

document.getElementById("switchToSignup").onclick = () => {
	loginForm.style.display = "none";
	signupForm.style.display = "flex";
};

closeModal.onclick = () => {
	modal.style.display = "none";
};

// Main SAVE button
saveButton.addEventListener("click", async () => {
	const isLoggedIn = await checkLoginStatus();
	if (isLoggedIn) {
		saveToDB(); // already logged in, go ahead and save
	} else {
		modal.style.display = "block"; // not logged in, open modal


		// Login
		loginForm.addEventListener("submit", async function(e) {
			e.preventDefault();

			const username = document.getElementById("loginUsername").value;
			const password = document.getElementById("loginPassword").value;

			const response = await fetch("/api/auth/login", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({ username, password }),
			});

			if (response.ok) {
				modal.style.display = "none";
				saveToDB();
				//alert("Login successful. Now click Save again to save your transcription.");
			} else {
				alert("Login failed");
			}
		});



		// Signup
		signupForm.addEventListener("submit", async function(e) {
			e.preventDefault();

			const username = document.getElementById("signupUsername").value;
			const password = document.getElementById("signupPassword").value;

			const response = await fetch("/api/auth/signup", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({ username, password }),
			});

			if (response.ok) {
				modal.style.display = "none";
				saveToDB();
				//alert("Signup successful. Now click Save again to save your transcription.");
			} else {
				alert("Signup failed");
			}
		});

	}
});





// Actual save call
async function saveToDB() {
	if (!trimmedAudioBlob || !transcriptionText) {
		alert("Audio or transcription not available");
		return;
	}


	// Prepare form data
	const formData = new FormData();
	formData.append("transcription", transcriptionText);
	formData.append("file", trimmedAudioBlob, "trimmed_audio.wav"); // 🔧 Must match backend param

	const response = await fetch("/api/transcription/save", {
		method: "POST",
		body: formData,
	});

	if (response.ok) {
		alert("Saved successfully");
	} else {
		const errorText = await response.text();
		alert("Failed to save: " + errorText);
	}
}





// Check login status
async function checkLoginStatus() {
	const res = await fetch("/api/auth/status");
	const data = await res.json();
	return data.loggedIn;
}
















// Add event listener for the Translate button
document.getElementById("translateButton").addEventListener("click", async function() {

	let translateButton = document.getElementById("translateButton");
	translateButton.disabled = true;  // Disable button



	// Get the selected language from the dropdown
	let selectedLanguage = document.getElementById("languageSelect").value;

	// Show loading indicator while waiting for the translation
	document.getElementById("loading2").style.display = "block";

	// Send a POST request to the backend with the transcription and selected language
	let response = await fetch("/api/audio/translate", {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			text: transcriptionText,  // Pass transcription text
			targetLanguage: selectedLanguage  // Pass selected language
		})
	});

	// Wait for the backend response
	let data = await response.json();

	// Hide loading indicator after translation is done
	document.getElementById("loading2").style.display = "none";



	// Update the transcription text with the translated text
	document.getElementById("result").innerText = data.translatedText;

	//hide the form
	document.getElementById('uploadtextForm').style.display = 'none';

	//show the original text button
	document.getElementById('originaltextbuttoncontainer').style.display = 'block';


});






// Add event listener for the show originaltext button
document.getElementById("originalTextButton").addEventListener("click", async function() {


	// Update the translated text with the transcripted text
	document.getElementById("result").innerText = transcriptionText;


	// hide the original text button
	document.getElementById('originaltextbuttoncontainer').style.display = 'none';


	// show the form
	document.getElementById('uploadtextForm').style.display = 'block';

	// Enable button after translation
	let translateButton = document.getElementById("translateButton");
	translateButton.disabled = false;


});










let rec, audioChunks = [], recording = false;
let micPermissionGranted = false;
let recordingTimeout = null; // to track 2-minute timeout

async function requestMicAndStartRecording() {
	try {
		const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

		micPermissionGranted = true;
		rec = new MediaRecorder(stream);
		audioChunks = [];

		rec.ondataavailable = e => {
			audioChunks.push(e.data);
			if (rec.state === "inactive") {
				let blob = new Blob(audioChunks, { type: 'audio/mp3' });
				sendData(blob);
			}
		};

		rec.start();
		recording = true;
		updateRecordButton();

		// Automatically stop after 2 minutes (120,000 ms)
		recordingTimeout = setTimeout(() => {
			if (recording && rec && rec.state !== "inactive") {
				rec.stop();
				recording = false;
				updateRecordButton();
				alert("Recording automatically stopped after 2 minutes.");
			}
		}, 120000); // 2 minutes = 120,000 milliseconds

	} catch (err) {
		console.warn("Microphone access denied or error:", err);
		micPermissionGranted = false;
		alert("Microphone access is denied. Please enable it in your browser settings and try again.");
	}
}

function updateRecordButton() {
	const recordBtn = document.getElementById("record");
	if (recording) {
		recordBtn.textContent = "Stop";
		recordBtn.style.backgroundColor = "blue";
	} else {
		recordBtn.textContent = "Record";
		recordBtn.style.backgroundColor = "red";
	}
}

function sendData(data) {
	document.getElementById("loading").style.display = "block";

	let formData = new FormData();
	formData.append("file", data, "recording.mp3");

	let separateSpeakers = document.getElementById("separateSpeakersSwitch").checked;
	formData.append("separateSpeakers", separateSpeakers);

	fetch("/api/audio/upload", {
		method: "POST",
		body: formData
	})
		.then(response => response.json())
		.then(result => {
			let transcriptionText = result.transcription;
			document.getElementById("result").innerText = transcriptionText || "No transcription available";
			document.getElementById("transcriptionContainer").style.display = "block";

			//show reset button 
			resetButton.style.display = "block";

			document.getElementById("loading").style.display = "none";
			document.getElementById('originaltextbuttoncontainer').style.display = 'none';
			document.getElementById('uploadtextForm').style.display = 'block';

			document.getElementById("translateButton").disabled = false;
		})
		.catch(error => {
			console.error("Error:", error);
			document.getElementById("result").innerText = "Error processing transcription.";
			document.getElementById("loading").style.display = "none";
		});
}

document.getElementById("record").onclick = function() {
	if (!recording) {
		requestMicAndStartRecording();
	} else {
		if (rec && rec.state !== "inactive") {
			rec.stop();
		}
		recording = false;
		clearTimeout(recordingTimeout); // stop auto-stop timer
		updateRecordButton();
	}
};









//logo effect
const endings = ["cribe", "late", "latify"];
const typeTarget = document.getElementById("typewriter");

let wordIndex = 0;
let charIndex = 0;
let isDeleting = false;

function type() {
	const currentWord = endings[wordIndex];
	const currentText = currentWord.substring(0, charIndex);

	typeTarget.textContent = currentText;

	let typingSpeed = 120;
	let deletingSpeed = 60;
	let holdDelay = 800;

	if (!isDeleting && charIndex < currentWord.length) {
		charIndex++;
		setTimeout(type, typingSpeed);
	} else if (isDeleting && charIndex > 0) {
		charIndex--;
		setTimeout(type, deletingSpeed);
	} else {
		// At the end of a word
		if (!isDeleting) {
			// If it's "latify", hold for longer (e.g. 2000ms)
			holdDelay = (currentWord === "latify") ? 2000 : 800;
		}

		isDeleting = !isDeleting;

		if (!isDeleting) {
			wordIndex = (wordIndex + 1) % endings.length;
		}

		setTimeout(type, holdDelay);
	}
}

document.addEventListener("DOMContentLoaded", type);


















async function showSavedTranscriptions() {
	const isLoggedIn = await checkLoginStatus();

	if (!isLoggedIn) {
		document.getElementById("authModal").style.display = "block";

		// If login/signup fails, switch back to transcribe
		const onClose = () => {
			document.getElementById("authModal").style.display = "none";
			showTab('transcribe');
		};

		document.querySelector(".close").onclick = onClose;

		document.getElementById("loginForm").onsubmit = async (e) => {
			e.preventDefault();
			const username = document.getElementById("loginUsername").value;
			const password = document.getElementById("loginPassword").value;
			const response = await fetch("/api/auth/login", {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ username, password })
			});
			if (response.ok) {
				document.getElementById("authModal").style.display = "none";

				loadSavedTranscriptions();

				transcriptionContainer.style.display = "none";
				transcriptionContainer2.style.display = "none";

				document.getElementById('saved-section').classList.remove('hidden');


			} else {
				alert("Login failed");
				onClose();
			}
		};

		document.getElementById("signupForm").onsubmit = async (e) => {
			e.preventDefault();
			const username = document.getElementById("signupUsername").value;
			const password = document.getElementById("signupPassword").value;
			const response = await fetch("/api/auth/signup", {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ username, password })
			});
			if (response.ok) {
				document.getElementById("authModal").style.display = "none";

				loadSavedTranscriptions();

				transcriptionContainer.style.display = "none";
				transcriptionContainer2.style.display = "none";

				document.getElementById('saved-section').classList.remove('hidden');
			} else {
				alert("Signup failed");
				onClose();
			}
		};

		return;
	}
	// If logged in
	else {
		loadSavedTranscriptions();

		transcriptionContainer.style.display = "none";
		transcriptionContainer2.style.display = "none";

		document.getElementById('saved-section').classList.remove('hidden');

	}
}

async function loadSavedTranscriptions() {
	const container = document.getElementById("saved-transcriptions-container");
	container.innerHTML = ""; // clear previous

	const response = await fetch("/api/transcription/list");
	if (!response.ok) {
		alert("Failed to load saved transcriptions");
		return;
	}
	const data = await response.json();

	const transcriptDisplay = document.getElementById("transcript-display");
	transcriptDisplay.classList.add("hidden");

	data.forEach(item => {
		const div = document.createElement("div");
		div.className = "p-4 bg-white rounded-lg shadow flex items-center justify-between";

		const audio = document.createElement("audio");
		audio.controls = true;
		audio.src = `data:audio/wav;base64,${item.audioBase64}`;
		audio.className = "mr-4";

		const btn = document.createElement("button");
		btn.textContent = "Show Transcript";
		btn.className = "bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600";
		btn.onclick = () => {
			transcriptDisplay.textContent = item.transcription;
			transcriptDisplay.classList.remove("hidden");
			transcriptDisplay.scrollIntoView({ behavior: "smooth" });
		};

		div.appendChild(audio);
		div.appendChild(btn);
		container.appendChild(div);
	});
}










//saved text toggle

function showTab(tab) {
	// Toggle button active state
	document.querySelectorAll('.tab').forEach(btn => {
		btn.classList.remove('active');
	});

	// Get container references
	const transcriptionContainer2 = document.getElementById("transcriptionContainer2");
	const transcriptionContainer = document.getElementById("transcriptionContainer");
	const ressDiv = document.getElementById("result");

	if (tab === 'transcribe') {
		document.querySelector('[onclick="showTab(\'transcribe\')"]').classList.add('active');
		document.getElementById('saved-section').classList.add('hidden');

		// Only show transcriptionContainer2 if result has content
		const resultText = ressDiv?.textContent?.trim();
		if (resultText && resultText.length > 0) {
			transcriptionContainer.style.display = "block";
		} else {
			transcriptionContainer.style.display = "none";
		}

		transcriptionContainer2.style.display = "block";
	}
	else if (tab === 'saved') {
		document.querySelector('[onclick="showTab(\'saved\')"]').classList.add('active');
		showSavedTranscriptions();


	}
}




// Reset Selection
resetButton.addEventListener("click", function() {
	fileInput.value = "";
	fileInput.disabled = false;
	recordButton.disabled = false;
	uploadButton.disabled = true;
	resetButton.style.display = "none";

	const transcont = document.getElementById("transcriptionContainer");
	const isVisible = window.getComputedStyle(transcont).display !== "none";

	if (isVisible) {
		transcriptionContainer.style.display = "none";
		document.getElementById("result").textContent = "";
	}
});



// Edit, Save and Download functionality
document.getElementById("editButton").addEventListener("click", function() {

	document.getElementById("downloadButton").disabled = true;
	document.getElementById("translateButton").disabled = true;
	//disable magic button
	document.getElementById("formatBtn").disabled = true;
	let resultDiv = document.getElementById("result");
	let currentText = resultDiv.innerText;

	// Create textarea element with the same content
	let textarea = document.createElement("textarea");
	textarea.className = "transcription-textarea";
	textarea.id = "editableText";
	textarea.value = currentText;

	// Replace the div with textarea
	resultDiv.style.display = "none";
	resultDiv.parentNode.insertBefore(textarea, resultDiv);

	// Show save button and hide edit button
	document.getElementById("editButton").style.display = "none";
	document.getElementById("saveButton").style.display = "inline-flex";


	// Focus on the textarea
	textarea.focus();
});

document.getElementById("saveButton").addEventListener("click", function() {

	document.getElementById("downloadButton").disabled = false;
	document.getElementById("formatBtn").disabled = false;
	document.getElementById("translateButton").disabled = false;
	let textarea = document.getElementById("editableText");
	let resultDiv = document.getElementById("result");

	// Update the text content
	resultDiv.innerText = textarea.value;
	transcriptionText = textarea.value; // Update the global variable

	// Remove textarea and show the result div
	textarea.remove();
	resultDiv.style.display = "block";

	// Show edit button and hide save button
	document.getElementById("editButton").style.display = "inline-flex";
	document.getElementById("saveButton").style.display = "none";
});