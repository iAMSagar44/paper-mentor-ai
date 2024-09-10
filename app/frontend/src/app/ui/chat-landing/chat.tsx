"use client";

import React, { useState, useEffect, useRef } from "react";
import styles from "./chat.module.css";
import { fetchEventSource } from "@microsoft/fetch-event-source";
import { nanoid } from "nanoid";
import { Message } from "./Message";
import { MessageProps } from "@/app/lib/definitions";
import ChatLoading from "./ChatLoading";
import { EventData } from "@/app/lib/definitions";

const StreamChat = () => {
  const [userInput, setUserInput] = useState("");
  const [messages, setMessages] = useState<MessageProps[]>([]);
  const [inputDisabled, setInputDisabled] = useState<boolean>(false);
  const [citations, setCitations] = useState<string[]>([]);
  const [streaming, isStreaming] = useState<boolean>(false);

  // automatically scroll to bottom of chat
  const messagesEndRef = useRef<HTMLDivElement | null>(null);
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };
  useEffect(() => {
    if (messages.length > 0) {
      scrollToBottom();
    }
  }, [messages]);

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setInputDisabled(true);
    if (!userInput.trim()) return;
    setMessages((prevMessages) => [
      ...prevMessages,
      { role: "user", text: userInput },
    ]);
    sendMessage(userInput);
    setUserInput("");

    scrollToBottom();
  };

  async function sendMessage(message: string) {
    setCitations([]);
    let first: boolean = true;
    try {
      isStreaming(true);
      await fetchEventSource("/api/stream", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          question: message,
        }),
        onmessage(event) {
          isStreaming(false);
          //console.log('Received event data in the Chat component:', event.data);
          const textChunk: string = (JSON.parse(event.data) as EventData).token;
          if (textChunk.includes("DONE") && textChunk.length == 4) {
            const files: string[] = (JSON.parse(event.data) as EventData)
              .fileNames;
            //console.log('Received files in the Chat component:', files);
            setCitations(files);
          }
          if (first && textChunk) {
            setMessages((prevMessages) => [
              ...prevMessages,
              { role: "assistant", text: textChunk },
            ]);
            first = false;
          } else {
            if (!textChunk.includes("DONE")) {
              appendToLastMessage(textChunk);
            }
          }
        },
        async onopen(res) {
          if (res.ok && res.status === 200) {
            console.log("Connection made ", res);
            return;
          } else if (
            res.status >= 400 &&
            res.status < 500 &&
            res.status !== 429
          ) {
            console.log("Client side error ", res);
          }
        },
        onclose() {
          isStreaming(false);
        },
        onerror(error) {
          console.error("Unexpexted service error:", error);
          throw new Error("Unexpexted service error");
        },
      });
    } catch (error) {
      isStreaming(false);
      console.error("Error fetching messages:", error);
      setMessages((prevMessages) => [
        ...prevMessages,
        {
          role: "assistant",
          text: "Sorry, I'm having trouble connecting to the server. Please try again later.",
        },
      ]);
    }
    setInputDisabled(false);
  }

  const appendToLastMessage = (text: string) => {
    setMessages((prevMessages) => {
      const lastMessage = prevMessages[prevMessages.length - 1];
      const updatedLastMessage = {
        ...lastMessage,
        text: lastMessage.text + text,
      };
      return [...prevMessages.slice(0, -1), updatedLastMessage];
    });
  };

  async function onCitationClicked(citation: string) {
    window.open(`https://arxiv.org/pdf/${citation}`, "_blank");
  }

  return (
    <div className={styles.chatContainer}>
      <div className={styles.messages}>
        {messages.map((msg, index) => (
          <Message key={index} message={msg} />
        ))}
        {streaming && <ChatLoading />}
        {!!citations.length && (
          <div>
            <h4 className="text-black">References:</h4>
            <ul role="list" className="divide-y divide-gray-100">
              {citations.map((citation, index) => (
                <a
                  key={index}
                  title={citation.substring(0, citation.indexOf("##"))}
                  className="underline hover:bg-sky-500 hover:ring-sky-500 cursor-pointer text-black"
                  onClick={() =>
                    onCitationClicked(
                      citation.substring(citation.indexOf("##") + 2)
                    )
                  }
                >
                  {`${++index}. ${citation.substring(
                    0,
                    citation.indexOf("##")
                  )} \n`}
                </a>
              ))}
            </ul>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>
      <form
        onSubmit={handleSubmit}
        className={`${styles.inputForm} ${styles.clearfix}`}
      >
        <input
          type="text"
          className={styles.input}
          value={userInput}
          onChange={(e) => setUserInput(e.target.value)}
          placeholder="Enter your question"
        />
        <button
          type="submit"
          className={styles.button}
          disabled={inputDisabled}
        >
          Send
        </button>
      </form>
    </div>
  );
};

export default StreamChat;
