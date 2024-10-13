import { MessageProps } from "@/app/lib/definitions";
import aichat from "../../../../public/ai.svg";
import user from "../../../../public/user.svg";
import Image from "next/image";
import styles from "./chat.module.css";
import ReactMarkdown from "react-markdown";
import DOMPurify from "dompurify";
import rehypeRaw from "rehype-raw";

export function Message({ message }: { message: MessageProps }) {
  function isMessageFromUser() {
    return message.role === "user";
  }

  return (
    <div
      className={`${
        isMessageFromUser() ? "place-self-end" : "place-self-start"
      } space-y-2`}
    >
      <div
        className={`bg-white p-5 rounded-2xl ${
          isMessageFromUser() ? "rounded-tr-none" : "rounded-tl-none"
        }`}
      >
        <div className="flex items-center">
          {isMessageFromUser() ? (
            <div>
              <Image src={user} alt="User" className="w-6 h-6" />
            </div>
          ) : (
            <div>
              <Image src={aichat} alt="AI" className="w-6 h-6" />
            </div>
          )}
        </div>
        <div
          className={
            isMessageFromUser() ? styles.userMessage : styles.assistantMessage
          }
        >
          {isMessageFromUser() ? (
            message.text
          ) : (
            <ReactMarkdown rehypePlugins={[rehypeRaw]}>
              {DOMPurify.sanitize(message.text.trim())}
            </ReactMarkdown>
          )}
        </div>
      </div>
    </div>
  );
}
