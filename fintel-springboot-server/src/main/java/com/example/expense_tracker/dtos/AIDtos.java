package com.example.expense_tracker.dtos;

import jakarta.validation.constraints.NotBlank;

public class AIDtos {

    public static class ChatRequest {
        private String systemPrompt;

        @NotBlank(message = "userMessage is required")
        private String userMessage;

        public ChatRequest() {}
        public ChatRequest(String systemPrompt, String userMessage) {
            this.systemPrompt = systemPrompt;
            this.userMessage = userMessage;
        }

        public String getSystemPrompt() { return systemPrompt; }
        public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

        public String getUserMessage() { return userMessage; }
        public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
    }

    public static class ChatResponse {
        private String reply;

        public ChatResponse() {}
        public ChatResponse(String reply) { this.reply = reply; }

        public String getReply() { return reply; }
        public void setReply(String reply) { this.reply = reply; }
    }

    public static class VisionRequest {
        @NotBlank(message = "base64Image is required")
        private String base64Image;

        private String mimeType = "image/jpeg";

        @NotBlank(message = "prompt is required")
        private String prompt;

        public VisionRequest() {}
        public VisionRequest(String base64Image, String mimeType, String prompt) {
            this.base64Image = base64Image;
            this.mimeType = mimeType;
            this.prompt = prompt;
        }

        public String getBase64Image() { return base64Image; }
        public void setBase64Image(String base64Image) { this.base64Image = base64Image; }

        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
    }

    public static class VisionResponse {
        private String data;

        public VisionResponse() {}
        public VisionResponse(String data) { this.data = data; }

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    public static class TranscribeRequest {
        @NotBlank(message = "base64Audio is required")
        private String base64Audio;

        private String mimeType = "audio/wav";

        public TranscribeRequest() {}
        public TranscribeRequest(String base64Audio, String mimeType) {
            this.base64Audio = base64Audio;
            this.mimeType = mimeType;
        }

        public String getBase64Audio() { return base64Audio; }
        public void setBase64Audio(String base64Audio) { this.base64Audio = base64Audio; }

        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    }

    public static class TranscribeResponse {
        private String transcript;

        public TranscribeResponse() {}
        public TranscribeResponse(String transcript) { this.transcript = transcript; }

        public String getTranscript() { return transcript; }
        public void setTranscript(String transcript) { this.transcript = transcript; }
    }

    public static class VoiceRequest {
        @NotBlank(message = "text is required")
        private String text;

        public VoiceRequest() {}
        public VoiceRequest(String text) { this.text = text; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
