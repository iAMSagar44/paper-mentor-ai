"use client";

import React, { useState, useEffect, useRef } from "react";
import { fetchEventSource } from "@microsoft/fetch-event-source";
import { MessageProps, Conversation } from "@/app/lib/definitions";
import ChatLoading from "./ChatLoading";
import { EventData } from "@/app/lib/definitions";
import {
  Send,
  Menu,
  Plus,
  LogOut,
  LogIn,
  Bot,
  Trash2,
  MoreVertical,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import ReactMarkdown from "react-markdown";
import DOMPurify from "dompurify";
import rehypeRaw from "rehype-raw";
import { useRouter } from "next/navigation";
import { useAuth } from "@/app/lib/useAuth";
import Link from "next/link";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

const StreamChat = () => {
  const router = useRouter();
  const [userInput, setUserInput] = useState("");
  const [messages, setMessages] = useState<MessageProps[]>([
    { content: "Hello! How can I assist you today?", sender: "llm" },
  ]);
  const [inputDisabled, setInputDisabled] = useState<boolean>(false);
  const [citations, setCitations] = useState<string[]>([]);
  const [streaming, isStreaming] = useState<boolean>(false);
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [currentChatId, setCurrentChatId] = useState<string>("");
  const [newChat, setNewChat] = useState<boolean>(true);
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const [isSignOutVisible, setIsSignOutVisible] = useState(false);
  const [refreshConversations, setRefreshConversations] = useState(false);

  const { authenticated, user, loading, logout } = useAuth();

  const currentChat =
    conversations.find((conversation) => conversation?.id === currentChatId) ||
    null;

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

  // Fetch conversations from the server
  const fetchConversations = async () => {
    try {
      const response = await fetch("/api/conversations");
      if (response.status === 200) {
        const data = await response.json();
        //console.log("Data received from the route handler:", data);
        const conversations: Conversation[] = data.conversations;
        setConversations(conversations);
      }
      if (response.status === 401) {
        console.log("User is not authenticated");
        router.push("/login");
      }
      if (response.status === 500) {
        console.log("Failed to fetch conversations");
        throw new Error("Failed to fetch conversations");
      }
    } catch (error) {
      console.error("Error fetching conversations:", error);
    }
  };

  useEffect(() => {
    const fetchAndSetRecentChat = async () => {
      try {
        const response = await fetch("/api/conversations");
        if (response.status === 200) {
          //console.log("Success response from the server", response);
          const data = await response.json();
          const conversations: Conversation[] = data.conversations;
          setConversations(conversations);
          if (conversations.length > 0) {
            const recentChat = conversations.reduce((prev, current) =>
              new Date(prev.updatedAt) > new Date(current.updatedAt)
                ? prev
                : current
            );
            setCurrentChatId(recentChat.id);
            setNewChat(false);
            const response = await fetch(`/api/messages/${recentChat.id}`, {
              method: "GET",
              headers: {
                "Content-Type": "application/json",
              },
            });
            if (!response.ok) {
              throw new Error("Failed to fetch messages for the recent chat");
            }
            const data = await response.json();
            setMessages(data);
          } else {
            setMessages([
              { content: "Hello! How can I assist you today?", sender: "llm" },
            ]);
            setCitations([]);
            setCurrentChatId("");
          }
        }
        if (response.status === 401) {
          console.log("User is not authenticated");
          router.push("/login");
        }
        if (response.status === 500) {
          console.log("Failed to fetch conversations");
          throw new Error("Failed to fetch conversations");
        }
      } catch (error) {
        console.error("Error fetching conversations:", error);
      }
    };
    if (authenticated) {
      fetchAndSetRecentChat();
    }
  }, [authenticated, refreshConversations]);

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setInputDisabled(true);
    if (!userInput.trim()) return;
    setMessages((prevMessages) => [
      ...prevMessages,
      { sender: "user", content: userInput },
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
          isNewMessage: newChat,
          conversation_id: newChat ? Date.now().toString() : currentChatId,
        }),
        onmessage(event) {
          isStreaming(false);
          const textChunk: string = (JSON.parse(event.data) as EventData).token;
          if (textChunk.includes("DONE") && textChunk.length == 4) {
            const files: string[] = (JSON.parse(event.data) as EventData)
              .fileNames;
            setCitations(files);
            const conversation_id = (JSON.parse(event.data) as EventData)
              .conversation_id;
            setCurrentChatId(conversation_id);
          }
          if (first && textChunk) {
            setMessages((prevMessages) => [
              ...prevMessages,
              { sender: "llm", content: textChunk },
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
            //console.log("Connection made ", res);
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
          if (newChat && authenticated) {
            fetchConversations();
            setNewChat(false);
          }
          isStreaming(false);
        },
        onerror(error) {
          console.error("Unexpected service error:", error);
          throw new Error("Unexpected service error");
        },
      });
    } catch (error) {
      isStreaming(false);
      console.error("Error fetching messages:", error);
      setMessages((prevMessages) => [
        ...prevMessages,
        {
          sender: "llm",
          content:
            "Sorry, I'm having trouble connecting to the server. Please try again later.",
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
        content: lastMessage.content + text,
      };
      return [...prevMessages.slice(0, -1), updatedLastMessage];
    });
  };

  async function onCitationClicked(citation: string) {
    window.open(`https://arxiv.org/pdf/${citation}`, "_blank");
  }

  const handleNewChat = async (): Promise<void> => {
    setNewChat(true);
    setCitations([]);
    console.log("Current chat id is:", currentChatId);
    console.log("Messages of current chat id is:", messages);

    if (!newChat) {
      await storeChatMessages(currentChatId);
    }

    const newConversation: Conversation = {
      id: Date.now().toString(),
      title: `New Chat`,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    setCurrentChatId(newConversation.id);
    setMessages([
      {
        content: "Hello! How can I assist you today?",
        sender: "llm",
      },
    ]);
    setNewChat(true);
  };

  const handleChatSelect = async (chatId: string): Promise<void> => {
    console.log("Trigger: Current chat id is:", currentChatId);

    // Update the messages of the current chat in the server
    if (!newChat) {
      await storeChatMessages(currentChatId);
    }

    console.log("Selected chat:", chatId);
    setCurrentChatId(chatId);
    setNewChat(false);
    // Fetch chat messages from the server for the selected chatId.
    try {
      const response = await fetch(`/api/messages/${chatId}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch messages");
      }

      const data = await response.json();
      console.log(
        "Messages received from the server [client component]:",
        data
      );
      setMessages(data);
    } catch (error) {
      console.error("Error fetching messages:", error);
    }
  };

  async function storeChatMessages(currentChatId: string) {
    console.log("Storing messages of current chat:", messages);
    try {
      const response = await fetch(`/api/messages/${currentChatId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(messages),
      });

      if (!response.ok) {
        throw new Error("Failed to update messages");
      }

      if (response.status === 200) {
        console.log("Messages updated successfully");
      }
    } catch (error) {
      console.error("Error updating messages:", error);
    }
  }

  async function handleLogout(): Promise<void> {
    console.log("Logging out...", currentChatId);
    if (!newChat && currentChatId !== "") {
      await storeChatMessages(currentChatId);
    }
    await logout();
  }

  async function handleDeleteChat(chatId: string): Promise<void> {
    try {
      const response = await fetch(`/api/conversations/${chatId}`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        throw new Error("Failed to delete conversation");
      }

      const data = await response.json();
      if (data.status === 204) {
        console.log("Conversation deleted successfully");
        setRefreshConversations(!refreshConversations);
      }
    } catch (error) {
      console.error("Error updating messages:", error);
    }
  }

  return (
    <div className="flex h-screen bg-background">
      {authenticated && !loading && (
        <div
          className={`fixed inset-y-0 left-0 z-50 w-64 transform ${
            isSidebarOpen ? "translate-x-0" : "-translate-x-full"
          } transition-transform duration-300 ease-in-out overflow-hidden flex flex-col bg-secondary`}
        >
          <div className="p-4 border-b">
            <Button onClick={handleNewChat} className="w-full">
              <Plus className="mr-2 h-4 w-4" /> New Chat
            </Button>
          </div>
          <ScrollArea className="flex-grow p-4">
            {conversations
              .filter(
                (conversation): conversation is Conversation =>
                  conversation !== undefined
              )
              .sort(
                (a, b) =>
                  new Date(b.updatedAt).getTime() -
                  new Date(a.updatedAt).getTime()
              )
              .map((conversation) => (
                <div
                  key={conversation.id}
                  className={`group relative mb-2 ${
                    conversation.id === currentChatId
                      ? "bg-primary/10"
                      : "hover:bg-primary/5"
                  } rounded-md transition-colors flex justify-between items-center`}
                >
                  <Button
                    key={conversation.id}
                    variant="ghost"
                    className="flex-grow justify-start py-2 px-3 hover:bg-primary/20 text-left truncate"
                    onClick={() => handleChatSelect(conversation.id)}
                    style={{ maxWidth: "calc(100% - 40px)" }} // Adjust width to leave space for the dropdown
                  >
                    <span className="truncate">{conversation.title}</span>
                  </Button>
                  <div className="absolute right-2 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreVertical className="h-4 w-4" />
                          <span className="sr-only">More options</span>
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem
                          onClick={() => handleDeleteChat(conversation.id)}
                        >
                          <Trash2 className="mr-2 h-4 w-4" />
                          <span>Delete</span>
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </div>
              ))}
          </ScrollArea>
          <div className="p-4 border-t">
            <Button
              variant="secondary"
              className="w-full justify-center text-md"
              onClick={() => setIsSignOutVisible(!isSignOutVisible)}
              title={user?.email}
            >
              {user?.name || "User"}
            </Button>
            {isSignOutVisible && (
              <Button
                variant="destructive"
                className="w-full mt-2"
                onClick={() => handleLogout()}
              >
                <LogOut className="mr-2 h-4 w-4" /> Sign Out
              </Button>
            )}
          </div>
        </div>
      )}

      <div
        className={`flex-grow flex flex-col items-center w-full transition-all duration-300 ease-in-out ${
          authenticated && !loading && isSidebarOpen ? "ml-64" : "ml-0"
        }`}
      >
        <div className="sticky top-0 z-40 p-4 border-b flex items-center justify-between bg-background w-full max-w-3xl">
          <div className="flex items-center">
            {authenticated && !loading && (
              <>
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                >
                  <Menu className="h-6 w-6" />
                </Button>
                <h1 className="ml-4 text-xl font-semibold">
                  {currentChat?.title || "New Chat"}
                </h1>
              </>
            )}
          </div>
          {!authenticated && !loading && (
            <Link href="/login" passHref>
              <Button variant="outline">
                <LogIn className="mr-2 h-4 w-4" /> Login
              </Button>
            </Link>
          )}
        </div>

        <ScrollArea className="flex-grow p-4 w-full max-w-3xl">
          {!loading &&
            messages.map((message, index) => (
              <div
                key={index}
                className={`mb-4 ${
                  message.sender === "user" ? "text-right" : "text-left"
                }`}
              >
                {message.sender === "llm" && (
                  <div className="flex-shrink-0 mr-2">
                    <Bot className="h-6 w-6 text-primary" />
                  </div>
                )}
                <div
                  className={`inline-block p-2 rounded-lg max-w-[80%] ${
                    message.sender === "user"
                      ? "bg-primary text-primary-foreground"
                      : "bg-secondary text-secondary-foreground"
                  }`}
                >
                  {message.sender === "user" ? (
                    message.content
                  ) : (
                    <ReactMarkdown rehypePlugins={[rehypeRaw]}>
                      {DOMPurify.sanitize(message.content.trim())}
                    </ReactMarkdown>
                  )}
                </div>
              </div>
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
        </ScrollArea>

        <div className="sticky bottom-0 p-4 border-t bg-background w-full max-w-3xl">
          <form onSubmit={handleSubmit} className="flex space-x-2">
            <Input
              value={userInput}
              onChange={(e) => setUserInput(e.target.value)}
              placeholder="Type your message..."
              className="flex-grow"
            />
            <Button
              type="submit"
              size="icon"
              disabled={inputDisabled || !userInput.trim()}
            >
              <Send className="h-4 w-4" />
              <span className="sr-only">Send</span>
            </Button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default StreamChat;
