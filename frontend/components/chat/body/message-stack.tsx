import { MessageType } from "@/constants/message-type";
import MessageItem from "./message-item";

const MessageStack = ({ messages }: { messages: MessageType[] }) => {
    return (
      <div className="flex flex-col gap-1">
        {messages.map((msg, idx) => (
          <MessageItem
            key={msg.id}
            message={msg}
            showAvatar={idx === 0}
            showTimestamp={idx === messages.length - 1}
          />
        ))}
      </div>
    );
  };

export default MessageStack;