import { Skeleton } from "@/components/ui/skeleton";
import { Bot } from "lucide-react";

const ChatLoading = () => {
  return (
    <div className="mb-4 text-left">
      <div className="inline-flex items-start">
        <div className="flex-shrink-0 mr-2">
          <Bot className="h-6 w-6 text-primary" />
        </div>
        <div className="inline-block p-2 rounded-lg max-w-[90%] bg-secondary">
          <Skeleton className="h-4 w-[170px] mb-2" />
          <Skeleton className="h-4 w-[150px]" />
        </div>
      </div>
    </div>
  );
};

export default ChatLoading;
