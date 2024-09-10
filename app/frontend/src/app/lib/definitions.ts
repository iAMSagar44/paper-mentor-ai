export type MessageProps = {
  role: "user" | "assistant";
  text: string;
};

export interface EventData {
  fileNames: string[];
  token: string;
}