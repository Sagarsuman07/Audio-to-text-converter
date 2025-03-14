# Audio to Text Converter

This project allows you to upload an audio file or record audio and convert it into text using AssemblyAI's API. You can also translate the transcribed text to different languages using Google Cloud Translation API.

## Prerequisites

Before running the application, ensure the following prerequisites are met:

- **Java**: JDK 8 or later installed.
- **Maven**: Installed and set up to handle project dependencies.
- **AssemblyAI API Key**: For transcription services.
- **Google Cloud Account**: For language translation services.

## Set AssemblyAI API Key in Environment Variable

1. **Create an account at [AssemblyAI](https://www.assemblyai.com/)** if you don't already have one.
2. **Obtain your API Key** from the AssemblyAI dashboard.
3. **Set the API Key as an environment variable**:
   - On Windows:
     1. Open **Command Prompt** (cmd).
     2. Run the following command:
        ```bash
        setx API_KEY "your-assemblyai-api-key"
        ```
   - On macOS/Linux:
     1. Open a terminal window.
     2. Add the following line to your `.bashrc` or `.zshrc` file:
        ```bash
        export API_KEY="your-assemblyai-api-key"
        ```
     3. Then, run the following command to load the new environment variable:
        ```bash
        source ~/.bashrc  # or ~/.zshrc for zsh users
        ```

This environment variable will be used by the application to authenticate and interact with the AssemblyAI API.

---

## Setting Up Google Cloud Account

### 1. Create a Google Cloud Account
   - Visit [Google Cloud](https://cloud.google.com/) and sign up for an account if you don't have one.
   - You may need to enter billing information, but there is a free tier with limited usage.

### 2. Create a New Project
   - After signing in to Google Cloud Console, click on **Select a Project** at the top.
   - Click **New Project** and enter a name for your project (e.g., `audio-to-text-converter`).
   - Click **Create**.

### 3. Enable Google Cloud Translation API
   - Go to the [Google Cloud Console](https://console.cloud.google.com/).
   - In the search bar, type **Translation API** and select it.
   - Click **Enable** to activate the Google Cloud Translation API for your project.

### 4. Create a Service Account
   - In the Google Cloud Console, navigate to **IAM & Admin** → **Service Accounts**.
   - Click on **Create Service Account**.
   - Fill out the **Service Account Details**:
     - **Service Account Name**: `translation-service-account`
     - **Role**: Select `Project` → `Editor` or `Viewer`. The `Editor` role allows full access to the project, while `Viewer` gives only read access (choose based on your needs).
     - Leave the **Key** section as **JSON** (this is important for authentication).
   - Click **Done**.

### 5. Download the Service Account Key in JSON Format
   - After creating the service account, you'll be presented with the option to download the service account key. Click **Create Key** and select **JSON**.
   - Save the downloaded `.json` file securely on your machine.

### 6. Set the Environment Variable for Authentication
   - The downloaded `.json` file is required for authenticating your application with Google Cloud services.
   - Set the **GOOGLE_APPLICATION_CREDENTIALS** environment variable to the path of the JSON file.
     - On **Windows**:
       1. Open **Command Prompt** (cmd) and run the following command:
          ```bash
          setx GOOGLE_APPLICATION_CREDENTIALS "C:\path\to\your\service-account-file.json"
          ```
     - On **macOS/Linux**:
       1. Open a terminal window and run the following command:
          ```bash
          export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/service-account-file.json"
          ```
       2. To make this change permanent, add the above line to your `.bashrc` or `.zshrc` file, and then run:
          ```bash
          source ~/.bashrc  # or ~/.zshrc for zsh users
          ```

This environment variable tells the application where to find the service account credentials needed for authentication with Google Cloud APIs.

---

## Running the Application

1. **Clone the repository** to your local machine:
   ```bash
   git clone https://github.com/your-repository/audio-to-text-converter.git
   cd audio-to-text-converter
