  
  
       
  
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
	    let transcriptionText="";
	    
	
        
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
            transcriptionText=data.transcription;
            
            resultText.innerText = transcriptionText;
            resultContainer.style.display = "block";
            
            
            
            
        });
        
        
        
        
        
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
            document.getElementById('originaltextbuttoncontainer').style.display='block';

           
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

        document.getElementById("record").onclick = function () {
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



    

    

     
     

        

        // Reset Selection
        resetButton.addEventListener("click", function() {
            fileInput.value = "";
            fileInput.disabled = false;
            recordButton.disabled = false;
            uploadButton.disabled = true;
            resetButton.style.display = "none";
        });
        
        
        
     // Edit, Save and Download functionality
        document.getElementById("editButton").addEventListener("click", function() {
        	
        	document.getElementById("downloadButton").disabled=true;
        	document.getElementById("translateButton").disabled=true; 
        	//disable magic button
            document.getElementById("formatBtn").disabled=true;
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
        	
        	document.getElementById("downloadButton").disabled=false;
        	document.getElementById("formatBtn").disabled=false;
        	document.getElementById("translateButton").disabled=false;
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