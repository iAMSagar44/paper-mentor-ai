"use client"

import React, { useState, useEffect, useRef } from "react";
import styles from "./chat.module.css";
import { nanoid } from "nanoid";
import { Message } from "./Message";
import { MessageProps } from "@/app/lib/definitions";
import ChatLoading from "./ChatLoading";
import { useFormStatus } from "react-dom";
import { aiAssistant } from "@/app/lib/actions";
import { EventData } from "@/app/lib/definitions";

const chatId = nanoid();

const AIChat = () => {
    const [userInput, setUserInput] = useState("");
    const [messages, setMessages] = useState<MessageProps[]>([]);
    const [citations, setCitations] = useState<string[]>([]);
    const [streaming, isStreaming] = useState<boolean>(false);
    const { pending } = useFormStatus();

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
        isStreaming(true);
        setCitations([]);
        try {
            const data: EventData = await aiAssistant(message);
            if (data.fileNames.length > 0) {
                const files: string[] = data.fileNames;
                //console.log('Received files in the Chat component:', files);
                setCitations(files);
            }
            setMessages((prevMessages) => [
                ...prevMessages,
                { role: "assistant", text: data.token }
            ]);
        } catch (error) {
            console.error("An error occurred while sending the message:", error);
            setMessages((prevMessages) => [
                ...prevMessages,
                { role: "assistant", text: "I am unable to respond to your message atm. Please try again later" }
            ]);
        } finally {
            isStreaming(false);
        }
    }

    async function onCitationClicked(citation: string) {
        try {
            const relativePath = `/api/files/${citation}`;
            window.open(relativePath, '_blank');
        } catch (error) {
            console.error('Error fetching the file:', error);
        }
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
                        <h4 className="text-black">Files referred:</h4>
                        <ul role="list" className="divide-y divide-gray-100">
                            {citations.map((citation, index) => (
                                <a key={index} title={citation} className="underline hover:bg-sky-500 hover:ring-sky-500 cursor-pointer text-black"
                                    onClick={() => onCitationClicked(citation)}>
                                    {`${++index}. ${citation} \n`}
                                </a>
                            ))}
                        </ul>
                    </div>
                )
                }
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
                    disabled={pending}
                >
                    Send
                </button>
            </form>
        </div>
    );
}

export default AIChat;