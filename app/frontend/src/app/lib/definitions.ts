export type MessageProps ={
  content: string;
  sender: "user" | "llm";
};

export type Conversation = {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
};

export type UserRequest = {
  message: string;
  isNewMessage: boolean;
  conversation_id: string;
}

export type AIResponse ={
  token: string;
  title: string;
  conversation_id: string;
}
export interface EventData {
  fileNames: string[];
  token: string;
  title: string;
  conversation_id: string;
}